package com.example.mark.watchtest01;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.common.sensor.DeviceSensorsData;
import com.orbotix.common.sensor.LocatorData;

/**
 * Created by Mark on 6/15/2016.
 */
public class GameStateBase implements IGameState {
    public void Enter(GameView.GameThread game) {}
    public boolean Update() {return true;}
    public void Exit() {}
    public void Draw(Canvas c) {}
    public boolean onTouch(View v, MotionEvent e) {return false;}
    public void ProcessCollision(CollisionDetectedAsyncData colData) {}
    public void ProcessLocatorData(LocatorData locDat, float targetX, float targetY) {}
    public void ProcessSensorData(DeviceSensorsData sensorData) {}
}
