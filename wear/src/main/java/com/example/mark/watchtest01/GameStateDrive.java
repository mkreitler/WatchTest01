package com.example.mark.watchtest01;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.mark.watchtest01.IGameState;
import com.orbotix.ConvenienceRobot;
import com.orbotix.command.RollCommand;
import com.orbotix.common.sensor.LocatorData;

import java.util.List;
import java.util.Vector;

/**
 * Created by Mark on 6/10/2016.
 */
public class GameStateDrive extends GameStateBase {
    private final int heartbeat             = 330;
    private final float FIND_DISTANCE       = 10.0f;    // cm
    private final float FIND_SPEED          = 20.0f;    // cm / sec
    private final float MAX_BLINK_DELAY     = 10000.0f; // MS
    private final float MIN_BLINK_DELAY_MS  = 67;
    private final int BLINK_LENGTH_MS       = 333;

    private static Rect _bounds = new Rect();

    private int driveCenterX            = 0;
    private int driveCenterY            = 0;
    private int driveRadius             = 0;
    private int swapCenterX             = 0;
    private int swapCenterY             = 0;
    private int swapRadius              = 0;
    private boolean bUIready            = false;
    private GameView.GameThread game    = null;
    private float dx                    = 0.0f;
    private float dy                    = 0.0f;
    private int blinkTimerMS            = 0;
    private int blinkDelayMS            = 0;
    private boolean bLit                = false;
    private long lastUpdateTime         = 0;
    protected BitmapDrawable[] eggs     = {null, null, null, null, null};

    private class TapData {
        public float x;
        public float y;

        public TapData(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private Vector<TapData> taps          = new Vector<TapData>();

    @Override
    public void ProcessLocatorData(LocatorData locDat, float targetX, float targetY) {
        // As we drive, check to see if we have arrived at the desired location.
        dx = locDat.getPositionX() - targetX;
        dy = locDat.getPositionY() - targetY;

        float vx = locDat.getVelocityX();
        float vy = locDat.getVelocityY();

        if (dx * dx + dy * dy < FIND_DISTANCE * FIND_DISTANCE &&
                vx * vx + vy * vy < FIND_SPEED * FIND_SPEED) {
            // FOR_NOW: turn green and stop flashing.
            ConvenienceRobot robot = game != null ? game.getRobot() : null;
            if (robot != null) {
                robot.setLed(0.0f, 1.0f, 0.0f);
            }

            blinkDelayMS = -1;
            ChooseEggAndExit();
        }
        else if (blinkDelayMS >= 0.0f) {
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            blinkDelayMS = (int)(dist / FIND_DISTANCE * MIN_BLINK_DELAY_MS);
            blinkDelayMS = (int)Math.max(blinkDelayMS, MIN_BLINK_DELAY_MS);
            blinkDelayMS = (int)Math.min(blinkDelayMS, MAX_BLINK_DELAY);
        }
    }

    @Override
    public void Enter(GameView.GameThread gameThread) {
        game = gameThread;
        blinkTimerMS = 0;
        blinkDelayMS = 0;
        lastUpdateTime = java.lang.System.currentTimeMillis();
        bLit = false;
        taps.clear();

        ConvenienceRobot robot = game != null ? game.getRobot() : null;
        if (robot != null) {
            robot.setLed(0.0f, 0.0f, 0.0f);
        }

        Resources res = gameThread.getContext().getResources();

        eggs[0] = (BitmapDrawable)res.getDrawable(R.drawable.egg_blue, null);
        eggs[1] = (BitmapDrawable)res.getDrawable(R.drawable.egg_green, null);
        eggs[2] = (BitmapDrawable)res.getDrawable(R.drawable.egg_red, null);
        eggs[3] = (BitmapDrawable)res.getDrawable(R.drawable.egg_purple, null);
        eggs[4] = (BitmapDrawable)res.getDrawable(R.drawable.egg_yellow, null);
    }

    // Update should return 'true' when the state has finished updating.
    @Override
    public boolean Update() {
        boolean bExit = false;
        long currentTimeMS = java.lang.System.currentTimeMillis();
        ConvenienceRobot robot = game != null ? game.getRobot() : null;

        if (bDriving && robot != null) {
            long curTimeMS = java.lang.System.currentTimeMillis();
            if (curTimeMS - driveTimeMS > heartbeat) {
                if (robot != null) {
                    robot.drive(driveAngle, driveVel);
                }

                driveTimeMS = curTimeMS;
            }
        }

        if (bLit && currentTimeMS > lastUpdateTime + BLINK_LENGTH_MS) {
            bLit = false;
            lastUpdateTime = currentTimeMS;
            if (robot != null) {
                robot.setLed(0.0f, 0.0f, 0.0f);
            }
        }
        else if (!bLit && blinkDelayMS >= 0) {
            blinkTimerMS += (int)(currentTimeMS - lastUpdateTime);
            lastUpdateTime = currentTimeMS;

            if (blinkTimerMS > blinkDelayMS && robot != null) {
                blinkTimerMS = 0;
                bLit = true;
                robot.setLed(1.0f, 1.0f, 1.0f);
            }
        }

        return bExit;
    }

    @Override
    public void Exit() {
    }

    @Override
    public void Draw(Canvas c) {
        if (!bUIready) {
            InitUI(c);
        }

        c.save();
        c.drawARGB(255, 0, 0, 0);

        GameView._paint.setStyle(Paint.Style.STROKE);
        GameView._paint.setColor(Color.GREEN);
        GameView._paint.setStrokeWidth(4.0f);
        c.drawArc(driveCenterX - driveRadius, driveCenterY - driveRadius, driveCenterX + driveRadius, driveCenterY + driveRadius , 0, 360, true, GameView._paint);

        GameView._paint.setTextSize(20);
        GameView._paint.setStyle(Paint.Style.FILL);
        GameView._paint.setColor(Color.WHITE);
        GameView._paint.setStrokeWidth(1.0f);

        String text = "Lifeform Detected";
        GameView._paint.getTextBounds(text, 0, text.length(), _bounds);

        int x = (c.getWidth() / 2) - (_bounds.width() / 2);
        int y = (c.getHeight() / 2) - (_bounds.height() / 2) - 12;
        c.drawText(text, x, y, GameView._paint);

        text = "Find it!";
        GameView._paint.getTextBounds(text, 0, text.length(), _bounds);
        x = (c.getWidth() / 2) - (_bounds.width() / 2);
        y += 22;
        c.drawText(text, x, y, GameView._paint);

        GameView._paint.setStyle(Paint.Style.FILL_AND_STROKE);
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

        taps.add(new TapData(startX - driveCenterX, startY - driveCenterY));

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

        swapRadius  = bigDim / INDENT;
        swapCenterX = swapRadius;
        swapCenterY = swapRadius;

        bUIready = true;
    }

    private void ChooseEggAndExit() {
        // Compute an angle using randomly-selected tap data.
        // Use this angle to choose one of the 5 eggs.
        float x = (float)(Math.random() - 0.5) * game.width();
        float y = (float)(Math.random() - 0.5) * game.height();

        int nTaps = taps.size();
        if (nTaps > 0) {
            int xIndex = (int) (Math.floor(Math.random() * nTaps));
            int yIndex = (int) (Math.floor(Math.random() * nTaps));

            x = taps.elementAt(xIndex).x;
            y = taps.elementAt(yIndex).y;
        }

        float angle = (float)(Math.atan2(y, x) * 180.0 / Math.PI);

        while (angle < 0.0f) {
            angle += 360.0f;
        }
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }

        int eggIndex = (int)(angle * eggs.length / 360.0f);

        game.enterShowEggState(eggs[eggIndex], eggIndex);
    }
}
