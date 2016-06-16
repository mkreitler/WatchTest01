package com.example.mark.watchtest01;

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
import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.command.RawMotorCommand;
import com.orbotix.common.sensor.Acceleration;
import com.orbotix.common.sensor.AttitudeSensor;
import com.orbotix.common.sensor.DeviceSensorsData;

/**
 * Created by Mark on 6/9/2016.
 */

public class GameStateShowEgg extends GameStateBase {
    private final int HATCH_TIMEOUT_MS      = 30000;
    private final int TWITCH_DELAY_MS       = 5500;
    private final int MIN_TWITCH_DELAY_MS   = 500;
    private final int FLASH_TIME            = 100;
    private final int MOTOR_MIN_POWER       = 50;
    private final int MOTOR_POWER_RANGE     = 200;
    private final float FLASH_CHANCE        = 0.1f;
    private final int TWITCH_THRESH         = 1;
    private final float COLLISION_THRESH    = 50.0f;
    private final float ACCEL_THRESH        = 2.0f;

    private BitmapDrawable egg          = null;
    private int iEgg                    = -1;
    private GameView.GameThread game    = null;
    private int colCount                = 0;
    private long lastTimeMS             = 0;
    private int hatchTimerMS            = 0;
    private int flashTimerMS            = 0;
    private int twitchStopMS            = 0;
    private float eggR                  = 0.0f;
    private float eggG                  = 0.0f;
    private float eggB                  = 0.0f;
    private float maxAccelX             = 0.0f;
    private float maxAccelY             = 0.0f;
    private float maxAccelZ             = 0.0f;
    private float accumYaw              = 0.0f;
    private float accumPitch            = 0.0f;
    private float accumRoll             = 0.0f;
    private int lastYaw                 = 0;
    private int lastPitch               = 0;
    private int lastRoll                = 0;
    private int nAttSamples             = 0;
    private boolean bTwitchAgain        = false;
    private int twitchWaitMS            = 0;

    // Interface ///////////////////////////////////////////////////////////////////////////////////
    public void SetEggInfo(BitmapDrawable egg, int index, float eggR, float eggG, float eggB) {
        this.egg = egg;
        iEgg = index;
        this.eggR = eggR;
        this.eggG = eggG;
        this.eggB = eggB;
    }

    @Override
    public void Enter(GameView.GameThread game) {
        this.game = game;

        colCount = 0;
        hatchTimerMS = 0;
        flashTimerMS = 0;
        twitchStopMS = 0;

        maxAccelX       = 0.0f;
        maxAccelY       = 0.0f;
        maxAccelZ       = 0.0f;
        accumYaw        = 0.0f;
        accumPitch      = 0.0f;
        accumRoll       = 0.0f;
        nAttSamples     = 0;

        lastTimeMS = java.lang.System.currentTimeMillis();

        ConvenienceRobot robot = game.getRobot();
        if (robot != null) {
            robot.calibrating(true);
            robot.enableStabilization(false);
            robot.setLed(eggR, eggG, eggB);
        }
    }

    @Override
    public void ProcessSensorData(DeviceSensorsData sensorData) {
        Acceleration accel = sensorData.getAccelerometerData().getFilteredAcceleration();
        AttitudeSensor att = sensorData.getAttitudeData();

        if (accel != null) {
            if (Math.abs(accel.x) > maxAccelX) {
                maxAccelX = (float)accel.x;

                if (Math.abs(maxAccelX) > ACCEL_THRESH) {
                    Flash();
                }
            }

            if (Math.abs(accel.y) > maxAccelY) {
                maxAccelY = (float)accel.y;

                if (Math.abs(maxAccelY) > ACCEL_THRESH) {
                    Flash();
                }
            }

            if (Math.abs(accel.z) > maxAccelZ) {
                maxAccelZ = (float)accel.z;

                if (Math.abs(maxAccelZ) > ACCEL_THRESH) {
                    Flash();
                }
            }
        }

        if (att != null) {
            if (Math.abs(att.yaw - lastYaw) > TWITCH_THRESH ||
                    Math.abs(att.pitch - lastPitch) > TWITCH_THRESH ||
                    Math.abs(att.roll - lastRoll) > TWITCH_THRESH) {
                if (twitchStopMS == 0 && colCount > 0) {
                    lastYaw = att.yaw;
                    lastPitch = att.pitch;
                    lastRoll = att.roll;

                    Twitch(true);
                }
            }

            accumPitch += att.pitch;
            accumYaw += att.yaw;
            accumRoll += att.roll;
            nAttSamples += 1;
        }
    }

    @Override
    public void ProcessCollision(CollisionDetectedAsyncData colData) {
        float powerX = colData.getImpactPower().x;
        float powerY = colData.getImpactPower().y;
        float power = (float)Math.sqrt(powerX * powerX + powerY * powerY);

        if (power > COLLISION_THRESH &&  colCount == 0) {
            // Wake up the embryo.

            Flash();
            Twitch(true);
        }

        colCount += 1;
    }

    @Override
    public boolean Update() {
        long currentTimeMS = java.lang.System.currentTimeMillis();
        int dtMS = (int)(currentTimeMS - lastTimeMS);
        ConvenienceRobot robot = game.getRobot();

        lastTimeMS = currentTimeMS;

        hatchTimerMS += dtMS;
        if (hatchTimerMS > HATCH_TIMEOUT_MS) {
            if (robot != null) {
                robot.setLed(0.0f, 0.0f, 0.0f);
            }

            game.enterShowHatchlingState(maxAccelX,
                                         maxAccelY,
                                         maxAccelZ,
                                         accumYaw / (float)nAttSamples,
                                         accumPitch / (float)nAttSamples,
                                         accumRoll / (float)nAttSamples,
                                         nAttSamples,
                                         iEgg);
        }

        if (flashTimerMS > 0) {
            flashTimerMS -= dtMS;
            if (flashTimerMS <= 0) {
                flashTimerMS = 0;

                if (robot != null) {
                    robot.setLed(eggR, eggG, eggB);
                }
            }
        }

        if (twitchStopMS > 0) {
            twitchStopMS -= dtMS;
            if (twitchStopMS <= 0) {
                twitchStopMS = 0;
                if (bTwitchAgain) {
                    Twitch(false);
                }
                else {
                    StopTwitch(robot);
                }
            }
        }

        if (twitchWaitMS > 0) {
            twitchWaitMS -= dtMS;
            twitchWaitMS = (int)Math.max(0, twitchWaitMS);
        }

        return false;
    }

    @Override
    public void Exit() {
        ConvenienceRobot robot = game.getRobot();

        if (robot != null) {
            robot.calibrating(false);
            robot.enableStabilization(true);
        }
    }

    private static Rect _srcRect = new Rect();
    private static Rect _destRect = new Rect();
    @Override
    public void Draw(Canvas c) {
        c.save();
        c.drawARGB(255, 0, 0, 0);

        GameView._paint.setColor(Color.WHITE);
        GameView._paint.setStyle(Paint.Style.FILL);
        GameView._paint.setTextSize(20.0f);
        GameView._paint.setStrokeWidth(5.0f);

        c.drawText("Sphero collected an egg!", 10, 25, GameView._paint);
        c.drawText("Shake to see what's inside.", 10, 50, GameView._paint);

        if (egg != null) {
            Bitmap bmp = egg.getBitmap();

            if (bmp != null) {
                int finalArc = (10 * hatchTimerMS) / HATCH_TIMEOUT_MS * 36;
                GameView._paint.setStyle(Paint.Style.STROKE);

                c.drawArc(c.getWidth() / 2 - bmp.getWidth() * 3 / 2,
                        c.getHeight() / 2 - bmp.getHeight() * 3 / 2,
                        c.getWidth() / 2 + bmp.getWidth() * 3 / 2,
                        c.getHeight() / 2 + bmp.getHeight() * 3 / 2,
                        0, finalArc, true, GameView._paint);

                _srcRect.set(0, 0, bmp.getWidth(), bmp.getHeight());

                int x = c.getWidth() / 2 - bmp.getWidth();
                int y = c.getHeight() / 2 - bmp.getHeight();

                _destRect.set(x, y, x + bmp.getWidth() * 2, y + bmp.getHeight() * 2);

                c.drawBitmap(bmp, _srcRect, _destRect, GameView._paint);
            }
        }

        GameView._paint.setStrokeWidth(1.0f);

        c.restore();
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        return false;
    }

    // Implementation //////////////////////////////////////////////////////////////////////////////
    private void Twitch(boolean bFirstHalf) {
        ConvenienceRobot robot = game.getRobot();
        if (robot != null && twitchWaitMS == 0) {
            bTwitchAgain = bFirstHalf;
            twitchStopMS = (int)Math.round(0.5f * MIN_TWITCH_DELAY_MS * Math.min(1.0f, Math.random() + 0.25f));

            int twitchType = (int)Math.floor(Math.random() * 4);
            int power = MOTOR_MIN_POWER + (int)Math.round(Math.random() * MOTOR_POWER_RANGE);

            switch(twitchType) {
                case 0:
                {
                    robot.setRawMotors(RawMotorCommand.MotorMode.MOTOR_MODE_FORWARD, power, RawMotorCommand.MotorMode.MOTOR_MODE_OFF, 0);
                    break;
                }

                case 1:
                {
                    robot.setRawMotors(RawMotorCommand.MotorMode.MOTOR_MODE_REVERSE, power, RawMotorCommand.MotorMode.MOTOR_MODE_OFF, 0);
                    break;
                }

                case 2:
                {
                    robot.setRawMotors(RawMotorCommand.MotorMode.MOTOR_MODE_OFF, 0, RawMotorCommand.MotorMode.MOTOR_MODE_FORWARD, power);
                    break;
                }

                default:
                {
                    robot.setRawMotors(RawMotorCommand.MotorMode.MOTOR_MODE_OFF, 0, RawMotorCommand.MotorMode.MOTOR_MODE_REVERSE, power);
                    break;
                }
            }
        }
    }

    private void Flash() {
        ConvenienceRobot robot = game.getRobot();

        if (robot != null && flashTimerMS == 0) {
            robot.setLed(1.0f, 1.0f, 1.0f);
            flashTimerMS = FLASH_TIME;
        }
    }

    private void StopTwitch(ConvenienceRobot robot) {
        twitchWaitMS = (int)(Math.random() * TWITCH_DELAY_MS) + MIN_TWITCH_DELAY_MS;

        if (robot != null) {
            robot.setRawMotors(RawMotorCommand.MotorMode.MOTOR_MODE_OFF, 0, RawMotorCommand.MotorMode.MOTOR_MODE_OFF, 0);
        }
    }
}
