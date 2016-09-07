package com.urcib.speedvts.ui.fragments;


import android.graphics.Color;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class GeofenceDetailView extends SpeedVtsFragmentBase implements View.OnClickListener, OnMapReadyCallback {

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

    private MapView mMapView;
    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.geofence_detail_view, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();
        init();
        return rootView;
    }

    private void init() {
        bundle = this.getArguments();
        if (bundle != null) {
            geofenceId = bundle.getString(BundleKeys.geofence_details_id);
        }
        mMapView.getMapAsync(this);
        lnrRoot = (LinearLayout) rootView.findViewById(R.id.lnrRoot);

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


                if (jsonObject.has("country"))
                ((TextView) rootView.findViewById(R.id.lblCountry)).setText(jsonObject.getString("country"));

                if (jsonObject.has("locality"))
                    ((TextView) rootView.findViewById(R.id.lblLocality)).setText(jsonObject.getString("locality"));

                if (jsonObject.has("admin_area"))
                    ((TextView) rootView.findViewById(R.id.lblAdminArea)).setText(jsonObject.getString("admin_area"));

                if (jsonObject.has("sub_locality"))
                    ((TextView) rootView.findViewById(R.id.lblSubLocality)).setText(jsonObject.getString("sub_locality"));

                if (mMap!=null){
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(speedVtsGeofence.geofence.latitude, speedVtsGeofence.geofence.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_red))
                            .title(speedVtsGeofence.geofence.title));

                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(speedVtsGeofence.geofence.latitude, speedVtsGeofence.geofence.longitude))
                            .radius(speedVtsGeofence.geofence.radius)
                            .fillColor(Color.parseColor("#8F64FFDA"))
                            .strokeColor(Color.parseColor("#00897B"))
                            .strokeWidth(8);
                    mMap.addCircle(circleOptions);

                    // Construct a CameraPosition focusing on Mountain VigetActivity()ew and animate the camera to that position.
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(speedVtsGeofence.geofence.latitude, speedVtsGeofence.geofence.longitude))      // Sets the center of the map to Mountain View
                            .zoom(12)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    mMapView.setFocusable(false);
                    mMapView.setClickable(false);
                    mMapView.setEnabled(false);
                }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setMyLocationEnabled(true);
        getGeofenceListDetail(true);

    }
}
