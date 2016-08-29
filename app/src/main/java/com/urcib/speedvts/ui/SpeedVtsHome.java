package com.urcib.speedvts.ui;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.api.Api;
import com.google.gson.Gson;
import com.urcib.speedvts.R;
import com.urcib.speedvts.app.SpeedVtsAppCombatBase;
import com.urcib.speedvts.helper.SpeedVtsGeofenceController;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.model.VehiclePosition;
import com.urcib.speedvts.ui.fragments.GeoFenceList;
import com.urcib.speedvts.ui.fragments.LivePresenceFragment;
import com.urcib.speedvts.ui.fragments.ProfileFragment;
import com.urcib.speedvts.webservice.RequestListener;
import com.urcib.speedvts.webservice.WebService;

import org.json.JSONObject;

import java.util.HashMap;
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
public class SpeedVtsHome extends SpeedVtsAppCombatBase implements
        NavigationView.OnNavigationItemSelectedListener{


    // Navigation view for slidemenu
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speedvts_home);

        init();
    }

    private void init(){
        SpeedVtsGeofenceController.getInstance().init(SpeedVtsHome.this);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        mNavigationView.setNavigationItemSelectedListener(this);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        switchFrags(new LivePresenceFragment(), "Live Presence", true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_search, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);

    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {
        super.onErrorResponse(tag, responseCode, responseMsg);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        logD("Timer Cancelled");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                mDrawerLayout.openDrawer(GravityCompat.START);
            }else {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.menuMyAccount:
                switchFrags(new ProfileFragment(), "Profile", true);
                break;
            case R.id.menuLogout:
                new AlertDialog.Builder(SpeedVtsHome.this)
                        .setMessage("Are you sure want to logout?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                SpeedVtsGeofenceController.getInstance().removeAllGeofences(geofenceControllerListener);
                                SpeedVtsPreferences.clearPreferences(SpeedVtsHome.this);
                                showActivity(SpeedVtsHome.this, LoginScreen.class);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.menuLivePresence:
                switchFrags(new LivePresenceFragment(), "Live Presence", true);
                break;
            case R.id.menuGeoFencing:
                switchFrags(new GeoFenceList(), "Geo Fence", true);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount()>1){
            super.onBackPressed();
        }else{
            finish();
        }
    }

    public void setActionBarTitle(String title){
        setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private SpeedVtsGeofenceController.SpeedVtsGeofenceControllerListener
            geofenceControllerListener = new SpeedVtsGeofenceController.SpeedVtsGeofenceControllerListener() {
        @Override
        public void onGeofencesUpdated() {

        }

        @Override
        public void onError() {
        }
    };

    BroadcastReceiver internetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (haveInternet(context)){

            }
        }
    };
}
