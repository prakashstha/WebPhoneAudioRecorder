package edu.uab.cis.spies.audiorecorderdemo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by Prakashs on 8/17/15.
 */
public class AudioPlayer extends Thread{

        private final String LOG = AudioPlayer.class.getSimpleName();
        private final String LOG_TAG = AudioPlayer.class.getSimpleName();
        private boolean isPlaying = false;
        private String recordingFile = null;

        private String tempAudioFilePath, audioFilePath;

    public AudioPlayer(){
        String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
        tempAudioFilePath = sdCard + File.separator +"sensor_values/web" + File.separator + "audio_data.csv";
        audioFilePath  = sdCard + File.separator + "sensor_values/web" + File.separator + "audio.wav";
        Log.d(LOG, "temp : " + tempAudioFilePath+">> \naudio: " + audioFilePath);

    }


    /* Play audio */
    @Override
    public void run() {
         final int RECORDER_BPP = 16;
          final int RECORDER_SAMPLERATE = 44100;//44100;
          final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
          //final int channels = 2;
          final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        //findAudioRecord();
        Log.d(LOG_TAG, "START");
           /* Get recording file to be played*/
           if((recordingFile = getRecordingFile())!=null){
                isPlaying = true;
               /*
               * audio is recorded with CHANNEL_IN_MONO configuration
               * playing with CHANNEL_IN_STEREO configuration
               * CHANNEL_IN_STEREO is just copy of CHANNEL_IN_MONO in two channel
               */
                int channel = AudioFormat.CHANNEL_IN_STEREO;
                int bufferSize = AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE,
                        channel, RECORDER_AUDIO_ENCODING);
                short[] audiodata = new short[bufferSize / 4];

                try {
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(recordingFile)));
                    AudioTrack audioTrack = new AudioTrack(
                            AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE,
                            channel, RECORDER_AUDIO_ENCODING, bufferSize,
                            AudioTrack.MODE_STREAM);

                    audioTrack.play();
                    Log.d(LOG_TAG, "RUNNING");
                    while (isPlaying && dis.available() > 0) {
                        int i = 0;
                        while (dis.available() > 0 && i < audiodata.length) {
                            audiodata[i] = dis.readShort();
                            audiodata[i+1] = audiodata[i];
                            i=i+2;
                        }
                        audioTrack.write(audiodata, 0, audiodata.length);
                        if(isInterrupted()){
                            break;
                        }
                    }
                    dis.close();

                } catch (Throwable t) {
                    Log.e(LOG_TAG,  "EXCEPTION");
                    throw new RuntimeException(t);
                }
           }
            else{
                Log.e(LOG_TAG,"EXCEPTION: No recording file found.");
                throw new RuntimeException("No recording file found.");
            }
        Log.d(LOG_TAG,"END");
        }

    /*
    * Copy audio data from csv file and create audio.wav file with CHANNEL_IN_MONO configuration
    * */
    private String getRecordingFile()  {
        String recoringFile = audioFilePath;
        String csvFile = tempAudioFilePath;
        DataOutputStream dos = null;
        BufferedReader br = null;
        String line = "";
        String csvSplit = ",";
        Log.d(LOG_TAG,"Starting writing on Audio.wav file");
        Log.d(LOG_TAG,"Recording file :"+recoringFile);
        Log.d(LOG_TAG,"CSV file: " + csvFile);
        try{
            dos =  new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFilePath)));
            br = new BufferedReader(new FileReader(csvFile));
            while((line = br.readLine())!=null){
                String[] val = line.split(csvSplit);
                for(int i = 2;i<val.length;i++){
                    short sval = Short.parseShort(val[i]);
                    dos.writeShort(sval);
                }
            }
            Log.d(LOG_TAG,"Reading writing completes");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                    Log.d(LOG_TAG,"br closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dos != null) {
                try {
                    dos.close();
                    Log.d(LOG_TAG,"dos closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return recoringFile;
        }
    }

    /*
    * Find the possible audio configuration settings on the device
    */
    public void findAudioRecord() {
        int[] mSampleRates = new int[]{8000, 11025, 22050, 44100};
        int noOfShorts = 0;

        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {

                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {

                            short []sData = new short[bufferSize];
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {

                                Log.e(LOG_TAG, "Initialized for ::>>>>>");
                                Log.d(LOG_TAG, "Rate " + rate + "Hz, bytes: " + audioFormat + ", channel: "
                                        + channelConfig);
                                recorder.startRecording();
                                for(int i = 0;i<5;i++){
                                    noOfShorts = recorder.read(sData, 0, bufferSize);
                                    Log.d(LOG_TAG, "no of shorts read: " + noOfShorts);
                                }
                                recorder.stop();
                                recorder = null;
                            }
                           }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
    }


}
