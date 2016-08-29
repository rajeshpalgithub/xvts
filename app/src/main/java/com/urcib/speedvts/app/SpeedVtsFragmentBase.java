package com.urcib.speedvts.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.crashlytics.android.Crashlytics;
import com.urcib.speedvts.adapters.InternetConnectionListener;
import com.urcib.speedvts.helper.SpeedVtsConstants;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.webservice.RequestListener;
import com.urcib.speedvts.webservice.api.WebserviceConstants;

import org.json.JSONException;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class SpeedVtsFragmentBase extends Fragment implements RequestListener,
        SpeedVtsConstants, WebserviceConstants, SpeedVtsPreferences.PreferenceKeys,
        FragmentManager.OnBackStackChangedListener{

    private static final String TAG = "SpeedVtsFragmentBase";
    boolean canPrintLog = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getFragmentManager().addOnBackStackChangedListener(this);
        Fabric.with(getActivity(), new Crashlytics());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    /**
     * Upcoming log methods to handle the log concept at on place
     * @param message message to logged
     */
    public void logD(String message){
        if (canPrintLog){
            Log.d(TAG, message);
        }
    }

    public void logW(String message){
        if (canPrintLog){
            Log.w(TAG, message);
        }
    }

    public void logI(String message){
        if (canPrintLog){
            Log.i(TAG, message);
        }
    }

    public void logV(String message){
        if (canPrintLog){
            Log.v(TAG, message);
        }
    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {

    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {

    }

    // To show a new Screen and finish the current screen object
    public void showActivity(Activity thisActivity, Class<?> To) {
        Intent intent = new Intent(thisActivity, To);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        thisActivity.startActivity(intent);
        thisActivity.finish();
    }

    // To show a new Screen and without finishing the current screen object
    public void showActivityNotFinished(Activity thisActivity,
                                        Class<?> To) {
        Intent intent = new Intent(thisActivity, To);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        thisActivity.startActivity(intent);
    }

    /**
     *
     * @param jsObj
     * @param param
     * @return
     */
    public String getJsonObjectValueForString(JSONObject jsObj,
                                              String param) {
        String value = "";
        if (jsObj.has(param)) {
            try {
                value = jsObj.getString(param);
                if (value.equals("null") || TextUtils.isEmpty(value)) {
                    value = "";
                }
            } catch (JSONException e) {
            }
        }

        return value;

    }

    /**
     *
     * @param jsObj
     * @param param
     * @return
     */
    public int getJsonObjectValueForInteger(JSONObject jsObj,
                                               String param) {
        int value = 0;
        if (jsObj.has(param)) {
            try {
                value = jsObj.getInt(param);
            } catch (JSONException e) {
            }
        }

        return value;

    }

    /**
     *
     * @param jsObj
     * @param param
     * @return
     */
    public boolean getJsonObjectValueForBoolean(JSONObject jsObj,
                                                String param) {
        boolean value = false;
        if (jsObj.has(param)) {
            try {
                value = jsObj.getBoolean(param);
            } catch (JSONException e) {
            }
        }

        return value;

    }

    @Override
    public void onBackStackChanged() {

    }

    public boolean checkLocationEnabled(Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if (!gps_enabled && !network_enabled){
            return false;
        }else{
            return true;
        }

    }

    /**
     * Check the internet connection is available or not
     * @param thisActivity current activity reference
     * @return true if connected false if not
     */
    public static boolean haveInternet(Context thisActivity) {
        if (thisActivity != null) {
            NetworkInfo info = ((ConnectivityManager) thisActivity
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();

            if (info == null || !info.isConnected()) {
                return false;
            }
            if (info.isRoaming()) {
                return true;
            }

        }
        return true;
    }

}
