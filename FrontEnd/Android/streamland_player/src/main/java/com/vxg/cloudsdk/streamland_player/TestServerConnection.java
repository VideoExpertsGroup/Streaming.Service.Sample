package com.vxg.cloudsdk.streamland_player;

import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by bleikher on 10.01.2018.
 */

public class TestServerConnection {
    public final String TAG = TestServerConnection.class.getSimpleName();

    private static final int readTimeout = 10000;
    private static final int connectTimeout = 15000;

    HttpCookie mCookie = null;
    String mServerAddress = null;

    public int do_login(String server_addr, String login, String password){

        mCookie = null;
        mServerAddress = server_addr;

        JSONObject data = new JSONObject();
        try {
            data.put("user_name", login);
            data.put("user_password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String endpoint = "";
        if(!server_addr.startsWith("http")){
            endpoint += "http://";
        }
        endpoint += server_addr;
        endpoint += "/TestServer/index.php?action=login&json";
        Pair<Integer, String> p = null;
        try {
            p = executePostRequest(endpoint, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (p==null?-1:p.first);
    }

    public JSONObject get_channels(){

        if(mServerAddress == null || mCookie == null)
            return null;

        String endpoint = "";
        if(!mServerAddress.startsWith("http")){
            endpoint += "http://";
        }
        endpoint += mServerAddress;
        endpoint += "/TestServer/index.php?action=channels&json";
        Pair<Integer, String> p = null;
        try {
            p = executeGetRequest(endpoint, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(p==null || p.second==null)
            return null;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(p.second);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private Pair<Integer,String> executeGetRequest(String endpoint, JSONObject data) throws IOException {
        URL uri = new URL(endpoint);
        Log.i(TAG, "executeGetRequest " + uri.toString());

        HttpURLConnection urlConnection = (HttpURLConnection) uri.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(readTimeout);
        urlConnection.setConnectTimeout(connectTimeout);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-type", "application/json");
        if(mCookie != null){
            urlConnection.setRequestProperty("Cookie", mCookie.getName()+"="+mCookie.getValue());
        }

        if (data != null) {
            urlConnection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(data.toString());
            wr.flush();
            wr.close();
        } else {
            urlConnection.setDoOutput(false);
        }

        StringBuilder buffer = new StringBuilder();

        Log.d(TAG, "GET ResponseCode: " + urlConnection.getResponseCode() + " for URL: " + uri);
        if (urlConnection.getResponseCode() == 401) {
            //throw new IOException("invalid_auth 401");
            return new Pair<>(urlConnection.getResponseCode(), buffer.toString());
        }

        readCookie(urlConnection);

        int codeResponse = urlConnection.getResponseCode();
        boolean isError = codeResponse >= 400;
        InputStream inputStream = isError ? urlConnection.getErrorStream() : urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        return new Pair<>(codeResponse, buffer.toString());
    }

    private Pair<Integer,String> executePostRequest(String endpoint, JSONObject data) throws IOException {
        URL uri = new URL(endpoint);
        Log.i(TAG, "executePostRequest " + uri.toString());

        HttpURLConnection urlConnection = (HttpURLConnection) uri.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setReadTimeout(readTimeout);
        urlConnection.setConnectTimeout(connectTimeout);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-type", "application/json");
        if(mCookie != null){
            urlConnection.setRequestProperty("Cookie", mCookie.getName()+"="+mCookie.getValue());
        }

        if (data != null) {
            urlConnection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(data.toString());
            wr.flush();
            wr.close();
        } else {
            urlConnection.setDoOutput(false);
        }

        StringBuilder buffer = new StringBuilder();

        Log.d(TAG, "POST ResponseCode: " + urlConnection.getResponseCode() + " for URL: " + uri);
        if (urlConnection.getResponseCode() == 401) {
            //throw new IOException("invalid_auth 401");
            return new Pair<>(urlConnection.getResponseCode(), buffer.toString());
        }

        readCookie(urlConnection);

        int codeResponse = urlConnection.getResponseCode();
        boolean isError = codeResponse >= 400;
        InputStream inputStream = isError ? urlConnection.getErrorStream() : urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        return new Pair<>(codeResponse, buffer.toString());
    }

    private void readCookie(HttpURLConnection urlConnection) {
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");

        if(cookiesHeader != null){
            for (String cookie : cookiesHeader) {
                List<HttpCookie> cookies;
                try {
                    cookies = HttpCookie.parse(cookie);
                } catch (NullPointerException e) {
                    Log.e(TAG, "Wrong cookie string " + cookie);
                    //ignore the Null cookie header and proceed to the next cookie header
                    continue;
                }
                mCookie = cookies.get(0);
            }
        }
    }

}
