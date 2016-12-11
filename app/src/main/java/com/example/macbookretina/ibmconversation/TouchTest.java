package com.example.macbookretina.ibmconversation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by MacbookRetina on 12/10/16.
 */
public class TouchTest extends AppCompatActivity {
    Button coffee,latte,espresso,tea,icetea,frappe;
    RadioGroup coffeegroup, lattegroup,espressogroup, teagroup, iceteagroup, frappegroup;
    TextView view;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);


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

        view = (TextView) findViewById(R.id.view);

        coffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("hey");
            }
        });

    }
}
