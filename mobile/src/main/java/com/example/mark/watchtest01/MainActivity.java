package com.example.mark.watchtest01;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private GameView gameView = null;

    // Interface ///////////////////////////////////////////////////////////////////////////////////
    private static final int REQUEST_ENABLE_BT  = 1;

    private static MainActivity _this = null;

    public static void _finish() {
        if (_this != null) {
            _this.finish();
        }
    }

    public static void _startBluetooth() {
        if (_this != null) {
            _this.startBluetooth();
        }
    }

    public static void _registerBluetoothReceiver(BroadcastReceiver receiver) {
        if (_this != null) {
            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            _this.registerReceiver(receiver, filter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _this = this;

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        gameView = (GameView) findViewById(R.id.gameview);
    }

    @Override
    public void onStart() {
        gameView.onStart();

        super.onStart();
    }

    @Override
    protected void onStop() {
        gameView.onStop();

        super.onStop();
    }

    public void startBluetooth() {
        if (!bWantsBluetoothOn) {
            bWantsBluetoothOn = true;
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            _this.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
        }
    }

    // Implementation //////////////////////////////////////////////////////////////////////////////
    private boolean bWantsBluetoothOn = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

        if (requestCode == REQUEST_ENABLE_BT){
            if (bluetooth.isEnabled()) {
                gameView.getThread().EnterStateListDevices();
            } else {
                gameView.getThread().Fail("Bluetooth: activation failed.");
            }
        }
    }
}
