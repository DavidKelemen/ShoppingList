package org.projects.shoppinglist;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by david on 9/29/2016.
 */

public class StateChangeActivity extends Activity {
    private static final String TAG = "com.example.StateChange";
    private ArrayList<Product> items = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        ListView items = (ListView) findViewById(R.id.list);
    }
}