package com.example.mark.watchtest01;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.Set;
import java.util.Vector;

/**
 * Created by Mark on 6/16/2016.
 */
public class GameStateListDevices extends GameStateBase {
    private GameView.GameThread game    = null;
    private BluetoothAdapter bluetooth  = null;
    private Set<BluetoothDevice> pairedDevices;
    private Vector<String> pairedDeviceNames = new Vector<String>();
    private Vector<String> foundDeviceNames = new Vector<String>();
    private boolean bRegistered = false;
    private boolean bFoundWearable = false;

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    Log.d("SpheroApp", "ACTION_STATE_CHANGED: STATE_ON");
                }
            }

            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("SpheroApp", "ACTION_DISCOVERY_STARTED");
            }

            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("SpheroApp", "ACTION_DISCOVERY_FINISHED");
                CheckForWearable();

                /*
                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                startActivity(newIntent);
                */
            }

            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {// When discovery finds a device
                Log.d("SpheroApp", "ACTION_FOUND");

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Add the name and the MAC address of the object to the arrayAdapter
                foundDeviceNames.add(device.getName() + " -- " + device.getAddress() + "(MAC)");
            }            
        }
    };

    public void CheckForWearable() {
        for (int i=0; i<pairedDeviceNames.size(); ++i) {
            String nameCheck = pairedDeviceNames.elementAt(i).toLowerCase();
            if (nameCheck.indexOf("gear live") >= 0) {
                pairedDeviceNames.removeElementAt(i);
                bFoundWearable = true;
                break;
            }
        }

        if (!bFoundWearable) {
            game.Fail("You must pair your Gear Live before running this prototype.");
        }
        else {

        }
    }

    public void Enter(GameView.GameThread gameThread) {
        game = gameThread;

        bFoundWearable = false;

        bluetooth = BluetoothAdapter.getDefaultAdapter();

        if (bluetooth != null) {
            // List paired devices...
            pairedDevices = bluetooth.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceNames.add(device.getName() + " -- " + device.getAddress() + "(MAC)");
            }

            // ...and look for new ones.
            if (!bluetooth.isDiscovering()) {
                foundDeviceNames.clear();

                if (!bRegistered) {
                    Log.d("SpheroApp", ">>> Registering Bluetooth receiver");
                    bRegistered = true;
                    MainActivity._registerBluetoothReceiver(bReceiver);
                }

                bluetooth.startDiscovery();
            }
        }
    }

    public boolean Update() {
        if (bluetooth == null) {
            game.Fail("Bluetooth: no adapter found when listing devices.");
        }

        return false;
    }

    public void Draw(Canvas c) {
        c.drawARGB(255, 0, 0, 0);

        c.save();

        if (bFoundWearable) {
            DrawOpponentList(c);
        } else {
            DrawDeviceLists(c);
        }

        c.restore();
    }

    public GameStateListDevices(GameView.GameThread gameThread) {
        super(gameThread);
    }

    // Implementation //////////////////////////////////////////////////////////////////////////////
    private void DrawOpponentList(Canvas c) {
        GameView._paint.setStyle(Paint.Style.FILL);
        GameView._paint.setTextSize(30);
        GameView._paint.setColor(Color.WHITE);

        int y = 30;
        int ySpacing = 45;

        DrawTextCenteredAtY(c, "Choose an Opponent", y);

        GameView._paint.setColor(Color.YELLOW);
        GameView._paint.setTextSize(20);

        y += ySpacing;
        DrawTextCenteredAtY(c, "Roboponent", y);

        for (int i=0; i<pairedDeviceNames.size(); ++i) {
            y += ySpacing;
            DrawTextCenteredAtY(c, pairedDeviceNames.elementAt(i), y);
        }

        for (int i=0; i<foundDeviceNames.size(); ++i) {
            y += ySpacing;
            DrawTextCenteredAtY(c, foundDeviceNames.elementAt(i), y);
        }
    }

    private void DrawDeviceLists(Canvas c) {
        GameView._paint.setStyle(Paint.Style.FILL);
        GameView._paint.setTextSize(30);
        GameView._paint.setColor(Color.WHITE);

        int x = 20;
        int y = 30;

        c.drawText("Paired Devices", x, y, GameView._paint);
        GameView._paint.setColor(Color.GREEN);
        GameView._paint.setTextSize(20);
        for (int i=0; i<pairedDeviceNames.size(); ++i) {
            y += 32;
            c.drawText(pairedDeviceNames.elementAt(i), x, y, GameView._paint);
        }

        x += c.getWidth() / 2;
        y = 30;
        GameView._paint.setTextSize(30);
        GameView._paint.setColor(Color.WHITE);
        c.drawText("Found Devices", x, y, GameView._paint);
        GameView._paint.setTextSize(20);
        GameView._paint.setColor(Color.YELLOW);
        for (int i=0; i<foundDeviceNames.size(); ++i) {
            y += 32;
            c.drawText(foundDeviceNames.elementAt(i), x, y, GameView._paint);
        }
    }
}
