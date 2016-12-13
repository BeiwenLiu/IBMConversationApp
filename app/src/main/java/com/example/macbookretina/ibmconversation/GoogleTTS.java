package com.example.macbookretina.ibmconversation;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Beiwen Liu on 12/13/16.
 */
public class GoogleTTS {
    private TextToSpeech t1;
    private String filePath;
    private File tempFile;
    private Context context;


    public GoogleTTS(Context context) {
        this.context = context;
        setOutputFile(Environment.getExternalStorageDirectory() + "/google_tts.wav");
        tempFile = new File(filePath);
    }

    public void setOutputFile(String outputFile) {
        filePath = outputFile;
    }

    public String sendTTS(String input) {
        final String tempString = input;
        t1= new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                    t1.synthesizeToFile(tempString, null, tempFile, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                    t1.speak(tempString, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                }
            }
        });
        return filePath;
    }
}

