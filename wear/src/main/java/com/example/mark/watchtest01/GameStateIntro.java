package com.example.mark.watchtest01;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.sensor.LocatorData;
import com.orbotix.le.RobotLE;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Mark on 6/9/2016.
 */

public class GameStateIntro extends GameStateBase {

    @Override
    public void Enter(GameView.GameThread game) {
    }

    @Override
    public void ProcessLocatorData(LocatorData locDat, float targetX, float targetY) {
    }

    @Override
    public boolean Update() {
        return false;
    }

    @Override
    public void Exit() {
    }

    @Override
    public void Draw(Canvas c) {
        c.save();
        c.drawARGB(255, 255, 0, 0);
        c.restore();
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        return false;
    }
}
