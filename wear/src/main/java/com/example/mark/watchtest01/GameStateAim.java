package com.example.mark.watchtest01;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.mark.watchtest01.IGameState;
import com.orbotix.ConvenienceRobot;
import com.orbotix.common.sensor.LocatorData;

/**
 * Created by Mark on 6/10/2016.
 */
public class GameStateAim extends GameStateBase {
    private int aimCenterX              = 0;
    private int aimCenterY              = 0;
    private int aimRadius               = 0;
    private int swapCenterX             = 0;
    private int swapCenterY             = 0;
    private int swapRadius              = 0;
    private boolean bUIready            = false;
    private boolean bTouched            = false;
    private GameView.GameThread game    = null;

    @Override
    public void ProcessLocatorData(LocatorData locDat, float targetX, float targetY) {
        // Nothing to process in the 'Aim' state.
    }

    @Override
    public void Enter(GameView.GameThread gameView) {
        game = gameView;

        ConvenienceRobot robot = gameView != null ? gameView.getRobot() : null;
        if (robot != null) {
            robot.calibrating(true);
        }
    }

    // Update should return 'true' when the state has finished updating.
    @Override
    public boolean Update() {
        boolean bExit = false;

        if (bTouched) {
            OnTouchMove();
        }

        return bExit;
    }

    @Override
    public void Exit() {
        ConvenienceRobot robot = game != null ? game.getRobot() : null;
        if (robot != null) {
            robot.setZeroHeading();
            robot.calibrating(false);
        }
    }

    @Override
    public void Draw(Canvas c) {
        if (!bUIready) {
            InitUI(c);
        }

        c.save();
        c.drawARGB(255, 0, 0, 0);

        GameView._paint.setStyle(Paint.Style.STROKE);
        GameView._paint.setColor(Color.BLUE);
        c.drawLine(aimCenterX - aimRadius, aimCenterY, aimCenterX + aimRadius, aimCenterY, GameView._paint);

        GameView._paint.setStyle(Paint.Style.FILL_AND_STROKE);
        GameView._paint.setColor(Color.GREEN);

        c.drawArc(swapCenterX - swapRadius, swapCenterY - swapRadius, swapCenterX + swapRadius, swapCenterY + swapRadius, 0, 360, true, GameView._paint);

        c.restore();
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        boolean bHandled = false;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                OnTouchDown(e);
                bHandled = true;
                break;
            }

            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:case MotionEvent.ACTION_MOVE: {
                OnTouchUp(e);
                bTouched = false;
                bHandled = true;
                break;
            }
        }

        return bHandled;
    }

    // Implementation //////////////////////////////////////////////////////////////////////////////
    private final int INDENT        = 15;
    private final float OMEGA_MS    = -0.5f;

    private float startX = 0;
    private float startY = 0;
    private float aimAngle = 0.0f;
    private long startTimeMS;

    private void OnTouchMove() {
        ConvenienceRobot robot = game.getRobot();

        if (robot != null) {
            long curTimeMS = java.lang.System.currentTimeMillis();
            float dx = startX - aimCenterX;

            aimAngle += dx / aimRadius * OMEGA_MS * (curTimeMS - startTimeMS);

            while (aimAngle < 0.0f) {
                aimAngle += 360.0f;
            }
            while (aimAngle > 360.0f) {
                aimAngle -= 360.0f;
            }

            startTimeMS = curTimeMS;

            robot.rotate(aimAngle);
        }
    }

    private void OnTouchUp(MotionEvent e) {
        bTouched = false;

        ConvenienceRobot robot = game.getRobot();
        robot.stop();

        if (WantsSwapStart() && WantsSwapEnd(startX, startY) && game != null) {
            game.enterDriveState();
        }
    }

    private void OnTouchDown(MotionEvent e) {
        startX = e.getX(0);
        startY = e.getY(0);

        if (Math.abs(startY - aimCenterY) < 0.5f * aimRadius) {
            bTouched = true;

            startTimeMS = java.lang.System.currentTimeMillis();
            aimAngle = 0.0f;
        }
    }

    private boolean WantsAimStart() {
        float dx = aimCenterX - startX;
        float dy = aimCenterY - startY;

        return dx * dx + dy * dy < aimRadius * aimRadius;
    }

    private boolean WantsAim(float x, float y) {
        float dx = aimCenterX - x;
        float dy = aimCenterY - y;

        return dx * dx + dy * dy < aimRadius * aimRadius;
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

        aimCenterX = w / 2;
        aimCenterY = h / 2;
        aimRadius  = bigDim / 2 * (INDENT - 2) / INDENT;

        swapRadius  = bigDim / INDENT;
        swapCenterX = swapRadius;
        swapCenterY = swapRadius;

        bUIready = true;
    }
}
