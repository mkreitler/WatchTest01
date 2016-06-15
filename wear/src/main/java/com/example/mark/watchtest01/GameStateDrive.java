package com.example.mark.watchtest01;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.mark.watchtest01.IGameState;
import com.orbotix.ConvenienceRobot;
import com.orbotix.command.RollCommand;

/**
 * Created by Mark on 6/10/2016.
 */
public class GameStateDrive implements IGameState {
    private final int heartbeat         = 330;

    private int driveCenterX            = 0;
    private int driveCenterY            = 0;
    private int driveRadius             = 0;
    private int swapCenterX             = 0;
    private int swapCenterY             = 0;
    private int swapRadius              = 0;
    private boolean bUIready            = false;
    private GameView.GameThread game    = null;

    public void Enter(GameView.GameThread gameView) {
        game = gameView;
    }

    // Update should return 'true' when the state has finished updating.
    public boolean Update() {
        boolean bExit = false;

        if (bDriving) {
            long curTimeMS = java.lang.System.currentTimeMillis();
            if (curTimeMS - driveTimeMS > heartbeat) {
                ConvenienceRobot robot = game != null ? game.getRobot() : null;
                if (robot != null) {
                    robot.drive(driveAngle, driveVel);
                }

                driveTimeMS = curTimeMS;
            }
        }

        return bExit;
    }

    public void Exit() {
    }


    public void Draw(Canvas c) {
        if (!bUIready) {
            InitUI(c);
        }

        c.save();
        c.drawARGB(255, 0, 0, 0);

        GameView._paint.setStyle(Paint.Style.FILL_AND_STROKE);
        GameView._paint.setColor(Color.GREEN);
        c.drawArc(driveCenterX - driveRadius, driveCenterY - driveRadius, driveCenterX + driveRadius, driveCenterY + driveRadius , 0, 360, true, GameView._paint);

        GameView._paint.setStyle(Paint.Style.STROKE);
        GameView._paint.setColor(Color.BLUE);

        c.drawArc(swapCenterX - swapRadius, swapCenterY - swapRadius, swapCenterX + swapRadius, swapCenterY + swapRadius, 0, 360, true, GameView._paint);

        c.restore();
    }

    public boolean onTouch(View v, MotionEvent e) {
        boolean bHandled = false;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                OnTouched(e);
                bHandled = true;
                break;
            }

            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:case MotionEvent.ACTION_MOVE: {
                OnTouchEnd(e);
                bHandled = true;
                break;
            }
        }

        return bHandled;
    }

    // Implementation //////////////////////////////////////////////////////////////////////////////
    private final int INDENT = 15;
    private float startX = 0;
    private float startY = 0;

    private float driveAngle    = 0.0f;
    private float driveVel      = 0.0f;
    private long driveTimeMS    = 0;
    private boolean bDriving    = false;

    private void OnTouchEnd(MotionEvent e) {
        ConvenienceRobot robot = game.getRobot();
        robot.stop();

        bDriving = false;

        if (WantsSwapStart() && WantsSwapEnd(startX, startY) && game != null) {
            game.enterAimState();
        }
    }

    private void OnTouched(MotionEvent e) {
        ConvenienceRobot robot = game.getRobot();

        startX = e.getX(0);
        startY = e.getY(0);

        if (WantsDrive(startX, startY)) {
            float dx = startX - driveCenterX;
            float dy = -(startY - driveCenterY);

            double rawAngle = Math.atan2((double) dx, (double) dy);
            float angleDeg = (float)(rawAngle * 180.0f / Math.PI);

            driveAngle = angleDeg;
            while (driveAngle < 0.0f) {
                driveAngle += 360.0f;
            }
            while (driveAngle > 360.0f) {
                driveAngle -= 360.0f;
            }

            driveVel = (float) Math.sqrt(dx * dx + dy * dy);
            driveVel = Math.max(1.0f, driveVel);
            bDriving = true;

            // Give greater sensitivity to lower speeds.
            driveVel *= driveVel;

            driveTimeMS = java.lang.System.currentTimeMillis();

            robot.drive(driveAngle, driveVel);
        }
    }

    private boolean WantsDrive(float x, float y) {
        float dx = driveCenterX - x;
        float dy = driveCenterY - y;

        return dx * dx + dy * dy < driveRadius * driveRadius;
    }

    private boolean WantsSwapEnd(float x, float y) {
        float dx = swapCenterX - x;
        float dy = swapCenterY - y;

        return dx * dx + dy * dy < swapRadius * swapRadius;
    }

    private boolean WantsSwapStart() {
        float dx = swapCenterX - startX;
        float dy = swapCenterY - startY;

        return dx * dx + dy * dy < swapRadius * swapRadius;
    }

    private void InitUI(Canvas c) {
        int w = c.getWidth();
        int h = c.getHeight();
        int bigDim = Math.max(w, h);

        driveCenterX = w / 2;
        driveCenterY = h / 2;
        driveRadius  = bigDim / 2 * (INDENT - 2) / INDENT;

        swapCenterX = w - bigDim / INDENT;
        swapCenterY = bigDim / INDENT;
        swapRadius  = bigDim / INDENT;

        bUIready = true;
    }
}
