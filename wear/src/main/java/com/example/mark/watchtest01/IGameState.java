package com.example.mark.watchtest01;

/**
 * Created by Mark on 6/9/2016.
 */
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.common.sensor.DeviceSensorsData;
import com.orbotix.common.sensor.LocatorData;

public interface IGameState {
    public void Enter(GameView.GameThread game);

    // Update should return 'true' when the state has finished updating.
    public boolean Update();

    public void Exit();
    public void Draw(Canvas c);
    public boolean onTouch(View v, MotionEvent e);

    public void ProcessCollision(CollisionDetectedAsyncData colData);
    public void ProcessLocatorData(LocatorData locDat, float targetX, float targetY);
    public void ProcessSensorData(DeviceSensorsData sensorData);
}
