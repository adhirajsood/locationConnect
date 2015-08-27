package com.task.phone.joisterproject;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by adhiraj on 26/8/15.
 */

public class APIManager implements IAsyncCallback {

    private int timeout = 15000;
    private static APIManager manager;
    String baseUrl;
    private  APIManager(){}


    public static APIManager getInstance(){
        if (manager == null){
            manager=new APIManager();
        }
        return manager;
    }

    public void sendAsyncCall(String requestMethod, String lat, String longitude, IAsyncCallback callback){

        AsyncCallHandler handler = new AsyncCallHandler(requestMethod,lat,longitude,callback);
        handler.execute();
    }

    @Override
    public void onSuccessResponse(String successResponse) {

    }

    @Override
    public void onErrorResponse(int errorCode, String errorResponse) {

    }


    class AsyncCallHandler extends AsyncTask<Void,Void,String> {
        private String longitude;
        private String requestMethod;
        private String lat;
        private IAsyncCallback callback;
        private boolean initialized = false;
        private String reply = null;


        private int responseCode = 0;

        public AsyncCallHandler(String requestMethod, String lat, String longitude,IAsyncCallback callback){

            this.requestMethod = requestMethod;
            this.lat = lat;
            this.longitude = longitude;
            this.callback = callback;
            initialized = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            if(!initialized){
                Log.e("API manager", "doInBackground : Request not initialized. Cancelling ");
                return null;
            }
            try {
                sendJSONRequest();
            } catch (Exception e) {
                Log.e("API manager", "doInBackground : Failed to process " + " " + requestMethod, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            callback.onSuccessResponse(reply);

        }


        private void sendJSONRequest() throws Exception {


            if(longitude!=null && lat!=null){

                baseUrl = "http://www.joister.com/cart-api/locations/c/271237e031f34e4351820a60e13b935a/k/24138/latitude/"+lat+"/longitude/"+longitude+"";

            }
            URL obj = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            //add request header
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod(requestMethod);

            connection.connect();

            responseCode = connection.getResponseCode();

            BufferedReader bufferedReader = null;

            InputStream errorStream = connection.getErrorStream();

            if(errorStream==null){
                InputStream inputStream = connection.getInputStream();
                bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream));
            }else{
                bufferedReader = new BufferedReader(
                        new InputStreamReader(errorStream));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
            bufferedReader.close();

            if(response!=null){
                reply = response.toString();
            }
            return;

        }

    }

}