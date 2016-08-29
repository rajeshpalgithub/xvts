package com.urcib.speedvts.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.urcib.speedvts.R;
import com.urcib.speedvts.helper.SpeedVtsConstants;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.webservice.RequestListener;
import com.urcib.speedvts.webservice.api.WebserviceConstants;

import org.json.JSONException;
import org.json.JSONObject;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class SpeedVtsAppCombatBase extends AppCompatActivity implements RequestListener,
        SpeedVtsConstants, WebserviceConstants, SpeedVtsPreferences.PreferenceKeys{

    boolean canPrintLog = true;
    private static final String TAG = "SpeedVtsAppCombatBase";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {

    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {

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

    /**
     * Thios function validates the text box is empty or not
     * @param txtFields Edittext to be validated
     * @param alert alert messages
     * @param window widnow to attached
     * @return return true or false
     */
    public boolean validateFieldsEmpty(EditText txtFields,
                                       String alert, Window window) {
        if (txtFields.getText().toString().trim().isEmpty()) {
            txtFields.setError(alert);
            requestFocus(txtFields, window);
            return false;
        }

        return true;
    }

    /**
     * Thios function validates the text box is empty or not
     * @param txtFields Edittext to be validated
     * @param alert alert messages
     * @param window widnow to attached
     * @return return true or false
     */
    public boolean validatePasswordLength(EditText txtFields,
                                       String alert, Window window) {
        if (txtFields.getText().toString().trim().length()<6) {
            txtFields.setError(alert);
            requestFocus(txtFields, window);
            return false;
        }

        return true;
    }

    /**
     * Thios function validates the text box is empty or not
     * @param txtFields Edittext to be validated
     * @param alert alert messages
     * @param window widnow to attached
     * @return return true or false
     */
    public boolean validateConfirmPassword(EditText txtFields, EditText txtConfirmFields,
                                          String alert, Window window) {

        String newPassword = txtFields.getText().toString();
        String confirmPassword = txtConfirmFields.getText().toString();
        if (!newPassword.equalsIgnoreCase(confirmPassword)) {
            txtConfirmFields.setError(alert);
            requestFocus(txtConfirmFields, window);
            return false;
        }

        return true;
    }

    /**
     * Requesting focus to the view
     * @param view view to be focussed
     * @param window view attached window
     */
    public void requestFocus(View view, Window window) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
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
    public String getJsonObjectValueForInteger(JSONObject jsObj,
                                               String param) {
        String value = "0";
        if (jsObj.has(param)) {
            try {
                value = jsObj.getString(param);
                if (value.equals("null") || TextUtils.isEmpty(value)) {
                    value = "0";
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

    public void switchFrags(Fragment fragment, String name, boolean addToBack) {
        FragmentTransaction transition = getSupportFragmentManager()
                .beginTransaction();
        transition.add(R.id.frame, fragment);
        transition.show(fragment);
        if (addToBack)
            transition.addToBackStack(fragment.getClass().getName());
        transition.commit();
        setTitle(name);
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

    public boolean validateFieldsEmptyWithAlert(EditText txtFields,
                                       String alert, Window window) {
        if (txtFields.getText().toString().trim().isEmpty()) {
            txtFields.setError(alert);
            requestFocus(txtFields, window);
            return false;
        }

        return true;
    }

    /**
     * Thios function validates the text box is empty or not
     * @param txtFields Edittext to be validated
     * @param alert alert messages
     * @param window widnow to attached
     * @return return true or false
     */
    public boolean validateFieldsEmpty(EditText txtFields,
                                       String alert, View rootView, Window window) {
        if (txtFields.getText().toString().trim().isEmpty()) {
//            txtFields.setError(alert);
            Snackbar.make(rootView, alert, Snackbar.LENGTH_LONG).show();
            requestFocus(txtFields, window);
            return false;
        }

        return true;
    }

}
