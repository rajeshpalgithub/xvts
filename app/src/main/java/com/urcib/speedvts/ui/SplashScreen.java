package com.urcib.speedvts.ui;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.urcib.speedvts.R;
import com.urcib.speedvts.app.SpeedVtsAppCombatBase;
import com.urcib.speedvts.helper.SpeedVtsPreferences;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class SplashScreen extends SpeedVtsAppCombatBase {

    private static final String TAG = "SplashScreen";

    private Runnable splashRunnable = new Runnable() {
        @Override
        public void run() {
            if (SpeedVtsPreferences.getStringValue(SplashScreen.this, token_key).length()<=0){
                showActivity(SplashScreen.this, LoginScreen.class);
            }else {
                showActivity(SplashScreen.this, SpeedVtsHome.class);
            }

        }
    };
    private Handler splashHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        splashHandler = new Handler();
        splashHandler.postDelayed(splashRunnable, SPLASH_LENGTH);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        splashHandler.removeCallbacks(splashRunnable);
    }
}
