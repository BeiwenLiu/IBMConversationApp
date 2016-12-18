package com.example.macbookretina.ibmconversation;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Beiwen Liu on 12/13/16.
 */
public class GoogleTts {
    private TextToSpeech mTextToSpeech;
    private String mFilePath;
    private Context mContext;

    public GoogleTts(Context context, String filePath) {
        mContext = context;
        setOutputFile(filePath);
    }

    public void setOutputFile(String outputFile) {
        mFilePath = outputFile;
    }

    public String sendTts(String input) {
        final String tempString = input;
        mTextToSpeech= new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.US);
                    File tempFile = new File(mFilePath);
                    mTextToSpeech.synthesizeToFile(tempString, Bundle.EMPTY, tempFile, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                    //t1.speak(tempString, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                }
            }
        });
        return mFilePath;
    }
}