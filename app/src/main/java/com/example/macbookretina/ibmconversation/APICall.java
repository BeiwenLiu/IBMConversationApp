package com.example.macbookretina.ibmconversation;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by MacbookRetina on 10/6/16.
 */
public class APICall {
    String request;
    String id;

    public String getID() {
        return id;
    }

    public void setURL(String url) {
        this.request = url;
    }

    public void setID(String id) {
        this.id = id;
    }

    public JSONObject sendRequest(String input, String seatNumber) throws IOException, JSONException
    {
        System.setProperty("http.keepAlive", "false");
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject out = null;

        JSONObject cred   = new JSONObject();
        cred.put("text", input);
        cred.put("seat", Integer.parseInt(seatNumber));
        cred.put("demo_id", id);
        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(cred.toString());
        wr.close();
        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            System.out.println("" + sb.toString());
            out = new JSONObject(sb.toString());
        } else {
            System.out.println(connection.getResponseMessage());
        }
        System.out.println(out);
        connection.disconnect();
        wr.close();
        return out;
    }

    public JSONObject sendRequestJson(String input, String seatNumber) throws IOException, JSONException
    {
        String request = "https://mono-v.mybluemix.net/conversation";
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject out = null;

        JSONObject cred   = new JSONObject();
        cred.put("text",input);
        cred.put("seat", Integer.parseInt(seatNumber));

        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(cred.toString());
        wr.close();
        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            System.out.println("" + sb.toString());
            out = new JSONObject(sb.toString());
        } else {
            System.out.println(connection.getResponseMessage());
        }

        connection.disconnect();
        return out;
    }

    public byte[] sendTTS(String input) throws IOException, JSONException
    {
        byte[] buffer = null;
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject cred   = new JSONObject();
        cred.put("text", input);

        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(cred.toString());
        wr.close();

        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {

            InputStream is = connection.getInputStream();
            DataInputStream in = new DataInputStream(is);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            buffer = new byte[50];
            int len = 0;
            int sum = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
                sum += len;
            }


            buffer = out.toByteArray();
        } else {
            System.out.println(connection.getResponseMessage());
        }

        connection.disconnect();
        return buffer;
    }

    public JSONObject demoinit() throws IOException, JSONException
    {
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject out = null;

        JSONObject cred   = new JSONObject();

        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(cred.toString());
        wr.flush();

        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            out = new JSONObject(sb.toString());
        } else {
            System.out.println(connection.getResponseMessage());
        }
        setID(out.get("demo_id").toString());
        for (int i = 1; i < 5; i++) {
            profileinit(String.valueOf(i));
        }

        return out;
    }

    public void profileinit(String seatNumber) throws IOException, JSONException
    {
        URL url = new URL("https://mono-v.mybluemix.net/demo/profile");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject out = null;

        JSONObject cred   = new JSONObject();
        cred.put("demo_id",id);
        cred.put("profile_id", "1234");
        cred.put("seat", seatNumber);

        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(cred.toString());
        wr.flush();

        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            System.out.println("done");
        } else {
            System.out.println(connection.getResponseMessage());
        }

    }


    public JSONObject demoEnd() throws IOException, JSONException
    {
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject out = null;

        JSONObject cred   = new JSONObject();

        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(cred.toString());
        wr.close();

        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            out = new JSONObject(sb.toString());
        } else {
            System.out.println(connection.getResponseMessage());
        }


        connection.disconnect();
        return out;
    }

    public String feedback(Map<String,String> input) throws IOException, JSONException
    {
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject out = null;

        JSONObject cred   = new JSONObject();
        for (String key : input.keySet()) {
            System.out.println("key " + key);
            System.out.println("value " + input.get(key));
            cred.put(key, input.get(key));
        }
        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(cred.toString());
        wr.close();
        String answer = null;
        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        System.out.println(HttpResult);
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            answer = "OK";
        } else {
            System.out.println(connection.getResponseMessage());
        }

        connection.disconnect();
        return answer;
    }
}
