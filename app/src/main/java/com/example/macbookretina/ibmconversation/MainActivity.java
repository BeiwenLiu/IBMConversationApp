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
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
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
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions.Builder;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;

import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity {
    protected static final int MSG_PRINT = 0x100;
    protected static final int MSG_STARTSAMPLE = 0x101;
    protected static final int MSG_ENDSAMPLE = 0x102;
    protected static final int MSG_RECORDING = 0x103;
    public native void buildList(String appDir);

    public native void buildGrammar(String appDir);

    public native void buildIncremental(String appDir);

    public native void recogList(String appDir);

    public native void recogGrammar(String appDir);

    public native long phrasespotInit(String appDir);

    public native String phrasespotPipe(long p, ByteBuffer b, long rate);

    public native void phrasespotClose(long p);

    public native long recogPipeInit(String appDir);

    public native long recogPipePipe(long p, ByteBuffer b, long rate, String appDir);

    public native void recogPipeClose(long p);

    public native long recogSeqInit(String appDir);

    public native long recogSeqPhrasespotPipe(long p, ByteBuffer b, long rate, String appDir);

    public native long recogSeqGrammarPipe(long p, ByteBuffer b, long rate, String appDir);

    public native void recogSeqClose(long p);

    public native void SpeakerVerification(String appDir, Record audio);

    public native long speakerIdentificationInit(short numUsers, short numEnroll, String appDir);

    public native short speakerIdentificationPipe(long p, ByteBuffer b, long rate);

    public native short speakerIdentificationResult(long p, short idx, short numUsers,
                                                    short numEnroll, short numLoop);

    public native void speakerIdentificationClose(long p);

    public native void udtsid(String appDir, Record audio);

    public native void recogEnroll(String appDir, Record audio);

    static void sendMsg(int id, String s) {
        Message m = new Message();
        m.what = id;
        if (s != null) {
            Bundle b = new Bundle();
            b.putString("txt", s);
            m.setData(b);
        }
        MainActivity.myHandler.sendMessage(m);
    }

    static {
        System.loadLibrary("TrulyHandsfreeJNI"); // load native SDK lib
    }

    public void asyncPrint(String s) {
        sendMsg(MainActivity.MSG_PRINT, s);
    }

    protected static Record audioInstance = null;
    protected static Thread athread = null;
    private Thread mRunSampleThread = null;

    ArrayList<String> logEmail = new ArrayList();
    StringBuffer universalString = new StringBuffer();
    Map<String, String> recentLog;
    Button sttButton,ttsButton,test,reset,sttIBM,ttsGoogle,like,dislike,log,confirm,email,touch;
    static Button recordButton;
    TextView speech_output, log_output1, log_output2, log_output3, log_output4;
    ScrollView scroll;
    ToggleButton toggle,newActivity;
    RadioButton seatButton;
    EditText speech_input, comment;
    SpeechToText service;
    private static final String TAG = "MainActivity";

    String textIBM;

    TextToSpeech t1;
    boolean startThread = false;
    int width;
    private RadioGroup radioGroup;
    boolean automate;
    private APICall apiCall;
    private String outputFile = null;
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String [] permissions = {"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
    public final int SPEECH_REQUEST_CODE = 123;
    SeekBar pitch, speed;
    String seatStringNumber = "100";
    private String demo_id;

    boolean confirmed;
    String previousResponse1 = "Empty";
    String previousResponse2 = "Empty";
    String previousResponse3 = "Empty";
    String previousResponse4 = "Empty";
    SimpleDateFormat isoFormat;
    boolean liked;
    boolean multiThreadCheck; //If enabled, will send request across all seats

    final Context context = this;



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

        AssetCache assets = new AssetCache(this, "");
        try {
            for (String file : new String[] { "nn_en_us_mfcc_16k_15_250_v5.1.1.raw",
                    "lts_en_us_9.5.2b.raw", "names.txt", "john_smith_16khz.wav",
                    "twelve_oclock_16khz.audio", "hbg_antiData_v6_0.raw", "hbg_genderModel.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_am.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_3.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_5.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_7.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_9.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_11.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_13.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_15.raw",
                    "phonemeSearch_1_5.raw", "svsid_1_1.raw",
                    "HBG_enUS_taskData_v2_2.raw", "UDT_enUS_taskData_v12_0_5.raw"
            }) {
                Log.i(TAG, "caching " + assets.getPath(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("GMT-4:00"));

        service = initSpeechToTextService();

        automate = false;
        speech_output = (TextView) findViewById(R.id.textView);
        speech_input = (EditText) findViewById(R.id.editText);
        scroll = (ScrollView) findViewById(R.id.scrollView2);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.wav";

        apiCall = new APICall();

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        log_output1 = (TextView) findViewById(R.id.textView1);
        log_output2 = (TextView) findViewById(R.id.textView2);
        log_output3 = (TextView) findViewById(R.id.textView5);
        log_output4 = (TextView) findViewById(R.id.textView4);

        test = (Button) findViewById(R.id.test);
        ttsButton = (Button) findViewById(R.id.tts);
        sttButton = (Button) findViewById(R.id.stt);
        speech_output = (TextView) findViewById(R.id.textView);
        reset = (Button) findViewById(R.id.reset);
        ttsGoogle = (Button) findViewById(R.id.ttsGoogle);
        like = (Button) findViewById(R.id.like);
        dislike = (Button) findViewById(R.id.dislike);
        pitch = (SeekBar) findViewById(R.id.pitch);
        speed = (SeekBar) findViewById(R.id.speed);
        comment = (EditText) findViewById(R.id.comment);
        confirm = (Button) findViewById(R.id.confirm);
        email = (Button) findViewById(R.id.email);
        newActivity = (ToggleButton) findViewById(R.id.newActivity);
        recordButton = (Button) findViewById(R.id.startRecord);

        touch = (Button) findViewById(R.id.touch);

        confirmed = true;

        recentLog = new HashMap();
        recentLog.put("device_identifier", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        AsyncTaskRunner demoinit = new AsyncTaskRunner();
        demoinit.execute("", "3");


        //Use for testing sensory
//        newActivity.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), Console.class);
//                intent.putExtra(EXTRA_MESSAGE, "hey");
//                startActivityForResult(intent, 0);
//            }
//        });

//        newActivity.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                speech_output.setText("");
//                new ParallelTask1().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                new ParallelTask2().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                new ParallelTask3().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                new ParallelTask4().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//        });

        touch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(v.getContext(), TouchTest.class);
                //startActivityForResult(intent, 0);

                GoogleTTS n = new GoogleTTS(context);
                String path = n.sendTTS("How are you");
                //System.out.println(path);
            }
        });


        newActivity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    multiThreadCheck = true;
                } else {
                    multiThreadCheck = false;
                }
            }
        });

        //Activate sensory keyword detection
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!startThread) {
                    audioInstance = new Record();
                    athread = new Thread(audioInstance);
                    athread.start();
                    startThread = true;
                }
                view.setEnabled(false);
                if (audioInstance.isRecording()) {
                    audioInstance.stopRecording();
                    if (mRunSampleThread != null)
                        mRunSampleThread.interrupt();
                } else {
                    mRunSampleThread = new Thread(new Runnable() {
                        public void run() {
                            runSample();
                        }
                    });
                    mRunSampleThread.start();
                }
            }
        });


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
                if (confirmed) {
                    AsyncTaskRunner runner = new AsyncTaskRunner();

                    if (speech_input.getText().toString().equals("")) {
                        if (!sttButton.isEnabled()) {
                            showGoogleInputDialog();
                        }
                    } else {
                        if (multiThreadCheck) {
                            speech_output.setText("Finish parallel pasks in this order: ");
                            String tempInput = speech_input.getText().toString();
                            new ParallelTask1().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempInput);
                            new ParallelTask2().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempInput);
                            new ParallelTask3().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempInput);
                            new ParallelTask4().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempInput);
                        } else {
                            runner.execute(speech_input.getText().toString(), "2", checkRadioButton());
                        }

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please confirm before proceeding!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Like will automatically populate log_output1 with a like
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
                    liked = true;
                    confirmed = false;
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

        //Dislike will automatically populate log_output1 with dislike
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
                } else if (outputNumber.equals("4") && outputNumber.equals(seatStringNumber)) {
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
                    liked = false;
                    confirmed = false;
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

        //Executes new Async task that communicates with logging API
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //New task

                universalString.append("\n" + comment.getText().toString() + "\n");
                recentLog.put("remarks", comment.getText().toString());
                recentLog.put("miscellaneous", null);
                if (liked) {
                    universalString.append("\n" + "liked: true" + "\n");
                    recentLog.put("liked","true");
                } else {
                    universalString.append("\n" + "liked: false" + "\n");
                    recentLog.put("liked","false");
                }
                LogTask runner = new LogTask();
                runner.execute();
            }
        });

        //Reset Button
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmed) {
                    AsyncTaskRunner runner = new AsyncTaskRunner();
                    runner.execute("", "4");
                } else {
                    Toast.makeText(getApplicationContext(), "Please confirm before proceeding!", Toast.LENGTH_SHORT).show();
                }
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
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

    }



    // Main API handler
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
                universalString.append("____________________________________");
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
                    System.out.println("Object : " + ob);
                    resp = ob.get("text").toString();
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

                    String currentDateandTime = isoFormat.format(new Date());
                    String[] dates = currentDateandTime.split(" ");
                    System.out.println(currentDateandTime);
                    StringBuffer a = new StringBuffer();
                    StringBuffer divider = new StringBuffer();
                    universalString.append("\nYour Input:\n" + input + "\n");
                    universalString.append("\nYour Selected Seat Number:\n" + seatNumber + "\n");
                    a.append("\nYour Input:\n" + input + "\n");
                    a.append("\nYour Selected Seat Number:\n" + seatNumber + "\n");
                    if (resp == null) {
                        speech_output.setText("Please connect to the Internet!");
                    } else {
                        universalString.append("\nWatson's Response: \n" + resp + "\n");
                        universalString.append("\nWatson's Raw Response: \n" + ob.toString() + "\n");
                        a.append("\nWatson's Response: \n" + resp + "\n");
                        a.append("\nWatson's Raw Response: \n" + ob.toString() + "\n");
                    }
                    universalString.append("\nDemo ID:\n" + demo_id + "\n");
                    a.append("\nDemo ID:\n" + demo_id + "\n");
                    if (automate) {
                        a.append("\nAutomate on - Watson will process this input:\n" + resp + "\n");
                    }

                    if (!sttButton.isEnabled()) {
                        universalString.append("\nGoogle Speech To Text is used\n");
                        a.append("\nGoogle Speech To Text is used\n");
                    } else if (!sttIBM.isEnabled()) {
                        universalString.append("\nIBM Speech To Text is used\n");
                        a.append("\nIBM Speech To Text is used\n");
                    }

                    if (!ttsButton.isEnabled()) {
                        universalString.append("\nIBM Text to Speech is used\n");
                        a.append("\nIBM Text to Speech is used\n");
                    } else if (!ttsGoogle.isEnabled()) {
                        universalString.append("\n" + "Google Text to Speech is used\n");
                        a.append("\nGoogle Text to Speech is used\n");
                    }
                    for (int i = 0; i < width; i++) {

                        divider.append("_");
                    }
                    a.append("\n" + divider.toString() + "\n");
                    recentLog.put("user_response", input);
                    recentLog.put("ending_watson_response", resp);
                    universalString.append("\n" + dates[0] + "\n");
                    universalString.append("\n" + dates[1] + "\n");
                    recentLog.put("date", dates[0]);
                    recentLog.put("time", dates[1]);


                    if (seatNumber.equals("1")) {
                        recentLog.put("starting_watson_response", previousResponse1);
                        previousResponse1 = resp;
                        if (log_output1.getEditableText() == null) {
                            log_output1.append(a.toString());
                        } else {
                            log_output1.getEditableText().insert(0, a.toString());
                        }
                    } else if (seatNumber.equals("2")) {
                        recentLog.put("starting_watson_response", previousResponse2);
                        previousResponse2 = resp;
                        if (log_output2.getEditableText() == null) {
                            log_output2.append(a.toString());
                        } else {
                            log_output2.getEditableText().insert(0, a.toString());
                        }
                    } else if (seatNumber.equals("3")) {
                        recentLog.put("starting_watson_response", previousResponse3);
                        previousResponse3 = resp;
                        if (log_output3.getEditableText() == null) {
                            log_output3.append(a.toString());
                        } else {
                            log_output3.getEditableText().insert(0, a.toString());
                        }
                    } else if (seatNumber.equals("4")) {
                        recentLog.put("starting_watson_response", previousResponse4);
                        previousResponse4 = resp;
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
                    speech_output.setText("Demo init success: " + apiCall.getID());

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
            } else if (text[0].equals("3")) {
                speech_output.setText("Initializing demo ID... Please wait");
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Voice recognition Demo...");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(10000));
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
                        if (multiThreadCheck) {
                            speech_output.setText("Finish parallel pasks in this order: ");
                            String tempInput = speech_input.getText().toString();
                            new ParallelTask1().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempInput);
                            new ParallelTask2().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempInput);
                            new ParallelTask3().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempInput);
                            new ParallelTask4().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempInput);
                        } else {
                            runner.execute(speech_input.getText().toString(), "2", checkRadioButton());
                        }

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


    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                speech_input.setText(text);
            }
        });
    }



    //Google text to speech
    private class GoogleTask extends AsyncTask<String, String, String> {

        @Override protected String doInBackground(String... params) {
            System.out.println("start google");
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

    //Playback audio
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

    private class LogTask extends AsyncTask<String, String, String> {
        String resp;
        @Override
        protected String doInBackground(String... params) {
            apiCall.setURL("https://mono-v-feedback.mybluemix.net/feedback");
            try {
                resp = apiCall.feedback(recentLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        };

        @Override
        protected void onPostExecute(String result) {
            if (resp != null) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Not successful", Toast.LENGTH_SHORT).show();
            }
            confirmed = true;
            comment.setText("");
            confirm.setVisibility(View.INVISIBLE);
            comment.setVisibility(View.INVISIBLE);
        }
    }

    public void sendEmail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"beiwenl@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Log");
        i.putExtra(Intent.EXTRA_TEXT   , universalString.toString());
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
        }
    }


    public void sendMessage(View view) {
        Intent intent = new Intent(this, Console.class);
        String message = "hi";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    //This function handles the sensory keyword activation
    public void runSample() {
        File dirFiles = getFilesDir();
        String directory = getFilesDir().toString() + "/";
        long context = phrasespotInit(directory);

        boolean audioStarted = false;
        if (context != 0) {
            audioInstance.startRecording();
            while (audioInstance.isRecording()) {
                ByteBuffer buf = null;
                try {
                    buf = audioInstance.getBuffer();
                    if (!audioStarted) {
                        sendMsg(Console.MSG_RECORDING, null);
                        audioStarted = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // WorkerThread can be interrupted.
                    // throw new
                    // IllegalStateException("WorkerThread: getBuffer failed");
                }
                if (phrasespotPipe(context, buf, audioInstance.samplerate) != null) {
                    audioInstance.stopRecording(); // success
                    showGoogleInputDialog();
                    System.out.println("done");
                    break;
                }
            }
            phrasespotClose(context);
        }
        if (audioInstance.isRecording()) audioInstance.stopRecording();
        sendMsg(Console.MSG_ENDSAMPLE, null);

    }

    //This function handles the GUI
    final static Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainActivity.MSG_PRINT:
                    break;
                case MainActivity.MSG_RECORDING:
                    recordButton.setEnabled(true);
                    recordButton.setText("Stop");
                    break;
                case MainActivity.MSG_STARTSAMPLE:
                    recordButton.setEnabled(false);
                    break;
                case MainActivity.MSG_ENDSAMPLE:
                    recordButton.setEnabled(true);
                    recordButton.setText("Start Keyword Detection");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private class ParallelTask1 extends AsyncTask<String, String, String> {
        String resp;
        String url;
        JSONObject ob;
        String input;
        @Override
        protected String doInBackground(String... params) {
            url = "https://mono-v.mybluemix.net/conversation";
            input = params[0];
            apiCall.setURL(url);
            try {
                ob = apiCall.sendRequest(params[0], "1");
                resp = ob.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Object : " + ob);
            System.out.println("Resp:" + resp);
            return null;
        };

        @Override
        protected void onPostExecute(String result) {
            StringBuffer a = new StringBuffer();
            a.append("\n");
            a.append("Your Input: " + input);
            a.append("\n");
            a.append("\nWatson Raw Response\n");
            a.append(resp);
            a.append("\n");
            for (int i = 0; i < width; i++) {

                a.append("_");
            }
            a.append("\n");
            if (log_output1.getEditableText() == null) {
                log_output1.append(a.toString());
            } else {
                log_output1.getEditableText().insert(0, a.toString());
            }
            speech_output.append("Task1 ");
            System.out.println("done1");
            if (automate) { //If automated, automatically call Text to Speech at the end of conversation
                if (!ttsGoogle.isEnabled()) {
                    GoogleTask a1 = new GoogleTask();
                    a1.execute(speech_output.getText().toString(), Integer.toString(pitch.getProgress()), Integer.toString(speed.getProgress()));
                } else if (!ttsButton.isEnabled()) {
                    AsyncTaskRunner a2 = new AsyncTaskRunner();
                    a2.execute(speech_output.getText().toString(), "1");
                }
            }
        }
    }

    private class ParallelTask2 extends AsyncTask<String, String, String> {
        String resp;
        String url;
        JSONObject ob;
        String input;
        @Override
        protected String doInBackground(String... params) {
            url = "https://mono-v.mybluemix.net/conversation";
            apiCall.setURL(url);
            input = params[0];
            try {
                ob = apiCall.sendRequest(params[0], "2");
                resp = ob.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Object : " + ob);
            System.out.println("Resp:" + resp);
            return null;
        };

        @Override
        protected void onPostExecute(String result) {
            StringBuffer a = new StringBuffer();
            a.append("\n");
            a.append("Your Input: " + input);
            a.append("\n");
            a.append("\nWatson Raw Response\n");
            a.append(resp);
            a.append("\n");
            for (int i = 0; i < width; i++) {

                a.append("_");
            }
            a.append("\n");
            if (log_output2.getEditableText() == null) {
                log_output2.append(a.toString());
            } else {
                log_output2.getEditableText().insert(0, a.toString());
            }
            speech_output.append("Task2 ");
            System.out.println("done2");
            if (automate) { //If automated, automatically call Text to Speech at the end of conversation
                if (!ttsGoogle.isEnabled()) {
                    GoogleTask a1 = new GoogleTask();
                    a1.execute(speech_output.getText().toString(), Integer.toString(pitch.getProgress()), Integer.toString(speed.getProgress()));
                } else if (!ttsButton.isEnabled()) {
                    AsyncTaskRunner a2 = new AsyncTaskRunner();
                    a2.execute(speech_output.getText().toString(), "1");
                }
            }
        }
    }

    private class ParallelTask3 extends AsyncTask<String, String, String> {
        String resp;
        String url;
        JSONObject ob;
        String input;
        @Override
        protected String doInBackground(String... params) {
            url = "https://mono-v.mybluemix.net/conversation";
            apiCall.setURL(url);
            input = params[0];
            try {
                ob = apiCall.sendRequest(params[0], "3");
                resp = ob.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Object : " + ob);
            System.out.println("Resp:" + resp);
            return null;
        };

        @Override
        protected void onPostExecute(String result) {
            StringBuffer a = new StringBuffer();
            a.append("\n");
            a.append("Your Input: " + input);
            a.append("\n");
            a.append("\nWatson Raw Response\n");
            a.append(resp);
            a.append("\n");
            for (int i = 0; i < width; i++) {

                a.append("_");
            }
            a.append("\n");
            if (log_output3.getEditableText() == null) {
                log_output3.append(a.toString());
            } else {
                log_output3.getEditableText().insert(0, a.toString());
            }
            speech_output.append("Task3 ");
            System.out.println("done3");
            if (automate) { //If automated, automatically call Text to Speech at the end of conversation
                if (!ttsGoogle.isEnabled()) {
                    GoogleTask a1 = new GoogleTask();
                    a1.execute(speech_output.getText().toString(), Integer.toString(pitch.getProgress()), Integer.toString(speed.getProgress()));
                } else if (!ttsButton.isEnabled()) {
                    AsyncTaskRunner a2 = new AsyncTaskRunner();
                    a2.execute(speech_output.getText().toString(), "1");
                }
            }
        }
    }

    private class ParallelTask4 extends AsyncTask<String, String, String> {
        String resp;
        String url;
        JSONObject ob;
        String input;
        @Override
        protected String doInBackground(String... params) {
            url = "https://mono-v.mybluemix.net/conversation";
            apiCall.setURL(url);
            input = params[0];
            try {
                ob = apiCall.sendRequest(params[0], "4");
                resp = ob.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Object : " + ob);
            System.out.println("Resp:" + resp);
            return null;
        };

        @Override
        protected void onPostExecute(String result) {
            StringBuffer a = new StringBuffer();
            a.append("\n");
            a.append("Your Input: " + input);
            a.append("\n");
            a.append("\nWatson Raw Response\n");
            a.append(resp);
            a.append("\n");

            for (int i = 0; i < width; i++) {

                a.append("_");
            }
            a.append("\n");
            if (log_output4.getEditableText() == null) {
                log_output4.append(a.toString());
            } else {
                log_output4.getEditableText().insert(0, a.toString());
            }
            speech_output.append("Task4 ");
            System.out.println("done4");

            if (automate) { //If automated, automatically call Text to Speech at the end of conversation
                if (!ttsGoogle.isEnabled()) {
                    GoogleTask a1 = new GoogleTask();
                    a1.execute(speech_output.getText().toString(), Integer.toString(pitch.getProgress()), Integer.toString(speed.getProgress()));
                } else if (!ttsButton.isEnabled()) {
                    AsyncTaskRunner a2 = new AsyncTaskRunner();
                    a2.execute(speech_output.getText().toString(), "1");
                }
            }
        }
    }

}


