/*
 * Copyright (C) 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 Copyright 2011 codeoedoc

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.box.game.planeandroid;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.admob.android.ads.AdView;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import android.util.Log;

class PlaneView extends SurfaceView implements SurfaceHolder.Callback,
        SensorEventListener {

    class PlaneThread extends Thread {

        private static final String TAG = "MyActivity";

        /*
         * Difficulty setting constants
         */
        public static final int DIFFICULTY_EASY = 1;
        public static final int DIFFICULTY_HARD = 3;
        public static final int DIFFICULTY_MEDIUM = 2;
        /*
         * Physics constants
         */
        public static final int PHYS_DOWN_ACCEL_SEC = 35;
        public static final int PHYS_FIRE_ACCEL_SEC = 80;
        public static final int PHYS_FUEL_INIT = 60;
        public static final int PHYS_FUEL_MAX = 100;
        public static final int PHYS_FUEL_SEC = 10;
        public static final int PHYS_SLEW_SEC = 120; // degrees/second rotate
        public static final int PHYS_SPEED_HYPERSPACE = 180;
        public static final int PHYS_SPEED_INIT = 30;
        public static final int PHYS_SPEED_MAX = 120;
        /*
         * State-tracking constants
         */
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_WIN = 5;

        /*
         * Goal condition constants
         */
        public static final int TARGET_ANGLE = 18; // > this angle means crash
        public static final int TARGET_BOTTOM_PADDING = 17; // px below gear
        public static final int TARGET_PAD_HEIGHT = 8; // how high above ground
        public static final int TARGET_SPEED = 28; // > this speed means crash
        public static final double TARGET_WIDTH = 1.6; // width of target
        /*
         * UI constants (i.e. the speed & fuel bars)
         */
        public static final int UI_BAR = 100; // width of the bar(s)
        public static final int UI_BAR_HEIGHT = 10; // height of the bar(s)
        private static final String KEY_DIFFICULTY = "mDifficulty";
        private static final String KEY_DX = "mDX";

        private static final String KEY_DY = "mDY";
        private static final String KEY_FUEL = "mFuel";
        private static final String KEY_GOAL_ANGLE = "mGoalAngle";
        private static final String KEY_GOAL_SPEED = "mGoalSpeed";
        private static final String KEY_GOAL_WIDTH = "mGoalWidth";

        private static final String KEY_GOAL_X = "mGoalX";
        private static final String KEY_HEADING = "mHeading";
        private static final String KEY_LANDER_HEIGHT = "mLanderHeight";
        private static final String KEY_LANDER_WIDTH = "mLanderWidth";
        private static final String KEY_WINS = "mWinsInARow";

        private static final String KEY_X = "mX";
        private static final String KEY_Y = "mY";

        /*
         * Member (state) fields
         */
        /** The drawable to use as the background of the animation canvas */
        private Bitmap mBackgroundImage;

        /**
         * Current height of the surface/canvas.
         * 
         * @see #setSurfaceSize
         */
        private int mCanvasHeight = 1;

        /**
         * Current width of the surface/canvas.
         * 
         * @see #setSurfaceSize
         */
        private int mCanvasWidth = 1;

        /**
         * Current difficulty -- amount of fuel, allowed angle, etc. Default is
         * MEDIUM.
         */
        private int mDifficulty;

        private String userid;

        /** Velocity dx. */
        private double mDX;

        /** Velocity dy. */
        private double mDY;

        /** Fuel remaining */
        private double mFuel;

        /** Allowed angle. */
        private int mGoalAngle;

        /** Allowed speed. */
        private int mGoalSpeed;

        /** Width of the landing pad. */
        private int mGoalWidth;

        /** X of the landing pad. */
        private int mGoalX;

        /** Message handler used by thread to interact with TextView */
        private Handler mHandler;

        /**
         * Lander heading in degrees, with 0 up, 90 right. Kept in the range
         * 0..360.
         */
        private double mHeading;

        /** Pixel height of lander image. */
        private int mLanderHeight;

        /** Pixel width of lander image. */
        private int mLanderWidth;

        /** Used to figure out elapsed time between frames */
        private long mLastTime;

        private long mLastActionTime;

        private boolean isTouched = false;

        // Moving Average Method
        private final int historySize = 10;
        private final int movingLength = 1;
        private final int oldIndexOffset = 5;
        private ArrayList<Float[]> sensorValues = new ArrayList<Float[]>(
                historySize);
        private int newValueCursor = -1;
        private float oldMovingAverage[] = new float[3];
        private float newMovingAverage[] = new float[3];

        /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
        private int mMode;

        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;

        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;

        /** Number of wins in a row. */
        private int mWinsInARow;

        /** X of lander center. */
        private double mX;

        /** Y of lander center. */
        private double mY;

        private int saveRecordCounter = 0;

        private Drawable mPlaneImage1;
        private Drawable mPlaneImage2;
        private Drawable mPlaneImage3;
        private Drawable mBullet;
        private Drawable[] mExplosion;

        private int mPlaneWidth;
        private int mPlaneHeight;

        private boolean mLeft = false;
        private boolean mRight = false;
        private boolean mUp = false;
        private boolean mDown = false;

        private int mPlanePos;

        private Bullet b;

        private int explosionFrameNum;
        private int mExplosionWidth;
        private int mExplosionHeight;

        private boolean isCollision;

        private long gameStartTime;
        private long gameEndTime;
        private double surviveTime;

        private CharSequence comment;

        public PlaneThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {

            // Moving average method
            for (int i = 0; i < historySize; i++) {
                Float values[] = new Float[3];
                values[0] = new Float(0);
                values[1] = new Float(0);
                values[2] = new Float(0);
                sensorValues.add(values);
            }

            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            Resources res = context.getResources();

            mPlaneImage1 = context.getResources()
                    .getDrawable(R.drawable.plane1);
            mPlaneImage2 = context.getResources()
                    .getDrawable(R.drawable.plane2);
            mPlaneImage3 = context.getResources()
                    .getDrawable(R.drawable.plane3);
            mBullet = context.getResources().getDrawable(R.drawable.bullet);
            mPlaneWidth = mPlaneImage1.getIntrinsicWidth();
            mPlaneHeight = mPlaneImage1.getIntrinsicHeight();

            mExplosion = new Drawable[4];
            mExplosion[0] = context.getResources().getDrawable(
                    R.drawable.explosion1);
            mExplosion[1] = context.getResources().getDrawable(
                    R.drawable.explosion2);
            mExplosion[2] = context.getResources().getDrawable(
                    R.drawable.explosion3);
            mExplosion[3] = context.getResources().getDrawable(
                    R.drawable.explosion4);
            mExplosionWidth = mExplosion[0].getIntrinsicWidth();
            mExplosionHeight = mExplosion[0].getIntrinsicHeight();

            mBackgroundImage = BitmapFactory.decodeResource(
                    res,
                    R.drawable.space);

            mPlanePos = 1;
            isCollision = false;

            mDifficulty = DIFFICULTY_EASY;

        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            synchronized (mSurfaceHolder) {

                if (mDifficulty == DIFFICULTY_EASY) {
                    b = new Bullet(mBullet, mCanvasWidth, mCanvasHeight, 20, 2);
                } else if (mDifficulty == DIFFICULTY_MEDIUM) {
                    b = new Bullet(mBullet, mCanvasWidth, mCanvasHeight, 40, 2);
                } else {
                    b = new Bullet(mBullet, mCanvasWidth, mCanvasHeight, 50, 3);
                }

                b.InitBulletsLocus();

                // pick a convenient initial location for the lander sprite
                mX = mCanvasWidth / 2;
                mY = mCanvasHeight / 2;

                explosionFrameNum = 0;

                mPlanePos = 1;
                mLeft = false;
                mRight = false;
                mUp = false;
                mDown = false;

                isCollision = false;

                mLastTime = System.currentTimeMillis() + 100;
                gameStartTime = System.currentTimeMillis();

                setState(STATE_RUNNING);
            }
        }

        /**
         * Pauses the physics update & animation.
         */
        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING)
                    setState(STATE_PAUSE);
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         * 
         * @param savedState
         *            Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
                setState(STATE_PAUSE);

                mDifficulty = savedState.getInt(KEY_DIFFICULTY);
                mX = savedState.getDouble(KEY_X);
                mY = savedState.getDouble(KEY_Y);
                mDX = savedState.getDouble(KEY_DX);
                mDY = savedState.getDouble(KEY_DY);
                mHeading = savedState.getDouble(KEY_HEADING);

                mLanderWidth = savedState.getInt(KEY_LANDER_WIDTH);
                mLanderHeight = savedState.getInt(KEY_LANDER_HEIGHT);
                mGoalX = savedState.getInt(KEY_GOAL_X);
                mGoalSpeed = savedState.getInt(KEY_GOAL_SPEED);
                mGoalAngle = savedState.getInt(KEY_GOAL_ANGLE);
                mGoalWidth = savedState.getInt(KEY_GOAL_WIDTH);
                mWinsInARow = savedState.getInt(KEY_WINS);
                mFuel = savedState.getDouble(KEY_FUEL);
            }
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        if (mMode == STATE_RUNNING)
                            updatePhysics();
                        doDraw(c);

                        if (saveRecordCounter > 0) {
                            saveRecordCounter++;
                        }
                        if (saveRecordCounter == 3) {
                            saveRecord(surviveTime);
                        }
                    }
                } finally {
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
                if (map != null) {
                    map.putInt(KEY_DIFFICULTY, Integer.valueOf(mDifficulty));
                    map.putDouble(KEY_X, Double.valueOf(mX));
                    map.putDouble(KEY_Y, Double.valueOf(mY));
                    map.putDouble(KEY_DX, Double.valueOf(mDX));
                    map.putDouble(KEY_DY, Double.valueOf(mDY));
                    map.putDouble(KEY_HEADING, Double.valueOf(mHeading));
                    map.putInt(KEY_LANDER_WIDTH, Integer.valueOf(mLanderWidth));
                    map.putInt(
                            KEY_LANDER_HEIGHT,
                            Integer.valueOf(mLanderHeight));
                    map.putInt(KEY_GOAL_X, Integer.valueOf(mGoalX));
                    map.putInt(KEY_GOAL_SPEED, Integer.valueOf(mGoalSpeed));
                    map.putInt(KEY_GOAL_ANGLE, Integer.valueOf(mGoalAngle));
                    map.putInt(KEY_GOAL_WIDTH, Integer.valueOf(mGoalWidth));
                    map.putInt(KEY_WINS, Integer.valueOf(mWinsInARow));
                    map.putDouble(KEY_FUEL, Double.valueOf(mFuel));
                }
            }
            return map;
        }

        /**
         * Sets the current difficulty.
         * 
         * @param difficulty
         */
        public void setDifficulty(int difficulty) {
            synchronized (mSurfaceHolder) {
                mDifficulty = difficulty;
            }
        }

        public void setUserID(String userId) {
            synchronized (mSurfaceHolder) {
                userid = userId;
            }
        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         * 
         * @param b
         *            true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @see #setState(int, CharSequence)
         * @param mode
         *            one of the STATE_* constants
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @param mode
         *            one of the STATE_* constants
         * @param message
         *            string to add to screen or null
         */
        public void setState(int mode, CharSequence message) {
            /*
             * This method optionally can cause a text message to be displayed
             * to the user when the mode changes. Since the View that actually
             * renders that text is part of the main View hierarchy and not
             * owned by this thread, we can't touch the state of that View.
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) {
                mMode = mode;

                if (mMode == STATE_RUNNING) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else {
                    Resources res = mContext.getResources();
                    CharSequence str = "";
                    if (mMode == STATE_READY)
                        str = res.getText(R.string.mode_ready);
                    else if (mMode == STATE_PAUSE)
                        str = res.getText(R.string.mode_pause);
                    else if (mMode == STATE_LOSE)
                        str = res.getText(R.string.mode_lose) + "\n"
                                + surviveTime + "sec\n" + comment;
                    else if (mMode == STATE_WIN)
                        str = res.getString(R.string.mode_win_prefix)
                                + mWinsInARow + " "
                                + res.getString(R.string.mode_win_suffix);

                    if (message != null) {
                        str = message + "\n" + str;
                    }

                    if (mMode == STATE_LOSE) {
                        mWinsInARow = 0;
                        isTouched = false;
                    }

                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }

        public void saveRecord(double record) {

            DBAdapter db = new DBAdapter(mContext);
            db.open();
            Cursor c = db.getRecordIDLevel(userid, mDifficulty);
            TelephonyManager telManager = (TelephonyManager) mContext
                    .getSystemService(mContext.TELEPHONY_SERVICE);
            if (c.getCount() == 0) {
                db.insertRecord(
                        userid,
                        telManager.getLine1Number(),
                        mDifficulty,
                        record,
                        "");
            } else {
                if (c.getInt(4) < record) {
                    db.updateRecord(
                            userid,
                            telManager.getLine1Number(),
                            mDifficulty,
                            record,
                            "");
                }
            }
            db.close();

            try {
                // Create a URL for the desired page
                URL url = new URL(PlaneAndroidConstants.SERVER_URL
                        + "?command=record&userid=" + userid + "&level="
                        + mDifficulty + "&record=" + record + "&isbn="
                        + telManager.getLine1Number());
                url.openStream();
            } catch (MalformedURLException e) {
                // Log.v(TAG, e.getMessage());
            } catch (IOException e) {
                // Log.v(TAG, e.getMessage());
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
                // don't forget to resize the background image
                mBackgroundImage = mBackgroundImage.createScaledBitmap(
                        mBackgroundImage,
                        width,
                        height,
                        true);
                mX = mCanvasWidth / 2;
                mY = mCanvasHeight / 2;
            }
        }

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
            }
            setState(STATE_RUNNING);
        }

        /**
         * Handles a key-down event.
         * 
         * @param keyCode
         *            the key that was pressed
         * @param msg
         *            the original event object
         * @return true
         */
        boolean doKeyDown(int keyCode, KeyEvent msg) {

            synchronized (mSurfaceHolder) {
                boolean okStart = false;
                if (mMode != STATE_RUNNING
                        && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    okStart = true;
                }

                if (okStart
                        && (mMode == STATE_READY || mMode == STATE_LOSE || mMode == STATE_WIN)) {
                    doStart();
                    return true;
                } else if (mMode == STATE_PAUSE && okStart) {
                    unpause();
                    return true;
                } else if (mMode == STATE_RUNNING) {

                    mLastActionTime = System.currentTimeMillis();

                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        mLeft = true;
                        mRight = false;
                        mUp = false;
                        mDown = false;
                        mPlanePos = 2;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        mRight = true;
                        mLeft = false;
                        mUp = false;
                        mDown = false;
                        mPlanePos = 3;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        mUp = true;
                        mDown = false;
                        mLeft = false;
                        mRight = false;
                        if (!mLeft && !mRight)
                            mPlanePos = 1;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mDown = true;
                        mUp = false;
                        mLeft = false;
                        mRight = false;
                        if (!mLeft && !mRight)
                            mPlanePos = 1;
                    }

                    return true;
                }

                return false;
            }

        }

        /**
         * Handles a key-up event.
         * 
         * @param keyCode
         *            the key that was pressed
         * @param msg
         *            the original event object
         * @return true if the key was handled and consumed, or else false
         */
        boolean doKeyUp(int keyCode, KeyEvent msg) {
            boolean handled = false;
            return handled;
        }

        public boolean doTrackballEvent(MotionEvent event) {

            return false;
        }

        public boolean doOnTouch(View v, MotionEvent event) {

            if (!isTouched && mMode != STATE_RUNNING) {
                doStart();
                isTouched = true;
            }
            return true;
        }

        public void doOrientationChange(float[] values) {

            // Moving average method
            if (newValueCursor == historySize - 1) {
                newValueCursor = 0;
            } else {
                newValueCursor++;
            }
            Float newValues[] = new Float[3];
            newValues[0] = values[0];
            newValues[1] = values[1];
            newValues[2] = values[2];
            sensorValues.add(newValueCursor, newValues);

            newMovingAverage[0] = 0;
            newMovingAverage[1] = 0;
            newMovingAverage[2] = 0;
            int index = newValueCursor;
            for (int i = 0; i < movingLength; i++) {
                if (index < 0) {
                    index = historySize - 1;
                }
                newMovingAverage[0] = newMovingAverage[0]
                        + sensorValues.get(index)[0];
                newMovingAverage[1] = newMovingAverage[1]
                        + sensorValues.get(index)[1];
                newMovingAverage[2] = newMovingAverage[2]
                        + sensorValues.get(index)[2];
                index--;
            }

            oldMovingAverage[0] = 0;
            oldMovingAverage[1] = 0;
            oldMovingAverage[2] = 0;
            int oldIndex = newValueCursor - oldIndexOffset;
            for (int i = 0; i < movingLength; i++) {
                if (oldIndex < 0) {
                    oldIndex = historySize - 1 + oldIndex;
                }
                oldMovingAverage[0] = oldMovingAverage[0]
                        + sensorValues.get(oldIndex)[0];
                oldMovingAverage[1] = oldMovingAverage[1]
                        + sensorValues.get(oldIndex)[1];
                oldMovingAverage[2] = oldMovingAverage[2]
                        + sensorValues.get(oldIndex)[2];
                oldIndex--;
            }

            int leftRightBuffer = 4;
            int upDownBuffer = 8;

            if (mMode == STATE_RUNNING) {

                if ((values[1] >= -90 && values[1] <= 90)) {
                    if (values[2] <= -leftRightBuffer) {
                        mRight = true;
                        mLeft = false;
                        mPlanePos = 3;
                    } else if (values[2] >= leftRightBuffer) {
                        mLeft = true;
                        mRight = false;
                        mPlanePos = 2;
                    }

                    else {
                        mLeft = false;
                        mRight = false;
                        mPlanePos = 1;
                    }

                } else {
                    if (values[2] >= leftRightBuffer) {
                        mRight = true;
                        mLeft = false;
                        mPlanePos = 3;
                    } else if (values[2] <= -leftRightBuffer) {
                        mLeft = true;
                        mRight = false;
                        mPlanePos = 2;
                    }

                    else {
                        mLeft = false;
                        mRight = false;
                        mPlanePos = 1;
                    }
                }

                if ((newMovingAverage[1] - oldMovingAverage[1]) > 300
                        || (newMovingAverage[1] - oldMovingAverage[1]) < -300) {
                    return;
                }

                if (newMovingAverage[1] - oldMovingAverage[1] >= upDownBuffer) {
                    mUp = true;
                    mDown = false;
                    if (!mLeft && !mRight) {
                        mPlanePos = 1;
                    }
                } else if (newMovingAverage[1] - oldMovingAverage[1] <= -upDownBuffer) {
                    mDown = true;
                    mUp = false;
                    if (!mLeft && !mRight) {
                        mPlanePos = 1;
                    }
                }

                if (Math.abs(newMovingAverage[1] - oldMovingAverage[1]) < upDownBuffer
                        && Math.abs(newMovingAverage[2] - oldMovingAverage[2]) >= leftRightBuffer) {
                    mDown = false;
                    mUp = false;
                    if (!mLeft && !mRight) {
                        mPlanePos = 1;
                    }
                }

            }
        }

        /**
         * Draws the ship, fuel/speed bars, and background to the provided
         * Canvas.
         */
        private void doDraw(Canvas canvas) {

            canvas.drawBitmap(mBackgroundImage, 0, 0, null);

            int yTop = mCanvasHeight - ((int) mY + mPlaneHeight / 2);
            int xLeft = (int) mX - mPlaneWidth / 2;

            if (mPlanePos == 1) {
                mPlaneImage1.setBounds(xLeft, yTop, xLeft + mPlaneWidth, yTop
                        + mPlaneHeight);
                mPlaneImage1.draw(canvas);
            } else if (mPlanePos == 2) {
                mPlaneImage2.setBounds(xLeft, yTop, xLeft + mPlaneWidth, yTop
                        + mPlaneHeight);
                mPlaneImage2.draw(canvas);
            } else if (mPlanePos == 3) {
                mPlaneImage3.setBounds(xLeft, yTop, xLeft + mPlaneWidth, yTop
                        + mPlaneHeight);
                mPlaneImage3.draw(canvas);
            }

            if (mMode == STATE_RUNNING && !isCollision) {
                b.setCanvas(canvas);
                isCollision = b.paint(xLeft, yTop);
                if (isCollision) {
                    gameEndTime = System.currentTimeMillis();
                }
            }
            if (mMode == STATE_LOSE) {
                mExplosion[3].setBounds(
                        xLeft,
                        yTop,
                        xLeft + mExplosionWidth,
                        yTop + mExplosionHeight);
                mExplosion[3].draw(canvas);
            }

            if (isCollision && explosionFrameNum <= 3) {

                mExplosion[explosionFrameNum].setBounds(xLeft, yTop, xLeft
                        + mExplosionWidth, yTop + mExplosionHeight);
                mExplosion[explosionFrameNum].draw(canvas);
                explosionFrameNum++;
            }
            if (isCollision && explosionFrameNum > 3) {

                surviveTime = (double) (gameEndTime - gameStartTime)
                        / (double) 1000;
                Resources res = mContext.getResources();

                if (surviveTime <= 10)
                    comment = res.getText(R.string.comment1);
                else if (surviveTime <= 16)
                    comment = res.getText(R.string.comment2);
                else if (surviveTime <= 20)
                    comment = res.getText(R.string.comment3);
                else if (surviveTime <= 25)
                    comment = res.getText(R.string.comment4);
                else if (surviveTime <= 30)
                    comment = res.getText(R.string.comment5);
                else if (surviveTime <= 40)
                    comment = res.getText(R.string.comment6);
                else if (surviveTime <= 99)
                    comment = res.getText(R.string.comment7);
                else
                    comment = res.getText(R.string.comment8);

                setState(STATE_LOSE);

                isCollision = false;

                saveRecordCounter = 1;

            }

        }

        /**
         * Figures the lander state (x, y, fuel, ...) based on the passage of
         * realtime. Does not invalidate(). Called at the start of draw().
         * Detects the end-of-game and sets the UI to the next state.
         */
        private void updatePhysics() {
            long now = System.currentTimeMillis();

            if (now - mLastActionTime > 50) {

            }

            double step = 1;

            if (mUp) {
                if (mY <= mCanvasHeight - 10) {
                    mY += step;
                }
            }
            if (mLeft) {
                if (mX >= 0 + 10) {
                    mX -= step;
                }
            }
            if (mDown) {
                if (mY >= 0 + 10) {
                    mY -= step;
                }
            }
            if (mRight) {
                if (mX <= mCanvasWidth - 10) {
                    mX += step;
                }
            }

            mLastTime = now;
        }
    }

    private static final String TAG = "TopGunView";

    /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;

    private AdView mAd;

    /** The thread that actually draws the animation */
    private PlaneThread thread;

    private int controlMethod = 0;

    public void setControlMethod(int controlMethod) {
        this.controlMethod = controlMethod;
    }

    public PlaneView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new PlaneThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mStatusText.setVisibility(m.getData().getInt("viz"));
                mStatusText.setText(m.getData().getString("text"));

                mAd.setVisibility(m.getData().getInt("viz"));
            }
        });

        this.setFocusable(true); // make sure we get key events
        this.setFocusableInTouchMode(true);

        this.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                return thread.doOnTouch(v, event);
            }
        });

    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     * 
     * @return the animation thread
     */
    public PlaneThread getThread() {
        return thread;
    }

    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

        return thread.doKeyDown(keyCode, msg);
    }

    /**
     * Standard override for key-up. We actually care about these, so we can
     * turn off the engine or stop rotating.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {

        return thread.doKeyUp(keyCode, msg);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        return thread.doTrackballEvent(event);
    }

    public void onSensorChanged(SensorEvent sensor) {
        if (controlMethod == 0) {
            return;
        }

        if (sensor.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            thread.doOrientationChange(sensor.values);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // We don't care.
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus)
            thread.pause();
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView, AdView adView) {
        mStatusText = textView;
        mAd = adView;
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