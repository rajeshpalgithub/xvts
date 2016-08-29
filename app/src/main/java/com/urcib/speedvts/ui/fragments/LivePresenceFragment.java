package com.urcib.speedvts.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.urcib.speedvts.R;
import com.urcib.speedvts.app.SpeedVtsFragmentBase;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.model.VehiclePosition;
import com.urcib.speedvts.ui.SpeedVtsHome;
import com.urcib.speedvts.webservice.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class LivePresenceFragment extends SpeedVtsFragmentBase implements OnMapReadyCallback,
        View.OnClickListener{

    private View rootView;

    private List<VehiclePosition> vehiclePositionArrayList;
    private Marker vehicleMarker;
    private List<VehiclePosition> vehicleCurrentMovingPositionArrayList;
    private Polyline polyline;

    private MapView mMapView;
    private GoogleMap mMap;

    private Timer timerPing;
    private LinearLayout lnrRoot;
    // Request code for the permissions
    private final int REQUEST_NEEDED_PERMISSION = 213;

    private ImageView imgMapType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.live_presence_fragment, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        init();
        return rootView;
    }

    private void init(){
        lnrRoot = (LinearLayout) rootView.findViewById(R.id.lnrRoot);
        rootView.findViewById(R.id.btnStatics).setOnClickListener(this);
        imgMapType = (ImageView) rootView.findViewById(R.id.imgMapType);
        imgMapType.setOnClickListener(this);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        if (haveInternet(getActivity())){
            String positionUrl = BASE_URL + ApiMethods.position + querySymbol + WebserviceKeys.token + "="
                    + SpeedVtsPreferences.getStringValue(getActivity(), token_key);

            WebService.getInstance(getActivity()).doRequestwithGET(getActivity(), ApiMethods.position,
                    positionUrl, new HashMap<String, String>(), this, false);
        }else {
            Snackbar.make(lnrRoot,"Internet is not connected.",Snackbar.LENGTH_LONG).show();
        }
    }


    private void ping(){
        String pingUrl = BASE_URL + ApiMethods.ping + querySymbol + WebserviceKeys.token + "="
                + SpeedVtsPreferences.getStringValue(getActivity(), token_key);

        WebService.getInstance(getActivity()).doRequestwithGET(getActivity(), ApiMethods.ping,
                pingUrl, new HashMap<String, String>(), this, false);
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be
     * used. This is where we can add markers or lines, add listeners or move the camera. In this
     * case, we just add a marker near Sydney, Australia. If Google Play services is not installed
     * on the device, the user will be prompted to install it inside the SupportMapFragment. This
     * method will only be triggered once the user has installed Google Play services and returned
     * to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        permissionChecking();
    }

    /**
     * Here we need to check the device SDK version, if that is equal or greater than Marshmallow
     * then we need to handle the permission enable dynamically.
     */
    private void permissionChecking() {
        // Check the device SDK version
        if (Build.VERSION.SDK_INT >= 23) {
            /**
             * This satisfies then the device is Marshmallow and more, we need to handle the
             * permission.
             * First we need to check the user is granted the required permission already.
             *
             * We need the following permissions
             * 1. ACCESS_FINE_LOCATION for getting device location
             *
             */
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED ) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Snackbar.make(lnrRoot, getString(R.string.permission_needed_message), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(android.R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Here request the permission again
                                    ActivityCompat.requestPermissions(getActivity(), new String[]
                                            {Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_NEEDED_PERMISSION);
                                }
                            }).show();
                } else {
                    // Here no need explanation, just request the permission
                    ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_NEEDED_PERMISSION);
                }
            } else {
                if (checkLocationEnabled(getActivity())){
                    mMap.setMyLocationEnabled(true);
                    if (haveInternet(getActivity())){
                        ping();
                        callAsynchronousTask();
                    }

                }

            }
        } else {
            if (checkLocationEnabled(getActivity())){
                mMap.setMyLocationEnabled(true);
                if (haveInternet(getActivity())){
                    ping();
                    callAsynchronousTask();
                }
            }
        }
    }

    /**
     * This will return when user allow or deny the requested permission
     *
     * @param requestCode  code used for requesting permission
     * @param permissions  requested permissions
     * @param grantResults granted results for requested permission in order by request order
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final List<String> ungrantedPermissions = new ArrayList<String>();
        switch (requestCode) {
            case REQUEST_NEEDED_PERMISSION:
                // Looping all permission and check the granted results for those
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        ungrantedPermissions.add(permissions[i]);
                    }
                }

                if (ungrantedPermissions.size() == 0) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    Log.e("Permission name", "Show the snackbar");
                    Snackbar.make(lnrRoot, getString(R.string.permission_needed_message), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(android.R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Here request the permission again
                                    String strUngrantedPermissions[] = new String[ungrantedPermissions.size()];
                                    ActivityCompat.requestPermissions(getActivity(), ungrantedPermissions.toArray(strUngrantedPermissions),
                                            REQUEST_NEEDED_PERMISSION);
                                }
                            }).show();
                }

                break;
            default:
                break;
        }
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        logD("onSharedPreferenceChanged: "+key);
//        if (key.equalsIgnoreCase(latest_position)){
//            try
//            {
//                JSONObject pingObj = new JSONObject(SpeedVtsPreferences.getStringValue(getActivity(),
//                        latest_position));
//                logD("pingObj: "+pingObj);
//                Gson gson = new Gson();
//                VehiclePosition vehiclePosition = gson.fromJson(pingObj.toString(), VehiclePosition.class);
//                if (vehiclePosition!=null && mMap!=null){
//                    placeMarker(vehiclePosition);
//                }
//            }catch(Exception ex){
//                ex.printStackTrace();
//            }
//        }
//    }

    private void placeMarker(VehiclePosition vehiclePosition) {
        double lat = Double.parseDouble(vehiclePosition.latitude);
        double lon = Double.parseDouble(vehiclePosition.longitude);
        LatLng newPosition = new LatLng(lat, lon);

        if (vehicleMarker!=null){
            vehicleMarker.setPosition(newPosition);
        }else {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(newPosition);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_red));
            if (vehiclePosition.marker == 2){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_green));
            }
            markerOptions.title(vehiclePosition.device_name);
            vehicleMarker = mMap.addMarker(markerOptions);
        }

        if (polyline!=null){
            List<LatLng> points = polyline.getPoints();
            if (!points.contains(newPosition))
                points.add(newPosition);
            polyline.setPoints(points);
        }

        // Construct a CameraPosition focusing on Mountain VigetActivity()ew and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(newPosition)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    private void drawRoute(){
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int z = 0; z < vehiclePositionArrayList.size(); z++) {
            VehiclePosition vehiclePosition = vehiclePositionArrayList.get(z);
            double lat = Double.parseDouble(vehiclePosition.latitude);
            double lon = Double.parseDouble(vehiclePosition.longitude);
            LatLng newPosition = new LatLng(lat, lon);
            options.add(newPosition);
        }
        polyline = mMap.addPolyline(options);
        logD("Polyline added");
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        timerPing = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            ping();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timerPing.schedule(doAsynchronousTask, 0, 10000); //execute in every 50000 ms
    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);
        if (tag.equalsIgnoreCase(ApiMethods.ping)){
            try {
                JSONObject responseJsObj = new JSONObject(responseMsg);
                if (responseJsObj.has(WebserviceKeys.ping)){
                    Gson gson = new Gson();
                    VehiclePosition vehiclePosition = gson.fromJson(getJsonObjectValueForString
                            (responseJsObj, WebserviceKeys.ping), VehiclePosition.class);
                    SpeedVtsPreferences.setStringValue(getActivity(), SpeedVtsPreferences.PreferenceKeys.latest_position,
                            gson.toJson(vehiclePosition));
                    if (mMap!=null){
                        placeMarker(vehiclePosition);
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
//        else if (tag.equalsIgnoreCase(ApiMethods.position)){
//            try {
//                JSONObject responseJsObj = new JSONObject(responseMsg);
//                if (responseJsObj.has(WebserviceKeys.ping)){
//                    JSONArray pingJsArray = responseJsObj.getJSONArray(WebserviceKeys.ping);
//                    if (pingJsArray!=null && pingJsArray.length()>0){
//                        vehiclePositionArrayList = new ArrayList<VehiclePosition>();
//                        Gson gson = new Gson();
//                        vehiclePositionArrayList = Arrays.asList(gson.fromJson(pingJsArray.toString(),
//                                VehiclePosition[].class));
//                        if (mMap!=null && vehiclePositionArrayList!=null && vehiclePositionArrayList.size()>0){
//                            drawRoute();
//                        }
//                    }
//                }
//            }catch (Exception ex){
//                ex.printStackTrace();
//            }
//        }

    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {
        super.onErrorResponse(tag, responseCode, responseMsg);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (timerPing!=null){
            timerPing.cancel();
            timerPing.purge();
        }
//        getActivity().getSharedPreferences(SpeedVtsPreferences.KEY, Context.MODE_PRIVATE).
//                registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        callAsynchronousTask();
//        getActivity().getSharedPreferences(SpeedVtsPreferences.KEY, Context.MODE_PRIVATE).
//                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (timerPing!=null){
            timerPing.cancel();
            timerPing.purge();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnMap:
                break;
            case R.id.btnStatics:
                Snackbar.make(lnrRoot, "Page under development", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.imgMapType:
                if (mMap!=null){
                    if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        imgMapType.setImageResource(R.drawable.ic_map_white_24dp);
                    }else{
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        imgMapType.setImageResource(R.drawable.ic_satellite_white_24dp);
                    }
                }
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
                ((SpeedVtsHome)getActivity()).setActionBarTitle("Live Presence");

            }

        }
        super.onBackStackChanged();
    }
}
