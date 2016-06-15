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
import com.orbotix.le.RobotLE;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Mark on 6/9/2016.
 */

public class GameStateIntro implements IGameState {

    public void Enter(GameView.GameThread game) {
    }

    public boolean Update() {
        return false;
    }

    public void Exit() {

    }

    public void Draw(Canvas c) {
        c.save();
        c.drawARGB(255, 255, 0, 0);
        c.restore();
    }

    public boolean onTouch(View v, MotionEvent e) {
        return false;
    }
}
