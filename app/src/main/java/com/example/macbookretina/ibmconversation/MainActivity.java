package com.example.macbookretina.ibmconversation;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.AsyncTask;


import org.json.JSONObject;
import org.json.JSONException;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions.Builder;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.WebSocketManager;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.ArrayList;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    Button play1,stop1,record1,speech1;
    TextView speech_output;
    private AudioRecordTest recorder;
    private String outputFile = null;
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String [] permissions = {"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private SpeechToText speechService;
    public final int SPEECH_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //Window that requests permission for accessing mic and recording
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Required for permissions
        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
        speech_output = (TextView) findViewById(R.id.textView);

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.wav";

        recorder = new AudioRecordTest(outputFile);

        speechService = initSpeechToTextService();

        record1 = (Button) findViewById(R.id.button_1);
        stop1 = (Button) findViewById(R.id.button_2);
        play1 = (Button) findViewById(R.id.button_3);
        speech1 = (Button) findViewById(R.id.button_4);
        speech_output = (TextView) findViewById(R.id.textView);

        stop1.setEnabled(false);

        play1.setEnabled(false);

        speech1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute("Hello");

                play1.setEnabled(false);
                record1.setEnabled(true);
                stop1.setEnabled(true);



                Toast.makeText(getApplicationContext(), "Conversation started", Toast.LENGTH_LONG).show();
            }
        });

        record1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    recorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                play1.setEnabled(false);
                record1.setEnabled(false);
                stop1.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }
        });

        stop1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    recorder.stop();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                stop1.setEnabled(false);
                record1.setEnabled(true);
                play1.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
            }
        });

        play1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException, SecurityException, IllegalStateException {
                MediaPlayer m = new MediaPlayer();

                try {
                    m.setDataSource(outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    m.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer m) {
                        m.release();
                    }
                });
                Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                permissionToWriteAccepted  = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) MainActivity.super.finish();
        if (!permissionToWriteAccepted ) MainActivity.super.finish();

    }

    private SpeechToText initSpeechToTextService() {
        SpeechToText service = new SpeechToText();
        String username = "e8ed3836-7273-493c-b5e0-f7e1283f61d6";
        String password = "Nsg0gxPZ0k4m";
        service.setUsernameAndPassword(username, password);
        service.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
        return service;
    }

    private RecognizeOptions getRecognizeOptions() {
        Builder a = new Builder(); //Instantiating RecognizeOptions is deprecated
        a.continuous(true);
        a.contentType(MicrophoneInputStream.CONTENT_TYPE);
        a.model("en-US_BroadbandModel");
        a.interimResults(true);
        a.inactivityTimeout(2000);
        return a.build();
    }

    public void showGoogleInputDialog() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Your device is not supported!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPEECH_REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speech_output.setText(result.get(0));
                }
                break;
            }

        }
    }

    private void test() throws FileNotFoundException {

//        InputStream audio = new FileInputStream(outputFile);
//
//        SpeechToText service = new SpeechToText();
//        service.setUsernameAndPassword("e8ed3836-7273-493c-b5e0-f7e1283f61d6", "Nsg0gxPZ0k4m");
//        Builder a = new Builder();
//        a.continuous(true).interimResults(true).contentType(HttpMediaType.AUDIO_WAV);
//
//        service.recognizeUsingWebSocket(audio, a.build(), new BaseRecognizeCallback() {
//            @Override
//            public void onTranscription(SpeechResults speechResults) {
//                System.out.println(speechResults);
//            }
//        });
        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword("e8ed3836-7273-493c-b5e0-f7e1283f61d6", "Nsg0gxPZ0k4m");

        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier("audio_file",
                        "raw", getPackageName()));


        Builder a = new Builder();
        a.contentType("audio/wav");

        RecognizeCallback s = new BaseRecognizeCallback();

        service.recognizeUsingWebSocket(ins, a.build(),s);
    }

    private void testing2() {
        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword("e8ed3836-7273-493c-b5e0-f7e1283f61d6", "Nsg0gxPZ0k4m");

        File audio = new File("src/main/res/raw/audio_file.wav");
        Builder a = new Builder();
        a.contentType(HttpMediaType.AUDIO_WAV);

        SpeechResults transcript = service.recognize(audio, a.build()).execute();
        System.out.println(transcript);
    }

    private String sendRequest(String input) throws IOException, JSONException
    {
        String request = "https://mono-v.mybluemix.net/conversation";
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject cred   = new JSONObject();
        cred.put("text",input);
        cred.put("seat", 1);

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
        } else {
            System.out.println(connection.getResponseMessage());
        }
        return sb.toString();
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Fetching Watson's Response...");
            try {
                resp = sendRequest(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
             catch (Exception e) {
                 e.printStackTrace();
             }
            return resp;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation\
            speech_output.setText(resp);
            speech1.setEnabled(true);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialon

        }
        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
            speech_output.setText(text[0]);
            speech1.setEnabled(false);
        }
    }


}
