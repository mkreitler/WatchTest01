/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.mark.watchtest01;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.le.RobotLE;


/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 *
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 * current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 * updatePhysics() advances the physics based on realtime. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
class GameView extends SurfaceView implements SurfaceHolder.Callback, RobotChangedStateListener, ResponseListener {
    protected ConvenienceRobot mRobot           = null;

    public static Paint _paint = new Paint();

    public void lookForRobot() {
        thread.lookForRobot(this);
    }

    class GameThread extends Thread {
        protected SurfaceHolder mSurfaceHolder  = null;
        protected Handler mHandler              = null;
        protected Context mContext              = null;
        protected boolean mRun                  = false;
        protected final Object mRunLock         = new Object();
        protected int mCanvasWidth              = 0;
        protected int mCanvasHeight             = 0;
        protected IGameState currentState       = null;
        protected GameStateConnect stateConnect = null;
        protected GameStateDrive stateDrive     = null;
        protected GameStateAim stateAim         = null;
        protected ConvenienceRobot mRobot       = null;

        protected GameStateIntro stateIntro     = new GameStateIntro();

        public void lookForRobot(RobotChangedStateListener robotMonitor) {
            stateConnect = new GameStateConnect(robotMonitor, mContext);
            stateDrive = new GameStateDrive();
            stateAim = new GameStateAim();
            thread.enterConnectState();
        }

        public GameThread(SurfaceHolder surfaceHolder, Context context,
                           Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            setState(stateIntro);
            /*
            Resources res = context.getResources();

            mBackgroundImage = BitmapFactory.decodeResource(res,
                    R.drawable.earthrise);
            */
        }

        public void enterDriveState() {
            setState(stateDrive);
        }
        public void enterAimState() { setState(stateAim); }

        public void enterConnectState() {
            setState(stateConnect);
        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            synchronized (mSurfaceHolder) {
                mRun = true;
            }
        }

        /**
         * Stops the game, resets the robot.
         */
        public void doStop() {
            //If a robot is connected to the device, disconnect it
            if( mRobot != null ) {
                mRobot.stop();
                mRobot.calibrating(false);
                mRobot.disconnect();
                thread.setRobot(null);
                mRobot = null;
            }

            synchronized(mSurfaceHolder) {
                mRun = false;
            }
        }

        /**
         * Pauses the physics update & animation.
         */
        public void pause() {
            synchronized (mSurfaceHolder) {
            }
        }

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         *
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
            }
        }

        public void setRobot(ConvenienceRobot robot) {
            mRobot = robot;
        }

        public ConvenienceRobot getRobot() {
            return mRobot;
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        // Critical section. Do not allow mRun to be set false until
                        // we are sure all canvas draw operations are complete.
                        //
                        // If mRun has been toggled false, inhibit canvas operations.
                        if (currentState != null) {
                            // TODO: check return value and exit it true?
                            currentState.Update();
                        }

                        synchronized (mRunLock) {
                            if (mRun) doDraw(c);
                        }
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         *
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
                /*
                if (map != null) {
                    map.putInt(KEY_DIFFICULTY, Integer.valueOf(mDifficulty));
                    map.putDouble(KEY_X, Double.valueOf(mX));
                    map.putDouble(KEY_Y, Double.valueOf(mY));
                    map.putDouble(KEY_DX, Double.valueOf(mDX));
                    map.putDouble(KEY_DY, Double.valueOf(mDY));
                    map.putDouble(KEY_HEADING, Double.valueOf(mHeading));
                    map.putInt(KEY_LANDER_WIDTH, Integer.valueOf(mLanderWidth));
                    map.putInt(KEY_LANDER_HEIGHT, Integer
                            .valueOf(mLanderHeight));
                    map.putInt(KEY_GOAL_X, Integer.valueOf(mGoalX));
                    map.putInt(KEY_GOAL_SPEED, Integer.valueOf(mGoalSpeed));
                    map.putInt(KEY_GOAL_ANGLE, Integer.valueOf(mGoalAngle));
                    map.putInt(KEY_GOAL_WIDTH, Integer.valueOf(mGoalWidth));
                    map.putInt(KEY_WINS, Integer.valueOf(mWinsInARow));
                    map.putDouble(KEY_FUEL, Double.valueOf(mFuel));
                }
                */
            }
            return map;
        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         *
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            // Do not allow mRun to be modified while any canvas operations
            // are potentially in-flight. See doDraw().
            synchronized (mRunLock) {
                mRun = b;
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         */
        public void setState(IGameState newState) {
            synchronized (mSurfaceHolder) {
                if (currentState != newState) {
                    if (currentState != null) {
                        currentState.Exit();
                    }

                    if (newState != null) {
                        newState.Enter(this);
                    }

                    currentState = newState;
                }
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

                /*
                // don't forget to resize the background image
                mBackgroundImage = Bitmap.createScaledBitmap(
                        mBackgroundImage, width, height, true);
                */
            }
        }

        public boolean onTouch(View v, MotionEvent e) {
            boolean bHandled = false;

            synchronized(mSurfaceHolder) {
                if (currentState != null) {
                   bHandled = currentState.onTouch(v, e);
                }
            }

            return bHandled;
        }

        private void doDraw(Canvas canvas) {
            if (currentState != null) {
                currentState.Draw(canvas);
            }
        }
    }

    // RobotChangedStateListener Interface /////////////////////////////////////////////////////////
    @Override
    public void handleRobotChangedState( Robot robot, RobotChangedStateNotificationType type ) {
        Log.d("Sphero", ">>> handleRobotChangedState");
        switch( type ) {
            case Online: {

                //If robot uses Bluetooth LE, Developer Mode can be turned on.
                //This turns off DOS protection. This generally isn't required.
                if( robot instanceof RobotLE) {
                    ( (RobotLE) robot ).setDeveloperMode( true );
                }

                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot( robot );
                mRobot.enableCollisions(true);
                mRobot.addResponseListener(this);

                Log.d( "Sphero", "Robot online..." );
                if (thread != null) {
                    Log.d( "Sphero", "...thread found, entering 'Drive' state." );
                    thread.setRobot(mRobot);
                    thread.enterAimState();
                }

                break;
            }

            default: {
                Log.d( "Sphero", "Robot found but not online." );
            }
        }
    }

    // ResponseListener Interface //////////////////////////////////////////////////////////////////
    private final float COLLISION_STRONG = 200.0f;
    private final float COLLISION_MEDIUM = 125.0f;
    private final float COLLISION_WEAK   = 75.0f;

    @Override
    public void handleResponse(DeviceResponse response, Robot robot) {
        // Do something with the response here
    }

    @Override
    public void handleStringResponse(String stringResponse, Robot robot) {
        // Handle string responses from the robot here
    }

    @Override
    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {
        Log.d("GameView", "Incoming async message...");
        if( asyncMessage instanceof CollisionDetectedAsyncData) {
            Vibrator vibrator = (Vibrator) thread.mContext.getSystemService(Context.VIBRATOR_SERVICE);

            //Collision occurred.
            CollisionDetectedAsyncData collision = (CollisionDetectedAsyncData)asyncMessage;
            float powerX = collision.getImpactPower().x;
            float powerY = collision.getImpactPower().y;
            float power = (float)Math.sqrt(powerX * powerX + powerY * powerY);

            Log.d("GameView", "   !!! Collision with power " + power);

            if (power > COLLISION_STRONG) {
                long[] vibrationPattern = {0, 50, 50, 250};
                vibrator.vibrate(vibrationPattern, -1);
            }
            else if (power > COLLISION_MEDIUM) {
                long[] vibrationPattern = {0, 250};
                vibrator.vibrate(vibrationPattern, -1);
            }
            else if (power > COLLISION_WEAK) {
                long[] vibrationPattern = {0, 100};
                vibrator.vibrate(vibrationPattern, -1);
            }

        }
    }

    // GameView Interface //////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean bHandled = false;

        if (thread != null) {
            bHandled = thread.onTouch(this, event);
        }

        return bHandled;
    }

    /** Pointer to the text view to display "Paused.." etc. */
    /** The thread that actually draws the animation */
    private GameThread thread   = null;

    public GameView(Context context) {
        super(context);
        createThread(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createThread(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createThread(context);
    }

    protected void createThread(Context context) {
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // create thread only; it's started in surfaceCreated()
        thread = new GameThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
            }
        });
        setFocusable(true); // make sure we get key events
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public GameThread getThread() {
        return thread;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        thread.setSurfaceSize(width, height);
    }
    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }
    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}
