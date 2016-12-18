package com.example.macbookretina.ibmconversation;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by Beiwen Liu on 12/16/16.
 */

public class TouchCommandTest extends AppCompatActivity {

    WatsonService watsonService;
    Button lightsOn, lightsOff, doorOn, doorOff, temperature, away, home, play, stop;
    SeekBar seekBar;
    TextView log, seek;
    EditText song;

    boolean firstTime;

    String demoId = "00b866d89308247b213eb6ea90766acb";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touchcommandtest);

        firstTime = true;

        lightsOn = (Button) findViewById(R.id.lightsOn);
        lightsOff = (Button) findViewById(R.id.lightsOff);
        doorOn = (Button) findViewById(R.id.unlockDoor);
        doorOff = (Button) findViewById(R.id.lockGarageDoor);
        temperature = (Button) findViewById(R.id.temperature);
        away = (Button) findViewById(R.id.away);
        home = (Button) findViewById(R.id.home);
        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);

        seekBar = (SeekBar) findViewById(R.id.temp);
        seekBar.setOnSeekBarChangeListener(new MyListener());

        log = (TextView) findViewById(R.id.textView14);

        song = (EditText) findViewById(R.id.song);
        seek = (TextView) findViewById(R.id.seek);

        watsonService = new WatsonService();


        lightsOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute(Commands.LIGHTS_ON, "");
            }
        });

        lightsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute(Commands.LIGHTS_OFF, "");
            }
        });

        doorOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute(Commands.LOCK_DOORS, "");
            }
        });

        doorOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute(Commands.UNLOCK_DOORS, "");
            }
        });

        away.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute(Commands.AWAY, "");
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute(Commands.HOME, "");
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String temp = song.getText().toString();
                Handle handler = new Handle();
                handler.execute(Commands.PLAY_MUSIC, temp);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute(Commands.STOP_MUSIC, "");
            }
        });

        temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String progress = Integer.toString(seekBar.getProgress() + 60);
                Handle handler = new Handle();
                handler.execute(Commands.TEMPERATURE, progress);
            }
        });


    }

    private class MyListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

            //set textView's text
            seek.setText("" + (progress + 60));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

    }

    private class Handle extends AsyncTask<String, String, String> {
        String resp;
        @Override
        protected String doInBackground(String... params) {
            String tempString = params[0] + params[1];

            try {
                JSONObject temp = watsonService.conversation(demoId, tempString, "1", "Touch");
                System.out.println(temp);
                resp = temp.get("text").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        @Override
        protected void onPostExecute(String result) {
            if (firstTime) {
                log.append(resp);
                firstTime = false;
            } else {
                log.getEditableText().insert(0, resp + "\n");

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


}
