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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;
import com.admob.android.ads.SimpleAdListener;
import com.box.game.planeandroid.PlaneView.PlaneThread;

/*
 * The main gaming activity
 */
public class PlaneAndroid extends Activity {

    private static final String TAG = "MyActivity";

    private static final int MENU_EASY = 1;

    private static final int MENU_HARD = 2;

    private static final int MENU_MEDIUM = 3;

    private static final int MENU_PAUSE = 4;

    private static final int MENU_RESUME = 5;

    private static final int MENU_START = 6;

    private static final int MENU_STOP = 7;

    private static final int MENU_RECORD = 8;

    private String userid;

    private int controlMethod;

    private PlaneThread mLunarThread;

    private PlaneView mLunarView;

    private SensorManager mSensorManager;

    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu
     *            the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_START, 0, R.string.menu_start);
        // menu.add(0, MENU_STOP, 0, R.string.menu_stop);
        menu.add(0, MENU_RECORD, 0, R.string.menu_record);
        // menu.add(0, MENU_PAUSE, 0, R.string.menu_pause);
        // menu.add(0, MENU_RESUME, 0, R.string.menu_resume);
        menu.add(1, MENU_EASY, 0, R.string.menu_easy);
        menu.add(1, MENU_MEDIUM, 0, R.string.menu_medium);
        menu.add(1, MENU_HARD, 0, R.string.menu_hard);

        return true;
    }

    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item
     *            the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_START:
                mLunarThread.doStart();
                return true;
            case MENU_STOP:
                mLunarThread.setState(
                        PlaneThread.STATE_LOSE,
                        getText(R.string.message_stopped));
                return true;
                // case MENU_PAUSE:
                // mLunarThread.pause();
                // return true;
                // case MENU_RESUME:
                // mLunarThread.unpause();
                // return true;
            case MENU_EASY:
                mLunarThread.setDifficulty(PlaneThread.DIFFICULTY_EASY);
                return true;
            case MENU_MEDIUM:
                mLunarThread.setDifficulty(PlaneThread.DIFFICULTY_MEDIUM);
                return true;
            case MENU_HARD:
                mLunarThread.setDifficulty(PlaneThread.DIFFICULTY_HARD);
                return true;
            case MENU_RECORD:
                Intent i = new Intent(this, RecordShow.class);
                i.putExtra("ID", userid);
                startActivity(i);
                finish();
                return true;
        }

        return false;
    }

    /**
     * Invoked when the Activity is created.
     * 
     * @param savedInstanceState
     *            a Bundle containing state saved from a previous execution, or
     *            null if this is a new execution
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userid = extras.getString("ID");
            controlMethod = extras.getInt("Control");
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.plane_layout);
        mLunarView = (PlaneView) findViewById(R.id.plane);
        mLunarView.setControlMethod(controlMethod);
        mLunarThread = mLunarView.getThread();
        mLunarThread.setUserID(userid);
        AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR,
                "xxxxxxxxxxxx", });
        AdView ad = (AdView) findViewById(R.id.ad);
        ad.setAdListener(new TopGunListener());
        mLunarView.setTextView((TextView) findViewById(R.id.text), ad);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager
                .getSensorList(Sensor.TYPE_ORIENTATION);

        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            mLunarThread.setState(PlaneThread.STATE_READY);
            Log.w(this.getClass().getName(), "SIS is null");
        } else {
            // we are being restored: resume a previous game
            mLunarThread.restoreState(savedInstanceState);
            Log.w(this.getClass().getName(), "SIS is nonnull");
        }
    }

    private class TopGunListener extends SimpleAdListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.admob.android.ads.AdView.SimpleAdListener#onFailedToReceiveAd
         * (com.admob.android.ads.AdView)
         */
        @Override
        public void onFailedToReceiveAd(AdView adView) {
            // TODO Auto-generated method stub
            super.onFailedToReceiveAd(adView);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.admob.android.ads.AdView.SimpleAdListener#
         * onFailedToReceiveRefreshedAd(com.admob.android.ads.AdView)
         */
        @Override
        public void onFailedToReceiveRefreshedAd(AdView adView) {
            // TODO Auto-generated method stub
            super.onFailedToReceiveRefreshedAd(adView);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.admob.android.ads.AdView.SimpleAdListener#onReceiveAd(com.admob
         * .android.ads.AdView)
         */
        @Override
        public void onReceiveAd(AdView adView) {
            // TODO Auto-generated method stub
            super.onReceiveAd(adView);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.admob.android.ads.AdView.SimpleAdListener#onReceiveRefreshedAd
         * (com.admob.android.ads.AdView)
         */
        @Override
        public void onReceiveRefreshedAd(AdView adView) {
            // TODO Auto-generated method stub
            super.onReceiveRefreshedAd(adView);
        }

    }

    public void onFailedToReceiveAd(AdView adView) {
        Log.d("Lunar", "onFailedToReceiveAd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.admob.android.ads.AdView.AdListener#onFailedToReceiveRefreshedAd(
     * com.admob.android.ads.AdView)
     */
    public void onFailedToReceiveRefreshedAd(AdView adView) {
        Log.d("Lunar", "onFailedToReceiveRefreshedAd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.admob.android.ads.AdView.AdListener#onReceiveAd(com.admob.android
     * .ads.AdView)
     */
    public void onReceiveAd(AdView adView) {
        Log.d("Lunar", "onReceiveAd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.admob.android.ads.AdView.AdListener#onReceiveRefreshedAd(com.admob
     * .android.ads.AdView)
     */
    public void onReceiveRefreshedAd(AdView adView) {
        Log.d("Lunar", "onReceiveRefreshedAd");
    }

    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mLunarView);
        finish();
    }

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState
     *            a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        mLunarThread.saveState(outState);
        Log.w(this.getClass().getName(), "SIS called");
    }
}
