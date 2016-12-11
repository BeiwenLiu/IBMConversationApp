package com.example.macbookretina.ibmconversation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by MacbookRetina on 12/10/16.
 */
public class TouchTest extends AppCompatActivity {
    Button coffee,latte,espresso,tea,icetea,frappe,hamburger,chickennuggets,icecream,fries,cookies, softdrink;
    RadioGroup coffeegroup, lattegroup,espressogroup, teagroup, iceteagroup, frappegroup;

    RadioGroup softdrinkgroup, friesgroup, icecreamgroup;
    TextView view;
    TouchHandler watson;
    HashMap<String, String> map;
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


        view = (TextView) findViewById(R.id.brewView);

        map = new HashMap();

        watson = new TouchHandler();


        coffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handle handler = new Handle();
                handler.execute();
            }
        });

        latte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(latte.getText().toString());
            }
        });

        espresso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(espresso.getText().toString());
            }
        });

        tea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(tea.getText().toString());
            }
        });

        icetea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(icetea.getText().toString());
            }
        });

        frappe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(frappe.getText().toString());
            }
        });

        icecream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(icecream.getText().toString());
            }
        });

        cookies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(cookies.getText().toString());
            }
        });

        fries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(fries.getText().toString());
            }
        });

        hamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(hamburger.getText().toString());
            }
        });

        chickennuggets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(chickennuggets.getText().toString());
            }
        });

        softdrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(softdrink.getText().toString());
            }
        });

    }

    private class Handle extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            watson.handleCommand("Midtown Brew", "Hello", "Whats");
            return null;
        };

        @Override
        protected void onPostExecute(String result) {
            System.out.println("Done");
        }
    }
}
