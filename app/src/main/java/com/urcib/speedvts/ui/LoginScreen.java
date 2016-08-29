package com.urcib.speedvts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.urcib.speedvts.R;
import com.urcib.speedvts.app.SpeedVtsAppCombatBase;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.webservice.WebService;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class LoginScreen extends SpeedVtsAppCombatBase implements View.OnClickListener{
    private static final String TAG = "LoginScreen";

    private EditText txtMobileNumber, txtPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        init();

    }

    private void init(){
        txtMobileNumber = (EditText) findViewById(R.id.txtMobileNumber);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        txtMobileNumber.setText("tan@yahoo.com");
        txtPassword.setText("a");

        findViewById(R.id.btnLogin).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnLogin:
                if (haveInternet(LoginScreen.this)){
                    submitForm();
                }else {
                    Snackbar.make(findViewById(R.id.lnrRoot),"Internet is not connected.",Snackbar.LENGTH_LONG).show();
                }

                break;
            default:
                break;
        }
    }

    private void submitForm(){
        if (!validateFieldsEmpty(txtMobileNumber,
                getString(R.string.enter_email_mobile_number), getWindow())){
            return;
        }
        if (!validateFieldsEmpty(txtPassword,
                getString(R.string.enter_password), getWindow())){
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(ApiParameters.user, txtMobileNumber.getText().toString().trim());
        params.put(ApiParameters.pwd, txtPassword.getText().toString());

//        String loginUrl = BASE_URL + ApiMethods.login + querySymbol + ApiParameters.user + "=" +
//                txtMobileNumber.getText().toString().trim() + andSymbol + ApiParameters.pwd + "=" +
//                txtPassword.getText().toString();
        String loginUrl = BASE_URL + ApiMethods.login ;

        WebService.getInstance(LoginScreen.this).doRequestwithPOST(LoginScreen.this, ApiMethods.login,
                loginUrl , params, this, true);

    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);
        logD(responseMsg);

        try
        {
            JSONObject jsonObject = new JSONObject(responseMsg);
            if (jsonObject.has(WebserviceKeys.token)){
                String token = getJsonObjectValueForString(jsonObject, WebserviceKeys.token);
                SpeedVtsPreferences.setStringValue(LoginScreen.this, token_key, token);
                showActivity(LoginScreen.this, SpeedVtsHome.class);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {
        super.onErrorResponse(tag, responseCode, responseMsg);
        if (responseCode == HttpStatus.SC_UNAUTHORIZED){
            Toast.makeText(LoginScreen.this, "Wrong credential", Toast.LENGTH_LONG).show();
        }
    }
}
