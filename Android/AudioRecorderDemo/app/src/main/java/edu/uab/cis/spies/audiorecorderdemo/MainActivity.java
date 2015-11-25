package edu.uab.cis.spies.audiorecorderdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import edu.uab.cis.spies.audiorecorderdemo.TimeSynchronizer.ServerTimeSynchronizer;
import edu.uab.cis.spies.audiorecorderdemo.gcm.GcmMessageHandler;


public class MainActivity extends Activity implements View.OnClickListener, Constants {
    RecordAudio recordTask;
    String LOG_TAG = "Records";
    Button startRecordingButton, stopRecordingButton;
    TextView statusText, txtRegID;

    private String tempFile, wavFile, audioTimeFile;

   private volatile boolean isRecording = false,isPlaying = false;

    private GoogleCloudMessaging gcm = null;
    private String regid;
    private final String PROJECT_NUMBER = "730187203336";


    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    int bufferSize = 0;
    String formattedDate = "";
    private ServerTimeSynchronizer timeSynchronizer;
    private ThreadGroup threadGroup = new ThreadGroup("AudioRecorder");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bufferSize = AudioTrack.getMinBufferSize(SAMPLERATE, AUDIO_CHANNEL_CONFIG, AUDIO_ENDCODING);
        initializeView();
        Intent intent = getIntent();
        formattedDate = intent.getStringExtra(Constants.DIRECTORY_NAME);
        txtRegID.setText(formattedDate);
        if(formattedDate!=null){
            if(formattedDate.length()!=0){
                startServices();
            }
        }
        else{
            formattedDate = getFormattedData();
            startServices();
        }
    }

    private void stopServices(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if(timeSynchronizer!=null){
            if(timeSynchronizer.isAlive()){
                timeSynchronizer.interrupt();
            }
        }

        if(isRecording){
            stopRecording();
        }
    }
    private void startServices(){
        stopServices();
        IntentFilter iff = new IntentFilter(GcmMessageHandler.INSTRUCTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, iff);

        if(formattedDate.length() == 0 || formattedDate==null){
            formattedDate = getFormattedData();
            Log.d(LOG_TAG, "in local device..."+formattedDate);
        }


        timeSynchronizer = new ServerTimeSynchronizer(threadGroup,formattedDate);
        timeSynchronizer.setDaemon(true);
        timeSynchronizer.setPriority(Thread.NORM_PRIORITY);
        timeSynchronizer.start();
        Log.d(LOG_TAG,"timeSync started");

        tempFile =getTempFilename();
        wavFile = getAudioFilePath();
        audioTimeFile = getAudioTimeInfoFilePath();
        record();




    }

    private void initializeView(){
        statusText = (TextView) this.findViewById(R.id.StatusTextView);
        txtRegID = (TextView) this.findViewById(R.id.txt_regID);
        startRecordingButton = (Button) this
                .findViewById(R.id.StartRecordingButton);
        stopRecordingButton = (Button) this
                .findViewById(R.id.StopRecordingButton);


        startRecordingButton.setOnClickListener(this);
        stopRecordingButton.setOnClickListener(this);

        startRecordingButton.setEnabled(false);
    }

    private String getFormattedData()
    {
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd-HH-mm");
        Date date = new Date();
        return dateFormat.format(date);
    }
    private String getAudioFilePath(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" +  formattedDate +"mAudio" +AUDIO_RECORDER_FILE_EXT_WAV);
    }
    private String getAudioTimeInfoFilePath(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + formattedDate +"mAudioTime.csv");
    }
    public void regiterOnClick(View v){
        getRegID();
    }

    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v == startRecordingButton){
            startServices();
        }
        else if(v == stopRecordingButton){
            stopServices();
        }


    }

    public void stopPlaying() {
        isPlaying = false;

    }
    public void record() {
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);

        recordTask = new RecordAudio();
        recordTask.execute();
    }
    public void stopRecording() {
        isRecording = false;
    }
    private class PlayAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;
            short[] audiodata = new short[bufferSize / 4];
            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC, SAMPLERATE,
                        AUDIO_CHANNEL_CONFIG, AUDIO_ENDCODING, bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();
                while (isPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < audiodata.length) {
                        audiodata[i] = dis.readShort();
                        i++;
                    }
                    audioTrack.write(audiodata, 0, audiodata.length);
                }
                dis.close();

            } catch (Throwable t) {
                Log.e("AudioTrack", "Playback Failed");
            }
            return null;
        }
    }

    private class RecordAudio1 extends TwoFactorThread{

        public RecordAudio1(ThreadGroup tGroup){
            super(tGroup,"RecordAudio");
        }

        @Override
        protected void mainloop() throws InterruptedException {
            isRecording = true;
            try {
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                new File(tempFile))));

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, SAMPLERATE,
                        AUDIO_CHANNEL_CONFIG, AUDIO_ENDCODING, bufferSize);

                byte[] buffer = new byte[bufferSize];
                audioRecord.startRecording();
                int r = 0;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    dos.write(buffer);
                    if(isInterrupted()){
                        Log.d(LOG_TAG + "audio record","Interrupted");
                        break;
                    }
                }
                audioRecord.stop();
                dos.close();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
        }
    }
    private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        private BufferedWriter bfrWriter = null;
        String timeInfo = "";
        @Override
        protected Void doInBackground(Void... params) {
            /* make ready a buffer writer to write about the audio time info */

            isRecording = true;
            try {
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                new File(tempFile))));

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, SAMPLERATE,
                        AUDIO_CHANNEL_CONFIG, AUDIO_ENDCODING, bufferSize);

                byte[] buffer = new byte[bufferSize];
                audioRecord.startRecording();

                timeInfo += "AUDIO_START_TIME, " + System.currentTimeMillis();

                int r = 0;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    //Log.d(LOG_TAG,"data size: "+bufferReadResult);
//                    for (int i = 0; i < bufferReadResult; i++) {
//                        //Log.d(LOG_TAG, "data: " + buffer[i]);
//                        dos.writeShort(buffer[i]);
//                    }
                    dos.write(buffer);
                    publishProgress(new Integer(r));
                    r++;
                }
                timeInfo += ",AUDIO_STOP_TIME," + System.currentTimeMillis();
                audioRecord.stop();
                dos.close();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        private void savedTime(String msg) {
            bfrWriter = getAudioTimeBufferWriter(audioTimeFile);
            if(bfrWriter==null){
                throw new IllegalAccessError("BufferWriter null pointer exception");
            }

            try {
                bfrWriter.append(msg);
                bfrWriter.flush();
                bfrWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            statusText.setText(progress[0].toString());
        }
        protected void onPostExecute(Void result) {
            startRecordingButton.setEnabled(true);
            stopRecordingButton.setEnabled(false);
            Log.d("stopRecording()", "copying to wav file...");
            copyWaveFile(getTempFilename(),getAudioFilePath());

            timeInfo += ",AUDIO_SAVED_TIME," + System.currentTimeMillis();
            savedTime(timeInfo);

            Log.d("stopRecording()", "copied");
        }
        private BufferedWriter getAudioTimeBufferWriter(String filePath){
            BufferedWriter bfrWriter = null;
            try {
                if(bfrWriter==null){
                    if(filePath == null || filePath.length() == 0){
                        filePath = getAudioTimeInfoFilePath();
                    }
                    Log.d(LOG_TAG, "Audio Info file path: " +  filePath);
                    bfrWriter = new BufferedWriter(new FileWriter(filePath));
                }
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
            return bfrWriter;
        }
    }
//
//    public void playRawAudio(View v){
//        startPlaybackButton.setText("playraw");
//        Log.d("Main", "PlayRawAudio()");
//        AudioPlayer player = new AudioPlayer();
//        player.setDaemon(true);
//        player.start();
//    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        int intTotalAudioLen = 0;

        int totalDataLen = intTotalAudioLen + 44;
        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);

            totalAudioLen = in.getChannel().size();
            if((totalAudioLen + 44) >Integer.MAX_VALUE){
                throw new RuntimeException("file size is greate that expected");
            }
            intTotalAudioLen = (int)totalAudioLen;
            totalDataLen = intTotalAudioLen + 44;

            Log.d("CopyWaveFile()", "File size: " + totalDataLen);

            writeWaveFileHeader(out, intTotalAudioLen, totalDataLen,
                    SAMPLERATE, channels, BPP);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        statusText.setText("Finished recordings");
    }

    public void writeWaveFileHeader(FileOutputStream out,
                                    int totalAudioLen,
                                    int totalDataLen,
                                    int sampleRate,
                                    short noOfChannels,
                                    short BPP) throws IOException {

        String riffHeader = "RIFF";
        String waveHeader = "WAVE";
        String fmtHeader = "fmt ";
        String data = "data";


        int lengthOfFormat = 16;
        short typeOfFormat = 1; //1 for PCM
        int bytesRate = sampleRate * BPP * noOfChannels/8;
        System.out.println("Byte Rate: " + bytesRate);
        short totalBytesPerSample =  (short) ((short)(BPP * noOfChannels)/8);

        int allocSize = 44; //default header size
        /**
         * riffHeader.getBytes().length + waveHeader.getBytes().length + fmtHeader.getBytes().length + data.getBytes().length + INT_SIZE*5 + SHORT_SIZE*4;
         */
        ByteBuffer headerBuffer = ByteBuffer.allocate(allocSize);

        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

		 /* RIFF (4 bytes) */
        headerBuffer.put(riffHeader.getBytes());
		 /* File Size (4 bytes) */
        headerBuffer.putInt(totalDataLen);
		 /* WAVE (4 byte) */
        headerBuffer.put(waveHeader.getBytes());
		 /* fmt (4 bytes) */
        headerBuffer.put(fmtHeader.getBytes());
		 /* Length of format data as listed above (4 bytes) */
        headerBuffer.putInt(lengthOfFormat);
		 /* Type of format (1 for PCM) 2 bytes */
        headerBuffer.putShort(typeOfFormat);
		 /*Number of channels (2 bytes)*/
        headerBuffer.putShort(noOfChannels);
		 /*Sample Rate (4 bytes)*/
        headerBuffer.putInt(sampleRate);
		 /*number of bytes in 1 seconds (4 bytes)*/
        headerBuffer.putInt(bytesRate);
		 /*number of bytes in 1 sample (combining both channel) (2 bytes)*/
        headerBuffer.putShort(totalBytesPerSample);
		 /*Bits per sample (2 bytes)*/
        headerBuffer.putShort(BPP);
		 /*data (4 bytes)*/
        headerBuffer.put(data.getBytes());
		 /*File size (4 bytes)*/
        headerBuffer.putInt(totalAudioLen);

        //displayHeaderContent(headerBuffer);
        out.write(headerBuffer.array());

    }

    public void displayHeaderContent(ByteBuffer headerBuffer){
        byte[] arr = headerBuffer.array();
        int start, end;


        for(int i = 0;i<arr.length;i++){
            System.out.println("Header["+(i++) +"]:"+(char)arr[i]);
        }
        //RIFF (4 bytes)
        start = 0;
        end = 4; //"RIFF" is 4 byte;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(new String(Arrays.copyOfRange(arr, start, end)));

        //File Size (4 byte)
        start = end;
        end = start  + Integer.SIZE/8;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

	      /*WAVE (4 byte)*/
        start = end;
        end = start + 4; //"WAVE" is 4 byte
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(new String(Arrays.copyOfRange(arr, start, end)));

		 /* fmt (4 byte)*/
        start = end;
        end = start + 4; // "fmt " followed by null is 4 byte
        String fmt = (new String(Arrays.copyOfRange(arr, start, end)));
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(fmt + " leng: " + fmt.length());

		  /*Length of format data as listed above (4 bytes)*/
        start = end;
        end = start + INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*Type of format (1 for PCM) 2 bytes*/
        start = end;
        end = start + SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		   /*Number of channels (2 bytes)*/
        start = end;
        end = start + SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		  /*Sample Rate (4 bytes)*/
        start = end;
        end = start + INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*number of bytes in 1 seconds (4 bytes)*/
        start = end;
        end = start + INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*number of bytes in 1 sample (combining both channel) (2 bytes)*/
        start = end;
        end = start + SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		  /*Bits per sample (2 bytes)*/
        start = end;
        end = start + SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		  /*data (4 bytes)*/
        start = end;
        end = start + 4; //"data" is 4 byte
        String d = new String(Arrays.copyOfRange(arr, start, end));
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(d);

		  /*File size (4 bytes)*/
        start = end;
        end = start + INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

    }

    public void getRegID() {
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);

                    msg = "Registration ID=" + regid;
                    Log.i("GCM", msg);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
               txtRegID.setText("ID:" + msg + "\n");
            }
        }.execute(null, null, null);
    }



    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop()");
        super.onStop();
        stopServices();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
       stopServices();
        super.onDestroy();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive() BroadCastReceiver...");
            String from = intent.getStringExtra("From");
            String message = intent.getStringExtra("Message");
            Log.d(LOG_TAG,"From: " +  from + " Message: "+ message);
            if(message.equalsIgnoreCase("Stop")){
                stopRecording();
                if(timeSynchronizer!=null){
                    if(timeSynchronizer.isAlive()){
                        timeSynchronizer.interrupt();
                        Log.d(LOG_TAG,"Waiting for timeSyncronizer to stopped");
                        while(timeSynchronizer.isAlive()){
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        Log.d(LOG_TAG,"TimeSynchronizer stopped");
                    }
                }
            }
            finish();
        }
    };

}
