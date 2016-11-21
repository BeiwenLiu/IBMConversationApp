package com.example.macbookretina.ibmconversation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Beiwen Liu on 11/21/16.
 */

//To run this class:
    /*
       In MainActivity,

       //To start STT
       final Context context = this;
       GoogleSST a = new GoogleSST(context);
       a.showGoogleInputDialog()

       //To handle response
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            a.onActivityResult(requestCode, resultCode, data);
        }


     */

public class GoogleSTT extends AppCompatActivity {
    public final int SPEECH_REQUEST_CODE = 123;

    Context context;
    public GoogleSTT(Context context) {
        this.context = context;
    }

    public GoogleSTT() {
        this(null);
    }
    public void showGoogleInputDialog() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Voice recognition Demo...");
        try {
            ((Activity) context).startActivityForResult(intent, SPEECH_REQUEST_CODE);
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
                    System.out.println("done");
                }
                break;
            }

        }
    }

    public void startSomething() {
        Intent myIntent = new Intent(context, MainActivity.class);
        context.startActivity(myIntent);
    }

}
