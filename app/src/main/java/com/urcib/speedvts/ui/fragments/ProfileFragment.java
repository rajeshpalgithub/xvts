package com.urcib.speedvts.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.urcib.speedvts.R;
import com.urcib.speedvts.app.SpeedVtsFragmentBase;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.model.UserAccount;
import com.urcib.speedvts.ui.ChangePassword;
import com.urcib.speedvts.ui.SpeedVtsHome;
import com.urcib.speedvts.webservice.WebService;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 10/08/16
 *
 * Application class
 */
public class ProfileFragment extends SpeedVtsFragmentBase implements View.OnClickListener{

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_fragment, container, false);
        init();
        return rootView;
    }

    private void init(){
        rootView.findViewById(R.id.imgBtnChangePassword).setOnClickListener(this);
        String pingUrl = BASE_URL + ApiMethods.account + querySymbol + WebserviceKeys.token + "="
                + SpeedVtsPreferences.getStringValue(getActivity(), token_key);

        WebService.getInstance(getActivity()).doRequestwithGET(getActivity(), ApiMethods.ping,
                pingUrl, new HashMap<String, String>(), this, false);
    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);
        try {
            JSONObject jsonObject = new JSONObject(responseMsg);
            if (jsonObject.has(WebserviceKeys.account)){
                Gson gson = new Gson();
                UserAccount userAccount = gson.fromJson(getJsonObjectValueForString
                        (jsonObject, WebserviceKeys.account), UserAccount.class);
                if (userAccount!=null){
                    ((TextView)rootView.findViewById(R.id.lblName)).setText(userAccount.name);
                    ((TextView)rootView.findViewById(R.id.lblEmail)).setText(userAccount.email);
                    ((TextView)rootView.findViewById(R.id.lblPhoneNumber)).setText(userAccount.phone);
                }
            }
        }catch (Exception ex){

        }
    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {
        super.onErrorResponse(tag, responseCode, responseMsg);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgBtnChangePassword:
                showActivityNotFinished(getActivity(), ChangePassword.class);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getFragmentManager()!=null){
            int backStackCount = getFragmentManager().getBackStackEntryCount();
            String backStackName = getFragmentManager().getBackStackEntryAt(backStackCount-1).getName();
            logD(backStackName);
            if (backStackName.equalsIgnoreCase(LivePresenceFragment.class.getName())){
                ((SpeedVtsHome)getActivity()).setActionBarTitle("Profile");

            }
        }

        super.onBackStackChanged();
    }
}
