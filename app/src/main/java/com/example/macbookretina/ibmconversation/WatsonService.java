package com.example.macbookretina.ibmconversation;

/**
 * Created by Beiwen Liu on 11/22/16.
 */

/*
    WatsonService service = WatsonService();
    demo_id = service.demoinit(); //To generate a random demo id
    service.profileinit(demo_id,"1234","1"); //You must tie this demo id with a registered profile. Use "1234" for now, as it is already registered.
    JSONObject response = service.conversation(demo_id, "hello", "1"); //This will give you the JSON response from Watson
*/

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WatsonService {

    public JSONObject conversation(String demoID, String input, String seatNumber) throws IOException, JSONException
    {
        System.setProperty("http.keepAlive", "false");
        URL url = new URL("https://mono-v.mybluemix.net/conversation");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject out = null;

        JSONObject cred   = new JSONObject();
        cred.put("text", input);
        cred.put("seat", Integer.parseInt(seatNumber));
        cred.put("demo_id", demoID);
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

    public String demoinit() throws IOException, JSONException
    {
        URL url = new URL("https://mono-v.mybluemix.net/demo/init");
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
        return out.get("demo_id").toString();
    }

    public void profileinit(String demoID, String profileID, String seatNumber) throws IOException, JSONException
    {
        URL url = new URL("https://mono-v.mybluemix.net/demo/profile");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject out = null;

        JSONObject cred   = new JSONObject();
        cred.put("demo_id",demoID);
        cred.put("profile_id", profileID);
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
        URL url = new URL("https://mono-v.mybluemix.net/demo/end");
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
}

