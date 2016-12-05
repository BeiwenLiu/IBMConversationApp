package com.example.macbookretina.ibmconversation;

import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by MacbookRetina on 11/23/16.
 */

/*
    IBMTTS tts = IBMTTS("PathToSDCard"); //If you leave blank, it will default to a path
    String path = tts.sendTTS("hello"); //"Returned wav will be stored in path and return String path
 */
    
public class IBMTTS {

    String outputFile;

    public IBMTTS() {
        this(Environment.getExternalStorageDirectory() + "/transcript.wav");
    }
    public IBMTTS(String outputFile) {
        this.outputFile = outputFile;
    }

    public void setDest(String outputFile) {
        this.outputFile = outputFile;
    }

    public String sendTTS(String input) throws IOException, JSONException
    {
        byte[] buffer = null;
        URL url = new URL("https://mono-v.mybluemix.net/tts");
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
            OutputStream out = new FileOutputStream(outputFile);

            buffer = new byte[50];
            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            in.close();
            out.close();


        } else {
            System.out.println(connection.getResponseMessage());
        }
        connection.disconnect();
        return outputFile;
    }

    public byte[] sendTTS2(String input) throws IOException, JSONException
    {
        byte[] buffer = null;
        URL url = new URL("https://mono-v.mybluemix.net/tts");
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

}
