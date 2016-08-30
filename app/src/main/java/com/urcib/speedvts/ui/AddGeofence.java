package com.urcib.speedvts.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.urcib.speedvts.R;
import com.urcib.speedvts.app.SpeedVtsAppCombatBase;
import com.urcib.speedvts.helper.BundleKeys;
import com.urcib.speedvts.helper.SpeedVtsConstants;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.model.SpeedVtsGeofence;
import com.urcib.speedvts.model.VehiclePosition;
import com.urcib.speedvts.webservice.WebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 16/08/16
 *
 * Add description about the class
 */
public class AddGeofence extends SpeedVtsAppCombatBase implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, View.OnClickListener, LocationListener {

    private MapView mMapView;
    private GoogleMap mMap;

    private CoordinatorLayout main_content;
    private LinearLayout lnrAddContent;
    private ImageView imgPull;

    private EditText txtName, txtDescription, txtLatitude, txtLongitude, txtRadius;
    private Marker markerGeoFence;

    private BottomSheetBehavior mBottomSheetBehavior;
    private LatLng latLngGeoFence;

    private SpeedVtsGeofence speedVtsGeofenceEdit;
    private RadioGroup rgbEnter;

    private LocationManager locationManager;
    private long MIN_TIME = 400;
    private float MIN_DISTANCE = 1000;

    int position = 0;

    ImageView imgMapType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_geo_fence);

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();
        init();
    }

    private void init() {

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Add Geofence");

        mMapView.getMapAsync(this);
        main_content = (CoordinatorLayout) findViewById(R.id.main_content);

        lnrAddContent = (LinearLayout) findViewById(R.id.lnrAddContent);
        imgPull = (ImageView) findViewById(R.id.imgPull);
        findViewById(R.id.lnrPull).setOnClickListener(this);
        findViewById(R.id.btnAddGeoFence).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        imgMapType = (ImageView) findViewById(R.id.imgMapType);
        imgMapType.setOnClickListener(this);

        txtName = (EditText) findViewById(R.id.txtGeoFenceName);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        txtLatitude = (EditText) findViewById(R.id.txtLatitude);
        txtLongitude = (EditText) findViewById(R.id.txtLongitude);
        txtRadius = (EditText) findViewById(R.id.txtRadius);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        rgbEnter = (RadioGroup) findViewById(R.id.rgNotify);

        View bottomSheet = findViewById(R.id.bottom_sheet);

        if (bottomSheet != null) {
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(BundleKeys.geofence_details)) {
                speedVtsGeofenceEdit = (SpeedVtsGeofence) extras.getSerializable(BundleKeys.geofence_details);
                txtRadius.setEnabled(false);
                txtName.setText(speedVtsGeofenceEdit.title);
                txtLatitude.setText("" + speedVtsGeofenceEdit.latitude);
                txtLongitude.setText("" + speedVtsGeofenceEdit.longitude);
                txtDescription.setText(speedVtsGeofenceEdit.message);
                txtRadius.setText("" + speedVtsGeofenceEdit.radius);
                ((Button) findViewById(R.id.btnAddGeoFence)).setText(getString(R.string.edit_geofence));
                rgbEnter.setEnabled(false);
                rgbEnter.setClickable(false);

                if (speedVtsGeofenceEdit.whenEnter){
                    ((RadioButton)findViewById(R.id.rdbtnWhenEnter)).setChecked(true);
                }else{
                    ((RadioButton)findViewById(R.id.rdbtnWhenExit)).setChecked(true);
                }
                ((RadioButton)findViewById(R.id.rdbtnWhenEnter)).setEnabled(false);
                ((RadioButton)findViewById(R.id.rdbtnWhenEnter)).setClickable(false);
                ((RadioButton)findViewById(R.id.rdbtnWhenExit)).setEnabled(false);
                ((RadioButton)findViewById(R.id.rdbtnWhenExit)).setClickable(false);
                getSupportActionBar().setTitle("Edit Geofence");
            }
            if (extras.containsKey(BundleKeys.position)){
                position = extras.getInt(BundleKeys.position);
            }
        }else{
            Snackbar.make(main_content, "Long press on a position in the map to add a Geo Fence.",
                    Snackbar.LENGTH_INDEFINITE).setAction("Okay", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).show();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
               finish();
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setMyLocationEnabled(true);
        if (speedVtsGeofenceEdit == null) {
            mMap.setOnMapLongClickListener(this);

            if (Build.VERSION.SDK_INT >=23){
                if (ContextCompat.checkSelfPermission(AddGeofence.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                }
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            }

            String pingUrl = BASE_URL + ApiMethods.ping + querySymbol + WebserviceKeys.token + "="
                    + SpeedVtsPreferences.getStringValue(AddGeofence.this, token_key);

            WebService.getInstance(AddGeofence.this).doRequestwithGET(AddGeofence.this, ApiMethods.ping,
                    pingUrl, new HashMap<String, String>(), this, false);
        }else{
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(speedVtsGeofenceEdit.latitude, speedVtsGeofenceEdit.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                    .title(speedVtsGeofenceEdit.title));
            // Construct a CameraPosition focusing on Mountain VigetActivity()ew and animate the camera to that position.
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(speedVtsGeofenceEdit.latitude, speedVtsGeofenceEdit.longitude))      // Sets the center of the map to Mountain View
                    .zoom(14)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(speedVtsGeofenceEdit.latitude, speedVtsGeofenceEdit.longitude))
                    .radius(speedVtsGeofenceEdit.radius)
                    .fillColor(Color.parseColor("#8F64FFDA"))
                    .strokeColor(Color.parseColor("#00897B"))
                    .strokeWidth(8);
            mMap.addCircle(circleOptions);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (speedVtsGeofenceEdit == null){
            mMap.clear();
            latLngGeoFence = latLng;
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_red))
                    .title("Geo Fencing"));
            txtLatitude.setText(""+latLng.latitude);
            txtLongitude.setText(""+latLng.longitude);
            imgPull.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24dp);
            lnrAddContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lnrPull:
                if (speedVtsGeofenceEdit == null && latLngGeoFence==null){
                    Snackbar.make(main_content, "Long press in the map to add a Geo Fence.",
                            Snackbar.LENGTH_INDEFINITE).show();
                    return;
                }
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.btnCancel:
                if (speedVtsGeofenceEdit == null) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    mMap.clear();
                    txtLatitude.setText("");
                    txtLongitude.setText("");
                    findViewById(R.id.lnrPull).performClick();
                    Snackbar.make(main_content, "Long press on a new position in the map to add a Geo Fence.",
                            Snackbar.LENGTH_INDEFINITE).setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }else {
                    finish();
                }

                break;
            case R.id.btnAddGeoFence:
                addFence();
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

    private void addFence(){
        if (!haveInternet(AddGeofence.this)){
            Snackbar.make(main_content,"Internet is not connected.", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!validateFieldsEmpty(txtName, "Please enter geo fence title", main_content, getWindow()))
            return;
        if (!validateFieldsEmpty(txtDescription, "Please enter description", main_content, getWindow()))
            return;
        if (!validateFieldsEmpty(txtLatitude, "Please enter Latitude", main_content, getWindow()))
            return;
        if (!validateFieldsEmpty(txtLongitude, "Please enter Longitude", main_content, getWindow()))
            return;
        if (!validateFieldsEmpty(txtRadius, "Please enter Longitude", main_content, getWindow()))
            return;

        double latitude = Double.parseDouble(txtLatitude.getText().toString().trim());
        double longitude = Double.parseDouble(txtLongitude.getText().toString().trim());

        if ((latitude < SpeedVtsConstants.MinLatitude || latitude > SpeedVtsConstants.MaxLatitude)
                || (longitude < SpeedVtsConstants.MinLongitude || longitude > SpeedVtsConstants.MaxLongitude)
               ) {
            Snackbar.make(main_content,"Please enter proper co-ordinates.", Snackbar.LENGTH_LONG).show();
            return;
        }

        RadioGroup rdbGroup = (RadioGroup) findViewById(R.id.rgNotify);

        if (rdbGroup.getCheckedRadioButtonId()==0){
            Snackbar.make(main_content,"Please select notify option", Snackbar.LENGTH_LONG).show();
            return;
        }


        String strTitle = txtName.getText().toString();
        String strDescription = txtDescription.getText().toString();
        String strLatitude = txtLatitude.toString();
        String strLongitude = txtLongitude.getText().toString();
        String strRadius = txtRadius.getText().toString();


        try
        {
            float radius = Float.parseFloat(strRadius);
            Intent intent = new Intent();
            if (speedVtsGeofenceEdit==null){
                SpeedVtsGeofence geofence = new SpeedVtsGeofence();
                geofence.title = strTitle;
                geofence.message = strDescription;
                geofence.latitude = latLngGeoFence.latitude;
                geofence.longitude = latLngGeoFence.longitude;
                geofence.radius = radius;
                if (rdbGroup.getCheckedRadioButtonId() == R.id.rdbtnWhenEnter){
                    geofence.alert_when = "IN";
                    geofence.whenEnter = true;
                }else  if (rdbGroup.getCheckedRadioButtonId() == R.id.rdbtnWhenEnter){
                    geofence.alert_when = "OUT";
                    geofence.whenEnter = false;
                }
                String meta = getAddress(latLngGeoFence.latitude, latLngGeoFence.longitude);
                if (meta!=null){
                    geofence.meta = getAddress(latLngGeoFence.latitude, latLngGeoFence.longitude);
                }
                intent.putExtra(BundleKeys.geofence_details, geofence);
            }else {
                if (rdbGroup.getCheckedRadioButtonId() == R.id.rdbtnWhenEnter){
                    speedVtsGeofenceEdit.alert_when = "IN";
                    speedVtsGeofenceEdit.whenEnter = true;
                }else  if (rdbGroup.getCheckedRadioButtonId() == R.id.rdbtnWhenEnter){
                    speedVtsGeofenceEdit.alert_when = "OUT";
                    speedVtsGeofenceEdit.whenEnter = false;
                }
                speedVtsGeofenceEdit.title = strTitle;
                speedVtsGeofenceEdit.message = strDescription;
                intent.putExtra(BundleKeys.position, position);
                intent.putExtra(BundleKeys.geofence_details, speedVtsGeofenceEdit);
            }
            setResult(RESULT_OK, intent);
            finish();

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private String getAddress(double latitude, double longitude) {
        String result = "";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                JSONObject jsonObject = new JSONObject();
                if (address.getAdminArea()!=null)
                    jsonObject.put("admin_area", address.getAdminArea());
                if (address.getLocality()!=null)
                    jsonObject.put("locality", address.getLocality());
                if (address.getSubLocality()!=null)
                    jsonObject.put("sub_locality", address.getSubLocality());
                if (address.getSubAdminArea()!=null)
                    jsonObject.put("sub_admin_area", address.getSubAdminArea());
                if (address.getCountryName()!=null)
                    jsonObject.put("country", address.getCountryName());
                Gson gson = new Gson();
//                result = gson.toJson(address);
                result = jsonObject.toString();
            }
        } catch (IOException e) {
            logD(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    @Override
    public void onLocationChanged(Location location) {
        getMyLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void getMyLocation(Location location) {

        if (mMap!=null && speedVtsGeofenceEdit==null){
            if (location!=null){
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                mMap.animateCamera(cameraUpdate);
                locationManager.removeUpdates(this);
            }else{

            }

        }
    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);
        try {
            JSONObject responseJsObj = new JSONObject(responseMsg);
            if (responseJsObj.has(WebserviceKeys.ping)){
                Gson gson = new Gson();
                VehiclePosition vehiclePosition = gson.fromJson(getJsonObjectValueForString
                        (responseJsObj, WebserviceKeys.ping), VehiclePosition.class);
                if (mMap!=null){
                    double lat = Double.parseDouble(vehiclePosition.latitude);
                    double lon = Double.parseDouble(vehiclePosition.longitude);
                    LatLng newPosition = new LatLng(lat, lon);
                    // Construct a CameraPosition focusing on Mountain VigetActivity()ew and animate the camera to that position.
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(newPosition)      // Sets the center of the map to Mountain View
                            .zoom(17)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
