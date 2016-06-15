package com.example.mark.watchtest01;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.RobotLE;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Mark on 6/9/2016.
 */

public class GameStateConnect implements IGameState {
    private RobotChangedStateListener mRobotListener    = null;
    private Context context                             = null;
    private boolean bFoundOrTimedOut                    = false;

    public GameStateConnect(RobotChangedStateListener robotStateListener, Context appContext) {
        mRobotListener = robotStateListener;
        context = appContext;
    }

    public void Enter(GameView.GameThread game) {
        if (mRobotListener != null) {
            DualStackDiscoveryAgent.getInstance().addRobotStateListener(mRobotListener);

            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery(context);
            }
            catch (Exception e) {
                Log.e("Sphero", "Failed to start discovery.");
            }
        }
    }

    public boolean Update() {
        return bFoundOrTimedOut;
    }

    public void Exit() {
        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }
    }

    public void Draw(Canvas c) {
        c.save();
        c.drawARGB(255, 255, 255, 0);
        c.restore();
    }

    public boolean onTouch(View v, MotionEvent e) {
        return false;
    }
}
