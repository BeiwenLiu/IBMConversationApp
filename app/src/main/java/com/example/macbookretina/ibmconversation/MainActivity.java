package com.example.macbookretina.ibmconversation;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.EditText;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.TextView.OnEditorActionListener;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.speech.tts.TextToSpeech;

import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechModel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions.Builder;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechSession;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ArrayList;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    Button play1,stop1,record1,speech1,sttButton,ttsButton,test,reset,sttIBM,ttsGoogle,like,dislike,log,confirm;
    TextView speech_output, log_output1, log_output2, log_output3, log_output4;
    ScrollView scroll;
    ToggleButton toggle;
    RadioButton seatButton;
    EditText speech_input, comment;

    SpeechToText service;

    String textIBM;

    TextToSpeech t1;

    int width;
    private RadioGroup radioGroup;
    boolean automate;
    private APICall apiCall;
    private AudioRecordTest recorder;
    private String outputFile = null;
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String [] permissions = {"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private SpeechToText speechService;
    public final int SPEECH_REQUEST_CODE = 123;

    boolean IBMstt;
    boolean IBMtts;
    boolean Googlesst;
    boolean Googletts;

    SeekBar pitch, speed;


    String seatStringNumber = "100";

    private String demo_id;
    private String profile_id;


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

        service = initSpeechToTextService();

        automate = false;
        speech_output = (TextView) findViewById(R.id.textView);
        speech_input = (EditText) findViewById(R.id.editText);
        scroll = (ScrollView) findViewById(R.id.scrollView2);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.wav";

        recorder = new AudioRecordTest(outputFile);
        apiCall = new APICall();


        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        log_output1 = (TextView) findViewById(R.id.textView1);
        log_output2 = (TextView) findViewById(R.id.textView2);
        log_output3 = (TextView) findViewById(R.id.textView5);
        log_output4 = (TextView) findViewById(R.id.textView4);

        test = (Button) findViewById(R.id.test);
        ttsButton = (Button) findViewById(R.id.tts);
        sttButton = (Button) findViewById(R.id.stt);
//        record1 = (Button) findViewById(R.id.button_1);
//        stop1 = (Button) findViewById(R.id.button_2);
//        play1 = (Button) findViewById(R.id.button_3);
        //speech1 = (Button) findViewById(R.id.button_4);
        speech_output = (TextView) findViewById(R.id.textView);
        reset = (Button) findViewById(R.id.reset);
//        sttIBM = (Button) findViewById(R.id.sstIBM);
        ttsGoogle = (Button) findViewById(R.id.ttsGoogle);
        like = (Button) findViewById(R.id.like);
        dislike = (Button) findViewById(R.id.dislike);
        //log = (Button) findViewById(R.id.log);
        pitch = (SeekBar) findViewById(R.id.pitch);
        speed = (SeekBar) findViewById(R.id.speed);
        comment = (EditText) findViewById(R.id.comment);
        confirm = (Button) findViewById(R.id.confirm);

        AsyncTaskRunner demoinit = new AsyncTaskRunner();
        demoinit.execute("", "3");

//        log.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String[] array = log_output1.getEditableText().toString().split("\n");
//                for (int i = 0 ; i < array.length; i++) {
//                    System.out.println("Round " + i + " text: " + array[i]);
//                }
//            }
//        });

        log_output1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                log_output1.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                width = log_output1.getWidth() / 14; //height is ready
            }
        });

        //Automate Watson
        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    automate = true;
                } else {
                    automate = false;
                }
            }
        });

        //Input text
        speech_input.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(speech_input.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        //Test Button
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskRunner runner = new AsyncTaskRunner();

                if (speech_input.getText().toString().equals("")) {
                    if (!sttButton.isEnabled()) {
                        showGoogleInputDialog();
                    }
                } else {
                    runner.execute(speech_input.getText().toString(), "2", checkRadioButton());
                }

                
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String outputNumber = checkRadioButton();
                boolean flag = false;
                if (outputNumber.equals("1") && outputNumber.equals(seatStringNumber)) {
                    if (log_output1.getEditableText() != null) {
                        log_output1.getEditableText().insert(0, "\nLiked\n");

                    } else {
                        flag = true;
                    }
                } else if (outputNumber.equals("2") && outputNumber.equals(seatStringNumber)) {
                    if (log_output2.getEditableText() != null) {
                        log_output2.getEditableText().insert(0, "\n" +
                                "Liked\n");
                        like.setEnabled(false);
                        //like.setBackgroundColor(0xe5f8ff);
                        dislike.setEnabled(false);
                        //dislike.setBackgroundColor(0xe5f8ff);
                    } else {
                        flag = true;
                    }
                } else if (outputNumber.equals("3") && outputNumber.equals(seatStringNumber)) {
                    if (log_output3.getEditableText() != null) {
                        log_output3.getEditableText().insert(0, "\n" +
                                "Liked\n");
                        like.setEnabled(false);
                        //like.setBackgroundColor(0xe5f8ff);
                        dislike.setEnabled(false);
                        //dislike.setBackgroundColor(0xe5f8ff);
                    } else {
                        flag = true;
                    }
                } else if (outputNumber.equals("4") && outputNumber.equals(seatStringNumber)) {
                    if (log_output4.getEditableText() != null) {
                        log_output4.getEditableText().insert(0, "\n" +
                                "Liked\n");
                        like.setEnabled(false);
                        //like.setBackgroundColor(0xe5f8ff);
                        dislike.setEnabled(false);
                        //dislike.setBackgroundColor(0xe5f8ff);
                    } else {
                        flag = true;
                    }
                }
                if (flag || !outputNumber.equals(seatStringNumber)) {
                    Toast.makeText(getApplicationContext(), "No Conversation or wrong seat number", Toast.LENGTH_SHORT).show();
                } else {
                    like.setEnabled(false);
                    like.getBackground().setColorFilter(new LightingColorFilter(0x80ffffff, 0x80007299));
                    dislike.setEnabled(false);
                    dislike.getBackground().setColorFilter(new LightingColorFilter(0x80ffffff, 0x80007299));
                    confirm.setVisibility(View.VISIBLE);
                    comment.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Liked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String outputNumber = checkRadioButton();
                boolean flag = false;

                if (outputNumber.equals("1") && outputNumber.equals(seatStringNumber)) {
                    if (log_output1.getEditableText() != null) {
                        log_output1.getEditableText().insert(0, "\nDisliked\n");
                        like.setEnabled(false);
                        //like.setBackgroundColor(0xe5f8ff);
                        dislike.setEnabled(false);
                        //dislike.setBackgroundColor(0xe5f8ff);
                    } else {
                        flag = true;
                    }
                } else if (outputNumber.equals("2") && outputNumber.equals(seatStringNumber)) {
                    if (log_output2.getEditableText() != null) {
                        log_output2.getEditableText().insert(0, "\n" +
                                "Disliked\n");
                        like.setEnabled(false);
                        //like.setBackgroundColor(0xe5f8ff);
                        dislike.setEnabled(false);
                        //dislike.setBackgroundColor(0xe5f8ff);
                    } else {
                        flag = true;
                    }
                } else if (outputNumber.equals("3") && outputNumber.equals(seatStringNumber)) {
                    if (log_output3.getEditableText() != null) {
                        log_output3.getEditableText().insert(0, "\n" +
                                "Disliked\n");
                        like.setEnabled(false);
                        //like.setBackgroundColor(0xe5f8ff);
                        dislike.setEnabled(false);
                        //dislike.setBackgroundColor(0xe5f8ff);
                    } else {
                        flag = true;
                    }
                } else if (outputNumber.equals("3") && outputNumber.equals(seatStringNumber)) {
                    if (log_output4.getEditableText() != null) {
                        log_output4.getEditableText().insert(0, "\n" +
                                "Disliked\n");
                        like.setEnabled(false);
                        //like.setBackgroundColor(0xe5f8ff);
                        dislike.setEnabled(false);
                        //dislike.setBackgroundColor(0xe5f8ff);
                    } else {
                        flag = true;
                    }
                }

                if (flag || !outputNumber.equals(seatStringNumber)) {
                    Toast.makeText(getApplicationContext(), "No Conversation or wrong seat number", Toast.LENGTH_SHORT).show();
                } else {
                    like.setEnabled(false);
                    like.getBackground().setColorFilter(new LightingColorFilter(0x80ffffff, 0x80007299));
                    dislike.setEnabled(false);
                    dislike.getBackground().setColorFilter(new LightingColorFilter(0x80ffffff, 0x80007299));
                    confirm.setVisibility(View.VISIBLE);
                    comment.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Disliked", Toast.LENGTH_SHORT).show();
                }
            }

        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //New task
                confirm.setVisibility(View.INVISIBLE);
            }
        });

        //Reset Button
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute("", "4");
            }
        });


        //Text To Speech Google
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                    t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            // Log.d("MainActivity", "TTS finished");
                            if (!sttButton.isEnabled()) {
                                showGoogleInputDialog();
                            }
                        }
                        @Override
                        public void onError(String utteranceId) {
                        }

                        @Override
                        public void onStart(String utteranceId) {
                        }
                    });
                }
            }
        });
//
//        ttsGoogle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String toSpeak = speech_output.getText().toString();
//                Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
//                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
//            }
//        });

        //Text to Speech IBM
        ttsButton.setEnabled(true);
        ttsButton.setBackgroundColor(0x8000796b);
        ttsButton.setTextColor(0xffffffff);
        ttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ttsGoogle.isEnabled()) {
                    ttsGoogle.setEnabled(true);
                    ttsGoogle.setBackgroundColor(0x8000796b);
                    ttsButton.setEnabled(false);
                    ttsButton.setBackgroundColor(0xff00796b);
                }
            }
        });

        //Text to Speech Google
        ttsGoogle.setEnabled(false);
        ttsGoogle.setBackgroundColor(0xff00796b);
        ttsGoogle.setTextColor(0xffffffff);
        ttsGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ttsButton.isEnabled()) {
                    ttsButton.setEnabled(true);
                    ttsButton.setBackgroundColor(0x8000796b);
                    ttsGoogle.setEnabled(false);
                    ttsGoogle.setBackgroundColor(0xff00796b);
                }
            }
        });


//        //Speech to Text IBM
//        sttIBM.setEnabled(true);
//        sttIBM.setBackgroundColor(0x8000796b);
//        sttIBM.setTextColor(0xffffffff);
//        sttIBM.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //speechToTextIBM(initSpeechToTextService());
//                if (!sttButton.isEnabled()) {
//                    sttButton.setEnabled(true);
//                    sttButton.setBackgroundColor(0x8000796b);
//                    sttIBM.setEnabled(false);
//                    sttIBM.setBackgroundColor(0xff00796b);
//                }
//            }
//        });

        //Speech to Text Google
        sttButton.setEnabled(false);
        sttButton.setBackgroundColor(0xff00796b);
        sttButton.setTextColor(0xffffffff);
        sttButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sttIBM.isEnabled()) {
                    sttIBM.setEnabled(true);
                    sttIBM.setBackgroundColor(0x8000796b);
                    sttButton.setEnabled(false);
                    sttButton.setBackgroundColor(0xff00796b);
                }
                //showGoogleInputDialog();
            }
        });

    }



    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String url;
        private String resp;
        private byte[] a = null;
        private String options;
        private String input;
        private String seatNumber;
        private JSONObject ob;

        @Override
        protected String doInBackground(String... params) {
            publishProgress(params[1], "Fetching Watson's Response...");
            try {
                if (params[1].equals("0")) { //Conversation call
                    options = params[1];
                    apiCall.setURL("https://mono-v.mybluemix.net/conversation");
                    resp = apiCall.sendRequest(params[0],checkRadioButton()).get("transcription").toString();
                } else if (params[1].equals("1")) { //Text to Speech Call
                    resp = params[0];
                    options = params[1];
                    apiCall.setURL("https://mono-v.mybluemix.net/tts");
                    a = apiCall.sendTTS(params[0]);
                } else if (params[1].equals("2")) { //Test Button -> Prints out all information
                    options = params[1];
                    url = "https://mono-v.mybluemix.net/conversation";
                    apiCall.setURL(url);
                    seatNumber = checkRadioButton();
                    ob = apiCall.sendRequest(params[0], seatNumber);
                    resp = ob.get("transcription").toString();
                    System.out.println("Resp:" + resp);
                    input = params[0];
                } else if (params[1].equals("3")) {
                    options = params[1];
                    url = "https://mono-v.mybluemix.net/demo/init";
                    apiCall.setURL(url);
                    resp = apiCall.demoinit().get("demo_id").toString();
                } else if (params[1].equals("4")) {
                    options = params[1];
                    url = "https://mono-v.mybluemix.net/demo/end";
                    apiCall.setURL(url);
                    resp = apiCall.demoEnd().get("message").toString();
                } else if (params[1].equals("5")) {
                    options = params[1];
                }
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
            seatStringNumber = seatNumber;
            like.setEnabled(true);
            dislike.setEnabled(true);
            like.getBackground().clearColorFilter();
            dislike.getBackground().clearColorFilter();
            // execution of result of Long time consuming operation\
            if (options.equals("0") || options.equals("1") || options.equals("2")) {
                speech_output.setText(resp);
                //speech1.setEnabled(true);
                if (a != null) {
                    playMedia3(a);
                }
                System.out.println(automate);
                System.out.println(options);
                if (options.equals("2")) { //If 2 is called, textView2 will be populated with these values:
                    StringBuffer a = new StringBuffer();
                    StringBuffer divider = new StringBuffer();
                    a.append("\nYour Input:\n" + input + "\n");
                    a.append("\nYour Selected Seat Number:\n" + seatNumber + "\n");
                    if (resp == null) {
                        speech_output.setText("Please connect to the Internet!");
                    } else {
                        a.append("\nWatson's Response: \n" + resp + "\n");
                    }
                    a.append("\nDemo ID:\n" + demo_id + "\n");
                    if (automate) {
                        a.append("\nAutomate on - Watson will process this input:\n" + resp + "\n");
                    }

                    if (!sttButton.isEnabled()) {
                        a.append("\nGoogle Speech To Text is used\n");
                    } else if (!sttIBM.isEnabled()) {
                        a.append("\nIBM Speech To Text is used\n");
                    }

                    if (!ttsButton.isEnabled()) {
                        a.append("\nIBM Text to Speech is used\n");
                    } else if (!ttsGoogle.isEnabled()) {
                        a.append("\nGoogle Text to Speech is used\n");
                    }
                    for (int i = 0; i < width; i++) {

                        divider.append("_");
                    }
                    a.append("\n" + divider.toString() + "\n");
                    if (seatNumber.equals("1")) {
                        if (log_output1.getEditableText() == null) {
                            log_output1.append(a.toString());
                        } else {
                            log_output1.getEditableText().insert(0, a.toString());
                        }
                    } else if (seatNumber.equals("2")) {
                        if (log_output2.getEditableText() == null) {
                            log_output2.append(a.toString());
                        } else {
                            log_output2.getEditableText().insert(0, a.toString());
                        }
                    } else if (seatNumber.equals("3")) {
                        if (log_output3.getEditableText() == null) {
                            log_output3.append(a.toString());
                        } else {
                            log_output3.getEditableText().insert(0, a.toString());
                        }
                    } else if (seatNumber.equals("4")) {
                        if (log_output4.getEditableText() == null) {
                            log_output4.append(a.toString());
                        } else {
                            log_output4.getEditableText().insert(0, a.toString());
                        }
                    }


                }
                if (automate && (options.equals("0") || options.equals("2"))) { //If automated, automatically call Text to Speech at the end of conversation
                    if (!ttsGoogle.isEnabled()) {
                        GoogleTask a1 = new GoogleTask();
                        a1.execute(speech_output.getText().toString(), Integer.toString(pitch.getProgress()), Integer.toString(speed.getProgress()));
                    } else if (!ttsButton.isEnabled()) {
                        AsyncTaskRunner a = new AsyncTaskRunner();
                        a.execute(speech_output.getText().toString(), "1");
                    }
                }

//            if (automate && options.equals("1")) {
//                showGoogleInputDialog();
//            }
                speech_input.setText("");
            } else { //For cases>2
                if (options.equals("4")) {
                    log_output1.setText("");
                    log_output2.setText("");
                    log_output3.setText("");
                    log_output4.setText("");
                    speech_output.setText(resp);
                    AsyncTaskRunner a = new AsyncTaskRunner();
                    a.execute("", "3");
                } else if (options.equals("3")) {
                    demo_id = resp;
                    apiCall.setID(demo_id); //Set New ID
                } else if (options.equals("5")) {
                    if (automate) {
                        //speechToTextIBM();
                    }
                    speech_input.setText(textIBM);
                }
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog

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
            System.out.println(text[0]);
            if (text[0].equals("0") || text[0].equals("2")) {
                speech_output.setText(text[1]);
            } else if (text[0].equals("5")) {
                speech_input.setText(textIBM);
            }
            //speech1.setEnabled(false);
        }
    }

    public String checkRadioButton() {
        // Check which radio button was clicked
        String answer = null;
        int selectedId = radioGroup.getCheckedRadioButtonId();

        seatButton = (RadioButton) findViewById(selectedId);

        if (seatButton.getText().toString().equals("Seat 1")) {
            answer = "1";
        } else if (seatButton.getText().toString().equals("Seat 2")) {
            answer = "2";
        } else if (seatButton.getText().toString().equals("Seat 3")) {
            answer = "3";
        } else if (seatButton.getText().toString().equals("Seat 4")) {
            answer = "4";
        }

        return answer;
    }


    /*
    IBM Speech to Text and Text to Speech
     */
    private SpeechToText initSpeechToTextService() {
        SpeechToText service = new SpeechToText();
        String username = "cace6a1c-dbb0-4042-ab11-e5ecb9bc36da";
        String password = "eDhPlNNyr3uX";
        service.setUsernameAndPassword(username, password);
        service.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
        return service;
    }

    private RecognizeOptions getRecognizeOptions() {
        Builder a = new Builder(); //Instantiating RecognizeOptions is in another sdk
        a.continuous(false);
        a.contentType(MicrophoneInputStream.CONTENT_TYPE);
        a.model("en-US_BroadbandModel");
        a.interimResults(true);
        a.inactivityTimeout(2000);
        return a.build();
    }


     /*-----------------------------------------
    Google Speech to Text
     */

    public void showGoogleInputDialog() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,500000);
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
                    speech_input.setText(result.get(0));
                    if (automate) {
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.execute(speech_input.getText().toString(), "2", checkRadioButton());

                    }
                }
                break;
            }

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }


//    private class TranslationTask extends AsyncTask<String, String, String> {
//
//        MicrophoneInputStream input;
//
//        @Override protected String doInBackground(String... params) {
//            service.recognizeUsingWebSocket(input,
//                    getRecognizeOptions(), new BaseRecognizeCallback() {
//                        @Override
//                        public void onTranscription(SpeechResults speechResults) {
//                            textIBM = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
//                            System.out.println(textIBM);
//                            showMicText(textIBM);
//                        }
//
//                        @Override
//                        public void onError(Exception e) {
//                        }
//
//                        @Override
//                        public void onDisconnected() {
//                            System.out.println("Done");
//                            try {
//                                input.close();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                    });
//            return "";
//        }
//
//        @Override
//        protected void onPreExecute() {
//            // Things to be done before execution of long running operation. For
//            // example showing ProgessDialog
//            input = new MicrophoneInputStream();
//        }
//    }

    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                speech_input.setText(text);
            }
        });
    }



    private class GoogleTask extends AsyncTask<String, String, String> {

        @Override protected String doInBackground(String... params) {
            System.out.println("start google");
//            t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//                @Override
//                public void onInit(int status) {
//                    if(status != TextToSpeech.ERROR) {
//                        t1.setLanguage(Locale.US);
//                    }
//                }
//            });
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            t1.setPitch((float) (Integer.parseInt(params[1]) / 10.0));
            t1.setSpeechRate((float) (Integer.parseInt(params[2]) / 10.0));
            t1.speak(params[0], TextToSpeech.QUEUE_FLUSH, map);
            return "";
        };

        @Override
        protected void onPostExecute(String result) {
        }

    }

    public void playMedia3(byte[] buffer) {
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 11000, AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT,
                buffer.length*2, AudioTrack.MODE_STATIC);
        audioTrack.write(buffer, 0, buffer.length);
        audioTrack.setNotificationMarkerPosition(10 * buffer.length / 45); //Tested this number through trial and error for delay... it works the best
        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack track) {
                // nothing to do
            }

            @Override
            public void onMarkerReached(AudioTrack track) {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                if (speech_input.getText().toString().equals("") && automate) {
                    showGoogleInputDialog();
                }
            }
        });
        audioTrack.play();
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
}


