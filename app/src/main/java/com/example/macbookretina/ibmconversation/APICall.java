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

/**
 * Created by MacbookRetina on 10/6/16.
 */
public class APICall {
    String request;

    public void setURL(String url) {
        this.request = url;
    }

    public String sendRequest(String input, String seatNumber) throws IOException, JSONException
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
            System.out.println("" + sb.toString());
            out = new JSONObject(sb.toString());
        } else {
            System.out.println(connection.getResponseMessage());
        }
        return out.get("transcription").toString();
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
            System.out.println("" + sb.toString());
            out = new JSONObject(sb.toString());
        } else {
            System.out.println(connection.getResponseMessage());
        }
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
        wr.flush();

        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {

            InputStream is = connection.getInputStream();
            DataInputStream in = new DataInputStream(is);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            buffer = out.toByteArray();
        } else {
            System.out.println(connection.getResponseMessage());
        }
        return buffer;
    }
}
