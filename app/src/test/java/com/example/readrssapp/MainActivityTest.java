package com.example.readrssapp;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;


public class MainActivityTest extends Activity {

    @Test
    public void updateFeed() {

        ListView feed = new ListView(MainActivityTest.this);
        ArrayList<String> matchesList = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        String filterString = "Some type of filter.";

        if (!filterString.equals("")) {

            for (String s: titles) {

                if (s.toLowerCase().contains(filterString.toLowerCase())) {
                    matchesList.add(s);
                }
            }
        } else {
            matchesList = titles;
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, matchesList);
        feed.setAdapter(adapter);

    }


    @Test
    public void getInputStream() throws Exception {

        URL url = new URL("https://www.thisIsATestURL.com");
        url.openConnection().getInputStream();

    }

}