package com.example.mark.watchtest01;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    // Interface ///////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FindPairedDevices();
        FindUnpairedDevices();
    }

    // Implementation //////////////////////////////////////////////////////////////////////////////
    private void FindPairedDevices() {

    }

    private void FindUnpairedDevices() {

    }
}
