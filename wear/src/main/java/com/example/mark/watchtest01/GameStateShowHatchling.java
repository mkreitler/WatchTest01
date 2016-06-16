package com.example.mark.watchtest01;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Mark on 6/15/2016.
 */
public class GameStateShowHatchling extends GameStateBase {
    private final float GAME_AVE_VALUE  = 3.0f;
    private final float GAME_MAX_VALUE  = 5.0f;
    private final float GAME_MIN_VALUE  = 1.0f;
    private final float MAX_ACCEL       = 10.0f;
    private final float ACCEL_TO_GAME   = 1.33f;

    private GameView.GameThread game    = null;

    private float maxAccelX     = 0.0f;
    private float maxAccelY     = 0.0f;
    private float maxAccelZ     = 0.0f;
    private float aveYaw        = 0.0f;
    private float avePitch      = 0.0f;
    private float aveRoll       = 0.0f;

    private int size            = 0;
    private int speed           = 0;
    private int power           = 0;
    private int bodyIndex       = 0;
    private int colorIndex      = 0;
    private int powerIndex      = 0;
    private int nPowerCards     = 0;

    private int drawState       = 0;

    private String[][] powerText= {
            {"Flood", "Cleanse", "Whirlpool"},
            {"Camouflage", "Regrow", "Bark"},
            {"Fire Breath", "Magma", "IR Vision"},
            {"Teleport", "Telepathy", "Telekinesis"},
            {"Acid", "Burrowing", "Hive Mind"}
    };

    private Sprite[][] bodies = {
            {null, null, null},
            {null, null, null},
            {null, null, null},
            {null, null, null},
            {null, null, null},
    };

    // Interface ///////////////////////////////////////////////////////////////////////////////////
    public void SetHatchlingData(float maxAccelX, float maxAccelY, float maxAccelZ,
                                 float aveYaw, float avePitch, float aveRoll, int nSamples, int iEgg) {
        this.maxAccelX = maxAccelX;
        this.maxAccelY = maxAccelY;
        this.maxAccelZ = maxAccelZ;

        this.aveYaw     = aveYaw;
        this.avePitch   = avePitch;
        this.aveRoll    = aveRoll;

        size = AccelToGameValue(maxAccelX);
        speed = AccelToGameValue(maxAccelY);
        power = AccelToGameValue(maxAccelZ);

        nPowerCards = (3 * (int)GAME_MAX_VALUE - (size + speed + power)) / 3;

        colorIndex = iEgg;

        if (Math.abs(aveYaw) > Math.abs(avePitch) && Math.abs(aveYaw) > Math.abs(aveRoll)) {
            // Choose body based on yaw.
            if (Math.abs(aveYaw * nSamples) < 120) {
                bodyIndex = 1;
            }
            else if (aveYaw < 0) {
                bodyIndex = 0;
            }
            else {
                bodyIndex = 2;
            }

            powerIndex = (int)Math.abs(avePitch + aveRoll) % powerText[0].length;
        }
        else if (Math.abs(avePitch) > Math.abs(aveRoll)) {
            // Choose body based on pitch.
            if (Math.abs(avePitch * nSamples) < 120) {
                bodyIndex = 1;
            }
            else if (avePitch < 0) {
                bodyIndex = 0;
            }
            else {
                bodyIndex = 2;
            }

            powerIndex = (int)Math.abs(aveYaw + aveRoll) % powerText[0].length;
        }
        else {
            // Choose body based on roll.
            if (Math.abs(aveRoll * nSamples) < 120) {
                bodyIndex = 1;
            }
            else if (aveRoll < 0) {
                bodyIndex = 0;
            }
            else {
                bodyIndex = 2;
            }

            powerIndex = (int)Math.abs(avePitch + aveYaw) % powerText[0].length;
        }
    }

    @Override
    public void Enter(GameView.GameThread game) {
        this.game = game;

        if (bodies[0][0] == null) {
            InitBodies();
        }

        drawState = 0;
    }

    @Override
    public void Exit() {
        game.ResetTarget();
    }

    @Override
    public void Draw(Canvas c) {
        c.drawARGB(255, 0, 0, 0);

        GameView._paint.setStyle(Paint.Style.FILL);
        GameView._paint.setColor(Color.WHITE);
        GameView._paint.setTextSize(20);

        switch(drawState) {
            case 0: {
                c.drawText("Your hatchling:", 10, 20, GameView._paint);
                c.drawText("   maxAccelX: " + (int)Math.round(maxAccelX), 10, 45, GameView._paint);
                c.drawText("   maxAccelY: " + (int)Math.round(maxAccelY), 10, 70, GameView._paint);
                c.drawText("   maxAccelZ: " + (int)Math.round(maxAccelZ), 10, 95, GameView._paint);
                c.drawText("   aveYaw   : " + (int)Math.round(aveYaw), 10, 120, GameView._paint);
                c.drawText("   avePitch : " + (int)Math.round(avePitch), 10, 145, GameView._paint);
                c.drawText("   aveRoll  : " + (int)Math.round(aveRoll), 10, 170, GameView._paint);

                break;
            }

            case 1: {
                c.drawText("Your hatchling:", 10, 20, GameView._paint);
                c.drawText("   Size : " + size, 10, 45, GameView._paint);
                c.drawText("   Speed: " + speed, 10, 70, GameView._paint);
                c.drawText("   Power: " + power, 10, 95, GameView._paint);
                c.drawText("   Cards: " + nPowerCards, 10, 120, GameView._paint);
                c.drawText("   Skill: " + powerText[colorIndex][powerIndex], 10, 145, GameView._paint);

                Sprite body = bodies[colorIndex][bodyIndex];
                body.setScale(0.5f + 0.5f * size, 0.5f + 0.5f * size);
                body.setAnchor(0.5f, 0.0f);
                body.setPosition(c.getWidth() / 2, c.getHeight() / 2);
                body.draw(c);

                break;
            }
        }
     }

    public boolean onTouch(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            drawState += 1;

            if (drawState >= 2) {
                game.enterDriveState();
            }
        }

        return true;
    }

    // Implementation //////////////////////////////////////////////////////////////////////////////
    private void InitBodies() {
        Resources res = game.getContext().getResources();

        bodies[0][0] = new Sprite(res.getDrawable(R.drawable.alien01_blue, null), 0, 0, 0.5f, 0.5f);
        bodies[0][1] = new Sprite(res.getDrawable(R.drawable.alien02_blue, null), 0, 0, 0.5f, 0.5f);
        bodies[0][2] = new Sprite(res.getDrawable(R.drawable.alien03_blue, null), 0, 0, 0.5f, 0.5f);

        bodies[1][0] = new Sprite(res.getDrawable(R.drawable.alien01_green, null), 0, 0, 0.5f, 0.5f);
        bodies[1][1] = new Sprite(res.getDrawable(R.drawable.alien02_green, null), 0, 0, 0.5f, 0.5f);
        bodies[1][2] = new Sprite(res.getDrawable(R.drawable.alien03_green, null), 0, 0, 0.5f, 0.5f);

        bodies[2][0] = new Sprite(res.getDrawable(R.drawable.alien01_red, null), 0, 0, 0.5f, 0.5f);
        bodies[2][1] = new Sprite(res.getDrawable(R.drawable.alien02_red, null), 0, 0, 0.5f, 0.5f);
        bodies[2][2] = new Sprite(res.getDrawable(R.drawable.alien03_red, null), 0, 0, 0.5f, 0.5f);

        bodies[3][0] = new Sprite(res.getDrawable(R.drawable.alien01_purple, null), 0, 0, 0.5f, 0.5f);
        bodies[3][1] = new Sprite(res.getDrawable(R.drawable.alien02_purple, null), 0, 0, 0.5f, 0.5f);
        bodies[3][2] = new Sprite(res.getDrawable(R.drawable.alien03_purple, null), 0, 0, 0.5f, 0.5f);

        bodies[4][0] = new Sprite(res.getDrawable(R.drawable.alien01_yellow, null), 0, 0, 0.5f, 0.5f);
        bodies[4][1] = new Sprite(res.getDrawable(R.drawable.alien02_yellow, null), 0, 0, 0.5f, 0.5f);
        bodies[4][2] = new Sprite(res.getDrawable(R.drawable.alien03_yellow, null), 0, 0, 0.5f, 0.5f);
    }

    private int AccelToGameValue(float accel) {
        float rawGameVal = accel > 0.0f ? (int)(accel / ACCEL_TO_GAME) : (int)((MAX_ACCEL + accel) / ACCEL_TO_GAME);

        int gameVal = (int)Math.round(rawGameVal);
        gameVal = (int)Math.max(gameVal, GAME_MIN_VALUE);
        gameVal = (int)Math.min(gameVal, GAME_MAX_VALUE);

        return gameVal;
    }
}
