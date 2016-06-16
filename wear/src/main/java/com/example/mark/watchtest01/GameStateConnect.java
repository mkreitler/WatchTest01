package com.example.mark.watchtest01;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
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

public class GameStateConnect extends GameStateBase {
    private static final int ANIM_FRAME_MS              = 333;

    private RobotChangedStateListener mRobotListener    = null;
    private Context context                             = null;
    private boolean bFoundOrTimedOut                    = false;
    private GameView.GameThread game                    = null;
    private Sprite[] statusSprites                      = {null, null, null, null, null};
    private int animTimerMS                             = 0;
    private long lastAnimTime                           = 0;
    private int animFrame                               = 0;

    public GameStateConnect(RobotChangedStateListener robotStateListener, Context appContext) {
        mRobotListener = robotStateListener;
        context = appContext;
    }

    public void ProcessLocatorData(LocatorData locDat, float targetX, float targetY) {
        // Nothing to process in this state.
    }

    public void Enter(GameView.GameThread game) {
        this.game = game;
        lastAnimTime = java.lang.System.currentTimeMillis();
        animTimerMS = 0;
        animFrame = 0;

        if (statusSprites[0] == null) {
            CreateStatusSprites();
        }

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
        long currentTimeMS = java.lang.System.currentTimeMillis();
        int dtMS = (int)(currentTimeMS - lastAnimTime);

        lastAnimTime = currentTimeMS;

        animTimerMS += dtMS;

        while (animTimerMS > ANIM_FRAME_MS) {
             animTimerMS -= ANIM_FRAME_MS;
            animFrame += 1;
            animFrame = animFrame % statusSprites.length;
        }

        // DEBUG:
        return bFoundOrTimedOut;
    }

    public void Exit() {
        game.ResetTarget();

        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }
    }

    public void TestEggState() {
        Resources res = game.getContext().getResources();
        BitmapDrawable bmpDraw = null;

        switch((int)(Math.random() * statusSprites.length)) {
            case 0: {
                bmpDraw = (BitmapDrawable)res.getDrawable(R.drawable.egg_blue, null);
                game.enterShowEggState(bmpDraw, 0);
            }
            case 1: {
                bmpDraw = (BitmapDrawable)res.getDrawable(R.drawable.egg_green, null);
                game.enterShowEggState(bmpDraw, 1);
            }
            case 2: {
                bmpDraw = (BitmapDrawable)res.getDrawable(R.drawable.egg_red, null);
                game.enterShowEggState(bmpDraw, 2);
            }
            case 3: {
                bmpDraw = (BitmapDrawable)res.getDrawable(R.drawable.egg_purple, null);
                game.enterShowEggState(bmpDraw, 3);
            }
            default: {
                bmpDraw = (BitmapDrawable)res.getDrawable(R.drawable.egg_yellow, null);
                game.enterShowEggState(bmpDraw, 4);
            }
        }
    }

    private Rect srcRect = new Rect();
    private Rect destRect = new Rect();
    public void Draw(Canvas c) {

        c.save();
        c.drawARGB(255, 0, 0, 0);

        GameView._paint.setColor(Color.WHITE);
        GameView._paint.setStyle(Paint.Style.FILL);
        GameView._paint.setTextSize(20.0f);

        c.drawText("Looking for Sphero...", 10, 25, GameView._paint);

        float dw = c.getWidth() / (2 * statusSprites.length - 1);

        int x = Math.round(dw * (0.5f + 2.0f * animFrame));
        int y = c.getHeight() / 2;

        statusSprites[animFrame].setPosition(x, y);
        statusSprites[animFrame].draw(c);

        c.restore();
    }

    public boolean onTouch(View v, MotionEvent e) {
        return false;
    }

    // Implementation //////////////////////////////////////////////////////////////////////////////
    private void CreateStatusSprites() {
        Resources res = game.getContext().getResources();

        statusSprites[0] = new Sprite(res.getDrawable(R.drawable.egg_blue, null), 0, 0, 0.5f, 0.5f, 2.0f, 2.0f);
        statusSprites[1] = new Sprite(res.getDrawable(R.drawable.egg_green, null), 0, 0, 0.5f, 0.5f, 2.0f, 2.0f);
        statusSprites[2] = new Sprite(res.getDrawable(R.drawable.egg_red, null), 0, 0, 0.5f, 0.5f, 2.0f, 2.0f);
        statusSprites[3] = new Sprite(res.getDrawable(R.drawable.egg_purple, null), 0, 0, 0.5f, 0.5f, 2.0f, 2.0f);
        statusSprites[4] = new Sprite(res.getDrawable(R.drawable.egg_yellow, null), 0, 0, 0.5f, 0.5f, 2.0f, 2.0f);
    }
}
