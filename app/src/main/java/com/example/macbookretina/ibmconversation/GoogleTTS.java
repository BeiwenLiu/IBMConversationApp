package com.example.macbookretina.ibmconversation;

import android.os.Environment;
import android.speech.tts.TextToSpeech;

import java.io.File;

/**
 * Created by Beiwen Liu on 12/13/16.
 */
public class GoogleTTS {
    TextToSpeech t1;
    String filePath;

    public GoogleTTS() {
        setOutputFile(Environment.getExternalStorageDirectory() + "/ibm_tts.wav");
    }

    public void setOutputFile(String outputFile) {
        filePath = outputFile;
    }

    public String sendTts(String input) {
        File tempFile = new File(filePath);
        t1.synthesizeToFile(input,null,tempFile,TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        return filePath;
    }
}
