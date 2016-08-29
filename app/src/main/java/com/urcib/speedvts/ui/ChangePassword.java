package com.urcib.speedvts.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.urcib.speedvts.R;
import com.urcib.speedvts.app.SpeedVtsAppCombatBase;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.webservice.WebService;

import java.util.HashMap;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class ChangePassword extends SpeedVtsAppCombatBase implements View.OnClickListener{

    private EditText txtCurrentPassword, txtNewPassword, txtConfirmPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        init();
    }

    private void init(){
        getSupportActionBar().setTitle("Change Password");
        txtCurrentPassword = (EditText) findViewById(R.id.txtCurrentPassword);
        txtNewPassword = (EditText) findViewById(R.id.txtNewPassword);
        txtConfirmPassword = (EditText) findViewById(R.id.txtConfirmPassword);

        findViewById(R.id.btnChangePassword).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnChangePassword:
                submitForm();
                break;
            default:
                break;
        }
    }

    private void submitForm(){
        if (!validateFieldsEmpty(txtCurrentPassword,
                getString(R.string.enter_current_password), getWindow())){
            return;
        }
        if (!validateFieldsEmpty(txtNewPassword,
                getString(R.string.enter_new_password), getWindow())){
            return;
        }
        if (!validatePasswordLength(txtNewPassword,
                getString(R.string.password_char_minimum), getWindow())){
            return;
        }
        if (!validateFieldsEmpty(txtConfirmPassword,
                getString(R.string.enter_confirm_password), getWindow())){
            return;
        }
        if (!validateConfirmPassword(txtNewPassword, txtConfirmPassword,
                getString(R.string.passwords_not_equal), getWindow())){
            return;
        }

        String changePassword = BASE_URL + ApiMethods.password + querySymbol + WebserviceKeys.token + "="
                + SpeedVtsPreferences.getStringValue(ChangePassword.this, token_key)+ andSymbol +
                WebserviceKeys.password + "=" + txtNewPassword.getText().toString();

        WebService.getInstance(ChangePassword.this).doRequestwithGET(ChangePassword.this, ApiMethods.password,
                changePassword, new HashMap<String, String>(), this, false);
    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);
        Toast.makeText(ChangePassword.this, "Password changed successfully", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {
        super.onErrorResponse(tag, responseCode, responseMsg);
    }
}
