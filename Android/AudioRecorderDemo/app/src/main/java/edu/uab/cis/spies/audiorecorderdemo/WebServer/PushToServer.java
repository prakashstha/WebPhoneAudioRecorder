package edu.uab.cis.spies.audiorecorderdemo.WebServer;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prakashs on 7/21/2015.
 */
public class PushToServer extends AsyncTask<String, Void, String> {
    private String LOG_TAG = PushToServer.class.getSimpleName();
    private String URL; // = "http://students.cis.uab.edu/prakashs/EventsReader/mReceiveData.php";
    private final String RTTCalcURL = "http://students.cis.uab.edu/prakashs/WebAudioRecorderDemo/calcTimeOffset.php";
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    public static final String RTTCalculation = "RTTCalculation";
    public AsyncResponse delegate = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... arg) {
        // TODO Auto-generated method stub
        String msgKey = arg[0];
        buildParams(arg);

        HttpServiceHandle serviceClient = new HttpServiceHandle();

        String json = serviceClient.makeServiceCall(URL, HttpServiceHandle.POST, params);


        if (json != null) {
            return json;
        } else {
            Log.e(LOG_TAG, "JSON data error!");
        }
        return null;
    }



    @Override
    protected void onPostExecute(String result) {
        delegate.handleResponse(result);
    }

    private void buildParams(String... arg)
    {
        String msgKey = arg[0];
        if(msgKey.equalsIgnoreCase(RTTCalculation)){
            initParamsForRTTCalculation(arg);
        }

    }

    private void initParamsForRTTCalculation(String... arg){
        String requestTime = arg[1];
        // Log.d(LOG_TAG, "initParamsForRTTCalculation()>>Request Time: " + requestTime);
        URL = RTTCalcURL;
        // Preparing post params
        params.add(new BasicNameValuePair("request_time", requestTime));

    }
}
