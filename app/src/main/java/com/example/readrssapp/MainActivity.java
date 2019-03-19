package com.example.readrssapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ListView feed;
    EditText regexpFilter;

    XmlPullParser parser;

    ArrayAdapter<String> adapter;

    ArrayList<String> titles;
    ArrayList<String> links;


    int items = 10, eventParser = 0;    /* Default: Showing 10 items. */
    String updateTime = "24 hours";     /* Default: Every 24 hours. */
    String urlDefault = "https://www.nasa.gov/rss/dyn/breaking_news.rss";
    String regexpString = "";


    public static final int REQUEST_SETTINGS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Preventing keyboard from distorting layout. */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        /* Declaring button that starts activity for changing User Preferences. */
        final Button preferencesButton = findViewById(R.id.btn_preferences);
        preferencesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent toPreferences = new Intent(MainActivity.this, Preferences.class);
                toPreferences.putExtra("items", items);
                toPreferences.putExtra("updateTime", updateTime);

                startActivityForResult(toPreferences, REQUEST_SETTINGS);
            }
        });


        /* Declaring button that resets the filtered feed. */
        final Button resetButton = findViewById(R.id.btn_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                regexpString = "";
                updateFeed();
            }
        });


        feed = findViewById(R.id.feedListView);
        regexpFilter = findViewById(R.id.filterEditText);

        regexpFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                    Toast.makeText(getApplicationContext(), "Enter filter to apply to feed.", Toast.LENGTH_LONG).show();
                } else {

                    regexpString = regexpFilter.getText().toString();
                    updateFeed();
                    regexpFilter.getText().clear();
                }
            }
        });


        titles = new ArrayList<>();
        links = new ArrayList<>();


        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String url = links.get(position);
                Intent openArticle = new Intent(MainActivity.this, Article.class);
                openArticle.putExtra("url", url);
                startActivity(openArticle);
            }
        });


        /* Fetch RSS data. */
        refreshStream();


        /* Convert updateTime from Preferences to seconds. */
        int waitTime = 0;
        switch (updateTime) {
            case "24 hours":
                waitTime = 86400;
                break;
            case "60 minutes":
                waitTime = 3600;
                break;
            case "10 minutes":
                waitTime = 600;
                break;
            default:
                break;
        }


        /* Wait a predetermined amount of time before feed is reset and data is fetched again, */
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                refreshStream();
            }
        }, waitTime * 1000);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_SETTINGS) {

                if (resultCode == RESULT_OK) {

                    if (data.getStringExtra("url") != null) {

                        urlDefault = data.getStringExtra("url");
                        refreshStream();
                    }

                    if (data.getStringExtra("items") != null) {

                        items = Integer.parseInt(data.getStringExtra("items"));
                        parseData(parser, eventParser, 0);
                    }

                    if (data.getStringExtra("updateTime") != null) {

                        updateTime = data.getStringExtra("updateTime");
                    }
                }
            }
        } catch (Exception ex) {
            Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    public void refreshStream() {

        new ProcessInBackground().execute();
    }


    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }


    public void parseData(XmlPullParser parser, int event, int count) throws IOException, XmlPullParserException {

        titles = new ArrayList<>();
        links = new ArrayList<>();

        boolean insideItem = false;
        while (event != XmlPullParser.END_DOCUMENT) {

            if (event == XmlPullParser.START_TAG) {

                if (parser.getName().equalsIgnoreCase("item")) {
                    insideItem = true;
                } else if (parser.getName().equalsIgnoreCase("title") && count < items) {

                    if (insideItem) {

                        titles.add(parser.nextText());
                        count++;

                    }
                } else if (parser.getName().equalsIgnoreCase("link")) {

                    if (insideItem) {

                        links.add(parser.nextText());

                    }
                }
            } else if (event == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                insideItem = false;
            }

            event = parser.next();
        }
    }


    public void updateFeed() {

        ArrayList<String> matchesList = new ArrayList<>();

        if (!regexpString.equals("")) {

            for (String s: titles) {

                if (s.toLowerCase().contains(regexpString.toLowerCase())) {
                    matchesList.add(s);
                }
            }
        } else {
            matchesList = titles;
        }


        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, matchesList);
        feed.setAdapter(adapter);
        regexpString = "";

    }


    @SuppressLint("StaticFieldLeak")
    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading RSS feed... Please wait!");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... params) {

            try {

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);

                parser = factory.newPullParser();
                parser.setInput(getInputStream(new URL(urlDefault)), "UTF_8");

                eventParser = parser.getEventType();


                parseData(parser, eventParser, 0);


            } catch (MalformedURLException e) {
                exception = e;
            } catch (XmlPullParserException e) {
                exception = e;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);

            updateFeed();

            progressDialog.dismiss();

        }
    }
}
