package edu.uab.cis.spies.audiorecorderdemo.WebServer;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import edu.uab.cis.spies.audiorecorderdemo.TwoFactorThread;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class PushToServer extends TwoFactorThread {
    private static final String LOG_TAG = PushToServer.class.getSimpleName();
    private String URL; // = "http://students.cis.uab.edu/prakashs/EventsReader/mReceiveData.php";
    private final String RTTCalcURL = "http://students.cis.uab.edu/prakashs/EventsReader/calcTimeOffset.php";
    private final String GCMDeviceRegistrationURL = "http://students.cis.uab.edu/prakashs/EventsReader/gcm/registerDevice.php";
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    public static final String GCMDeviceRegistration = "GCMDeviceRegistration";
    public static final String RTTCalculation = "RTTCalculation";
    public static final String ServerTimeRequest = "ServerTimeRequest";
    public AsyncResponse delegate = null;
    private String args[];
    private volatile String json;

    public PushToServer(ThreadGroup sThreadGroup, String args[]){
        super(sThreadGroup, LOG_TAG);
        this.args = args;
    }


    @Override
    public void mainloop() {
        // TODO Auto-generated method stub
        String msgKey = args[0];
        buildParams(args);

        HttpServiceHandle serviceClient = new HttpServiceHandle();

        json = serviceClient.makeServiceCall(URL, HttpServiceHandle.POST, params);

        if (json == null) {
            Log.e(LOG_TAG, "JSON data error!");
        }else{
            if(delegate!=null)
                delegate.handleResponse(json);
        }
    }

//    private void handleResponse(String json, String msgKey)
//    {
//        try {
//            JSONObject jsonObj = new JSONObject(json);
//            boolean error = jsonObj.getBoolean("error");
//            // checking for error node in json
//            if (!error) {
//                Log.d(LOG_TAG, "Success in " + msgKey + " >> " + jsonObj.getString("message"));
//            } else {
//                Log.e(LOG_TAG, "Error while " + msgKey + " >> " + jsonObj.getString("message"));
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }



    private void buildParams(String... arg)
    {
        String msgKey = arg[0];
        if(msgKey.equalsIgnoreCase(GCMDeviceRegistration)) {
            initParamsForGCMDeviceRegistration(arg);
        }
        else if(msgKey.equalsIgnoreCase(RTTCalculation)){
            initParamsForRTTCalculation(arg);
        }

    }
    //init URL as well as params to push to the server
    private void initParamsForGCMDeviceRegistration(String... arg){
        String projectName = arg[1];
        String regId = arg[2];
        URL = GCMDeviceRegistrationURL;
        // Preparing post params
        params.add(new BasicNameValuePair("projectName", projectName));
        params.add(new BasicNameValuePair("regId", regId));


    }
    private void initParamsForRTTCalculation(String... arg){
        String requestTime = arg[1];
        // Log.d(LOG_TAG, "initParamsForRTTCalculation()>>Request Time: " + requestTime);
        URL = RTTCalcURL;
        // Preparing post params
        params.add(new BasicNameValuePair("request_time", requestTime));

    }
}