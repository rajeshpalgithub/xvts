package com.urcib.speedvts.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.urcib.speedvts.R;
import com.urcib.speedvts.adapters.GeofenceListAdapter;
import com.urcib.speedvts.app.SpeedVtsFragmentBase;
import com.urcib.speedvts.helper.BundleKeys;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.model.SpeedVtsGeofence;
import com.urcib.speedvts.webservice.WebService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 01/09/16
 *
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.geofence_detail_view,container,false);
        setHasOptionsMenu(true);
        init();
        return rootView;
    }

    private void init() {
        bundle = this.getArguments();
        if(bundle!=null){
            geofenceId = bundle.getString(BundleKeys.geofence_details_id);
        }
        getGeofenceListDetail(true);
    }

    private void getGeofenceListDetail(boolean loading) {
        String getGeofenceUrl = BASE_URL + ApiMethods.geofence + querySymbol +
                WebserviceKeys.token + equalSymbol + SpeedVtsPreferences.getStringValue(getActivity() , token_key)
                +andSymbol + WebserviceKeys.id +equalSymbol + geofenceId ;
        logD(getGeofenceUrl);
        Log.v("geofence detail",getGeofenceUrl);
        String tag =  ApiMethods.geofence+"/get";


        WebService.getInstance(getActivity()).doRequestwithGET(getActivity(), tag, getGeofenceUrl,
                new HashMap<String, String>(), this, loading);

    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);
        Log.v("geofence detail",responseMsg);

    }

    @Override
    public void onClick(View view) {

    }
}
