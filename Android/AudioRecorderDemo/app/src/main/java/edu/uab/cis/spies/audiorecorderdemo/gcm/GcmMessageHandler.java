package edu.uab.cis.spies.audiorecorderdemo.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.uab.cis.spies.audiorecorderdemo.Constants;
import edu.uab.cis.spies.audiorecorderdemo.MainActivity;

/**
 * Created by Prakashs on 7/31/15.
 */
public class GcmMessageHandler extends IntentService implements GCMCommands
    {
    private static final String LOG_TAG = GcmMessageHandler.class.getSimpleName();
    public static final String INSTRUCTION = "Instruction";
    String msg = "not set yet....";
    private Handler handler;
    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        // Message from server
        msg = extras.getString("message");
        showToast();
        if(msg!=null)
        {
            if(msg.startsWith(GCM_START))
            {
                //send msg to wear to initiate sensor recordings

                String[] str = msg.split(",");
                String directoryName;
                if(str.length == 2){
                    directoryName = str[1];
                    Intent dialogIntent = new Intent(getBaseContext(), MainActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    dialogIntent.putExtra(Constants.DIRECTORY_NAME,directoryName);
                    getApplication().startActivity(dialogIntent);
                }
        //Log.d(LOG_TAG, msg);
            }
            else if(msg.equalsIgnoreCase(GCM_STOP))
            {
                Log.e("GcmMsgHandler()", "Stop service received");
                Intent in = new Intent(INSTRUCTION);
                in.putExtra("From", LOG_TAG);
                in.putExtra("Message","STOP");
                LocalBroadcastManager.getInstance(this).sendBroadcast(in);


            }
        }
        Log.i("GCM", "Received : (" + messageType + ")  " + extras.getString("message"));
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    public void showToast(){
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
