package com.urcib.speedvts.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.urcib.speedvts.R;
import com.urcib.speedvts.adapters.GeofenceListAdapter;
import com.urcib.speedvts.app.SpeedVtsFragmentBase;
import com.urcib.speedvts.helper.BundleKeys;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.model.MainGeofence;
import com.urcib.speedvts.model.SpeedVtsGeofence;
import com.urcib.speedvts.webservice.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 01/09/16
 * <p>
 * Application class
 */

public class GeofenceDetailView extends SpeedVtsFragmentBase implements View.OnClickListener {

    private View rootView;
    private RecyclerView recViewGeofenceList;
    private GeofenceListAdapter geofenceListAdapter;

    private final int ADD_GEOFENCE = 123;
    private SpeedVtsGeofence speedVtsGeofence;
    private SpeedVtsGeofence speedVtsGeofenceToRemove;

    private List<SpeedVtsGeofence> arrListSpeedGeofences = new ArrayList<SpeedVtsGeofence>();
    private int page = 1;
    private int record = 5;
    private SearchView searchView;

    private int editPosition = 0;
    private int deletePosition = 0;

    Bundle bundle;
    String geofenceId;

    private LinearLayout lnrRoot;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.geofence_detail_view, container, false);
        init();
        return rootView;
    }

    private void init() {
        bundle = this.getArguments();
        if (bundle != null) {
            geofenceId = bundle.getString(BundleKeys.geofence_details_id);
        }
        lnrRoot = (LinearLayout) rootView.findViewById(R.id.lnrRoot);
        getGeofenceListDetail(true);
    }

    private void getGeofenceListDetail(boolean loading) {
        String getGeofenceUrl = BASE_URL + ApiMethods.geofence + querySymbol +
                WebserviceKeys.token + equalSymbol + SpeedVtsPreferences.getStringValue(getActivity(), token_key)
                + andSymbol + WebserviceKeys.geofence_id + equalSymbol + geofenceId;
        logD(getGeofenceUrl);
        Log.v("geofence detail", getGeofenceUrl);
        String tag = ApiMethods.geofence + "/get";


        WebService.getInstance(getActivity()).doRequestwithGET(getActivity(), tag, getGeofenceUrl,
                new HashMap<String, String>(), this, loading);

    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);
        Log.v("geofence detail", responseMsg);
        try {

            Gson gson = new Gson();
            if (!TextUtils.isEmpty(responseMsg)) {
                lnrRoot.setVisibility(View.VISIBLE);

                MainGeofence speedVtsGeofence = gson.fromJson(responseMsg, MainGeofence.class);

                Log.v("geofence title", speedVtsGeofence.geofence.title);

                if (!TextUtils.isEmpty(speedVtsGeofence.geofence.title))
                    ((TextView) rootView.findViewById(R.id.lblName)).setText(speedVtsGeofence.geofence.title);

                if (!TextUtils.isEmpty(speedVtsGeofence.geofence.message))
                    ((TextView) rootView.findViewById(R.id.lblDescription)).setText(speedVtsGeofence.geofence.message);

                if (String.valueOf(speedVtsGeofence.geofence.radius) != null)
                    ((TextView) rootView.findViewById(R.id.lblRadius)).setText(String.valueOf(speedVtsGeofence.geofence.radius));

                if (String.valueOf(speedVtsGeofence.geofence.latitude) != null)
                    ((TextView) rootView.findViewById(R.id.lblLatitude)).setText(String.valueOf(speedVtsGeofence.geofence.latitude));

                if (String.valueOf(speedVtsGeofence.geofence.longitude) != null)
                ((TextView) rootView.findViewById(R.id.lblLongitude)).setText(String.valueOf(speedVtsGeofence.geofence.longitude));

                JSONObject jsonObject = new JSONObject(speedVtsGeofence.geofence.meta);


                if (!TextUtils.isEmpty(jsonObject.getString("country")))
                ((TextView) rootView.findViewById(R.id.lblCountry)).setText(jsonObject.getString("country"));

                if (!TextUtils.isEmpty(jsonObject.getString("locality")))
                    ((TextView) rootView.findViewById(R.id.lblLocality)).setText(jsonObject.getString("locality"));

                if (!TextUtils.isEmpty(jsonObject.getString("admin_area")))
                    ((TextView) rootView.findViewById(R.id.lblAdminArea)).setText(jsonObject.getString("admin_area"));

                if (!TextUtils.isEmpty(jsonObject.getString("sub_locality")))
                    ((TextView) rootView.findViewById(R.id.lblSubLocality)).setText(jsonObject.getString("sub_locality"));



            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {
        super.onErrorResponse(tag, responseCode, responseMsg);
        logD(responseMsg);
    }

    @Override
    public void onClick(View view) {

    }
}
