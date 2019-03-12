package com.example.readrssapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


public class Preferences extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        /* Preventing keyboard from distorting layout. */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        final EditText urlText = (EditText) findViewById(R.id.urlEditText);
        final Spinner itemsSpinner =        findViewById(R.id.spinner1items);
        final Spinner frequencySpinner =    findViewById(R.id.spinner2frequency);
        String[] items =     { "5", "10", "20", "50", "100" };         // Number of items to display in feed.
        int aMs = 600000;
        int bMs = 3600000;
        int cMs = 86400000; /* int aMin = 10, bMin = 60, cMin = 1440; */
        final String[] frequency = {"24 hours", "60 minutes", "10 minutes"}; // Frequency at which the app fetches content, stored in ms.


        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(Preferences.this,
                android.R.layout.simple_spinner_item, items);
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<String>(Preferences.this,
                android.R.layout.simple_spinner_item, frequency);

        itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemsSpinner.setAdapter(itemsAdapter);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int pos = itemsAdapter.getPosition(String.valueOf(extras.getInt("items")));
            itemsSpinner.setSelection(pos);

            pos = frequencyAdapter.getPosition(extras.getString("updateTime"));
            frequencySpinner.setSelection(pos);
        }


        final Button backButton = findViewById(R.id.buttonToHome);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent toMain = new Intent(Preferences.this, MainActivity.class);
                toMain.putExtra("items", itemsSpinner.getSelectedItem().toString());
                toMain.putExtra("updateTime", frequencySpinner.getSelectedItem().toString());

                setResult(RESULT_OK, toMain);
                finish();
            }
        });


        final Button saveAndReturn = findViewById(R.id.savePref);
        saveAndReturn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent toMain = new Intent(Preferences.this, MainActivity.class);
                toMain.putExtra("url", urlText.getText().toString().trim());
                toMain.putExtra("items", itemsSpinner.getSelectedItem().toString());
                toMain.putExtra("updateTime", frequencySpinner.getSelectedItem().toString());

                setResult(RESULT_OK, toMain);
                finish();
            }
        });


        saveAndReturn.setEnabled(false);

        // Add check to see if an URL is correctly inputted.
        urlText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().length() == 0 || !Patterns.WEB_URL.matcher(s.toString()).matches()) {

                    saveAndReturn.setEnabled(false);
                    saveAndReturn.setBackgroundColor(Color.WHITE);

                } else {

                    saveAndReturn.setEnabled(true);
                    saveAndReturn.setBackgroundColor(Color.parseColor("#00cc00"));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

}
