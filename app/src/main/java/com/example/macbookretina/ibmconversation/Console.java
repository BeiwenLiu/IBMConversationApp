package com.example.macbookretina.ibmconversation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.nio.ByteBuffer;

import java.nio.ByteBuffer;

import com.example.macbookretina.ibmconversation.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Console extends AppCompatActivity {
    protected static Record audioInstance = null;
    protected static Thread athread = null;

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

    protected static final int MSG_PRINT = 0x100;
    protected static final int MSG_STARTSAMPLE = 0x101;
    protected static final int MSG_ENDSAMPLE = 0x102;
    protected static final int MSG_RECORDING = 0x103;
    public static TextView text = null;
    public static Button next = null;
    public static Button start = null;
    private Thread mRunSampleThread = null;

    static {
        System.loadLibrary("TrulyHandsfreeJNI"); // load native SDK lib
    }

    static void sendMsg(int id, String s) {
        Message m = new Message();
        m.what = id;
        if (s != null) {
            Bundle b = new Bundle();
            b.putString("txt", s);
            m.setData(b);
        }
        Console.myHandler.sendMessage(m);
    }

    // Called from JNI
    public void asyncPrint(String s) {
        sendMsg(Console.MSG_PRINT, s);
    }

    // Worker thread
    public void runSample() {
        sendMsg(Console.MSG_STARTSAMPLE, null);
            System.out.println("start-------------");
            File dirFiles = getFilesDir();
            for (String strFile : dirFiles.list()) {
                System.out.println(strFile);
            }
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
                        System.out.println("done");
                        break;
                    }
                }
                phrasespotClose(context);
            }
            if (audioInstance.isRecording()) audioInstance.stopRecording();
            sendMsg(Console.MSG_ENDSAMPLE, null);

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);
        text = (TextView) findViewById(R.id.TextView01);
        text.setMovementMethod(new ScrollingMovementMethod());

        audioInstance = new Record();
        athread = new Thread(audioInstance);
        athread.start();

        // Define Next button behavior
        next = (Button) findViewById(R.id.Button01);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // Define Start button behavior
        start = (Button) findViewById(R.id.Button02);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.setEnabled(false);
                if (audioInstance.isRecording()) {
                    audioInstance.stopRecording();
                    if (mRunSampleThread != null)
                        mRunSampleThread.interrupt();
                } else {
                    text.setText("");
                    text.scrollTo(0, 0);
                    mRunSampleThread = new Thread(new Runnable() {
                        public void run() {
                            runSample();
                        }
                    });
                    mRunSampleThread.start();
                }
            }
        });
    }

    final static Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Console.MSG_PRINT:
                    text.append(msg.getData().getString("txt") + "\n");
                    // scroll to end
                    final Layout layout = text.getLayout();
                    if (layout != null) {
                        int scrollDelta = layout.getLineBottom(text.getLineCount() - 1)
                                - text.getScrollY() - text.getHeight();
                        if (scrollDelta > 0)
                            text.scrollBy(0, scrollDelta);
                    }
                    break;
                case Console.MSG_RECORDING:
                    start.setEnabled(true);
                    start.setText("Stop");
                    text.append("Recording...\n");
                    break;
                case Console.MSG_STARTSAMPLE:
                    start.setEnabled(false);
                    next.setEnabled(false);
                    break;
                case Console.MSG_ENDSAMPLE:
                    start.setEnabled(true);
                    next.setEnabled(true);
                    start.setText("Run Sample");
                    break;
            }
            super.handleMessage(msg);
        }
    };

}
