package edu.uab.cis.spies.audiorecorderdemo.WebServer;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by prakashs on 7/21/2015.
 */
public class HttpServiceHandle {
    private String LOG_TAG = HttpServiceHandle.class.getSimpleName();
    private InputStream is = null;
    private String response = null;
    public final static int GET = 1;
    public final static int POST = 2;

    public HttpServiceHandle(){

    }
    public String makeServiceCall(String url, int method){
        return this.makeServiceCall(url, method, null);
    }
    public String makeServiceCall(String url, int method, List<NameValuePair> params){
        // Log.d(LOG_TAG, "inside makeService call");
        try{
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            if(method == POST){
                //Log.d(LOG_TAG, "inside post method of service call call");
                HttpPost httpPost = new HttpPost(url);
                if(params!=null){
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }
                httpResponse = httpClient.execute(httpPost);
            }
            else if(method == GET){
                if(params!=null){
                    String paramString = URLEncodedUtils.format(params,"utf-8");
                    url+="?"+paramString;
                }
                HttpGet httpGet = new HttpGet(url);
                httpResponse = httpClient.execute(httpGet);
            }
            httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        }catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            response = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error: " + e.toString());
        }

        return response;
    }
}
