package com.example.macbookretina.ibmconversation;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by MacbookRetina on 12/10/16.
 */
public class TouchTest extends AppCompatActivity {
    Button coffee,latte,espresso,tea,icetea,frappe,hamburger,chickennuggets,icecream,fries,cookies, softdrink, brewConfirm, cafeConfirm,watsonButton;
    RadioGroup coffeegroup, lattegroup,espressogroup, teagroup, iceteagroup, frappegroup;

    RadioGroup softdrinkgroup, friesgroup, icecreamgroup;
    TextView brewview, cafeview,watsonView,overallView;
    TouchHandler watson;
    HashMap<String, String> map;

    WatsonService watsonService;
    public final int SPEECH_REQUEST_CODE = 123;
    String id = "00b866d89308247b213eb6ea90766acb";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touchtest);


        coffee = (Button) findViewById(R.id.coffee);
        latte = (Button) findViewById(R.id.latte);
        espresso = (Button) findViewById(R.id.espresso);
        tea = (Button) findViewById(R.id.tea);
        icetea = (Button) findViewById(R.id.icetea);
        frappe = (Button) findViewById(R.id.frappe);

        coffeegroup = (RadioGroup) findViewById(R.id.coffeegroup);
        lattegroup = (RadioGroup) findViewById(R.id.lattegroup);
        espressogroup = (RadioGroup) findViewById(R.id.espressogroup);
        teagroup = (RadioGroup) findViewById(R.id.teagroup);
        iceteagroup = (RadioGroup) findViewById(R.id.iceteagroup);
        frappegroup = (RadioGroup) findViewById(R.id.frappegroup);

        icecreamgroup = (RadioGroup) findViewById(R.id.icecreamgroup);
        friesgroup = (RadioGroup) findViewById(R.id.friesgroup);
        softdrinkgroup = (RadioGroup) findViewById(R.id.softdrinkgroup);

        fries = (Button) findViewById(R.id.fries);
        hamburger = (Button) findViewById(R.id.hamburger);
        chickennuggets = (Button) findViewById(R.id.chickennuggets);
        cookies = (Button) findViewById(R.id.cookies);
        icecream = (Button) findViewById(R.id.icecream);
        softdrink = (Button) findViewById(R.id.softdrink);


        brewview = (TextView) findViewById(R.id.brewView);
        cafeview = (TextView) findViewById(R.id.cafeView);
        watsonView = (TextView) findViewById(R.id.watsonView);
        overallView = (TextView) findViewById(R.id.overallView);
        brewConfirm = (Button) findViewById(R.id.brewConfirm);
        cafeConfirm = (Button) findViewById(R.id.cafeconfirm);

        watsonButton = (Button) findViewById(R.id.watsonButton);

        final Context context = this;
        map = new HashMap();

        watson = new TouchHandler();
        watsonService = new WatsonService();


        coffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = coffeegroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Brew", coffee.getText().toString(),temp.getText().toString());
                System.out.println(temp.getText().toString());
            }
        });

        latte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = lattegroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Brew", latte.getText().toString(),temp.getText().toString());
                System.out.println(temp.getText().toString());
            }
        });

        espresso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = espressogroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Brew", espresso.getText().toString(),temp.getText().toString());
                System.out.println(temp.getText().toString());
            }
        });

        tea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = teagroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Brew", tea.getText().toString(),temp.getText().toString());
                System.out.println(temp.getText().toString());
            }
        });

        icetea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = iceteagroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Brew", icetea.getText().toString(),temp.getText().toString());
                System.out.println(temp.getText().toString());
            }
        });

        frappe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = frappegroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Brew", frappe.getText().toString(),temp.getText().toString());
                System.out.println(temp.getText().toString());            }
        });

        icecream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = icecreamgroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Cafe", icecream.getText().toString(), temp.getText().toString());
                System.out.println(temp.getText().toString());
            }
        });

        cookies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute("Midtown Cafe", cookies.getText().toString(), "");
            }
        });

        fries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = friesgroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Cafe", fries.getText().toString(), temp.getText().toString());
                System.out.println(temp.getText().toString());
            }
        });

        hamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute("Midtown Cafe", hamburger.getText().toString(), "");
            }
        });

        chickennuggets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute("Midtown Cafe", chickennuggets.getText().toString(), "");
            }
        });

        softdrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                int selectedId = softdrinkgroup.getCheckedRadioButtonId();

                RadioButton temp = (RadioButton) findViewById(selectedId);
                handler.execute("Midtown Cafe", softdrink.getText().toString(), temp.getText().toString());
                System.out.println(temp.getText().toString());
            }
        });

        brewConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute("", brewConfirm.getText().toString(), "");
            }
        });

        cafeConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute("", cafeConfirm.getText().toString(), "");
            }
        });

        watsonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GoogleSTT a = new GoogleSTT(context);
                a.showGoogleInputDialog();
            }
        });

    }

    private class Handle extends AsyncTask<String, String, String> {
        String resp;
        String whichView;
        String cafe;
        String brew;
        @Override
        protected String doInBackground(String... params) {
            resp = watson.handleCommand(params[0], params[1], params[2]);
            whichView = params[0];
            if (whichView.equals("Midtown Cafe")) {
                cafe = params[2] + " " + params[1];
            } else if (whichView.equals("Midtown Brew")) {
                brew = params[2] + " " + params[1];
            }
            return null;
        };

        @Override
        protected void onPostExecute(String result) {
            if (whichView.equals("Midtown Brew")) {
                brewview.append(brew);
                brewview.append("\n");
            } else if (whichView.equals("Midtown Cafe")) {
                cafeview.append(cafe);
                cafeview.append("\n");
            }

            overallView.append(resp);
            overallView.append("\n");

        }
    }

    private class Watson extends AsyncTask<String, String, String> {
        String resp;
        String whichView;
        String cafe;
        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject temp = watsonService.conversation(id, params[0], "1", "Speech");
                resp = temp.get("text").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        @Override
        protected void onPostExecute(String result) {
            watsonView.setText(resp);
            overallView.append(resp);
            overallView.append("\n");
        }
    }

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
                    Watson handler = new Watson();
                    handler.execute(result.get(0));
                }
                break;
            }

        }
    }
}
