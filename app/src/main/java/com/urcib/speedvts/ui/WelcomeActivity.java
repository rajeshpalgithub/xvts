package com.urcib.speedvts.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.urcib.speedvts.R;
import com.urcib.speedvts.app.SpeedVtsAppCombatBase;
import com.urcib.speedvts.helper.SpeedVtsGeofenceController;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.model.SpeedVtsGeofence;
import com.urcib.speedvts.webservice.RequestListener;
import com.urcib.speedvts.webservice.WebService;
import com.urcib.speedvts.webservice.api.WebserviceConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */

public class WelcomeActivity extends Activity implements RequestListener, WebserviceConstants,
        SpeedVtsPreferences.PreferenceKeys{

    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.welcome_screen);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3};

        addBottomDots(0);
        changeStatusBarColor();

        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    getGeofenceList();

                }
            }
        });
    }

    private void launchHomeScreen(){
        Intent intent = new Intent(WelcomeActivity.this, SpeedVtsHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        SpeedVtsPreferences.setBooleanValue(WelcomeActivity.this,
                SpeedVtsPreferences.PreferenceKeys.IS_FIRST_TIME_LAUNCH, true);
        finish();
//        showActivity(WelcomeActivity.this, SpeedVtsHome.class);
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        try {
            JSONObject jsObj = new JSONObject(responseMsg);
            if (jsObj.has(WebserviceKeys.geofence)) {
                JSONArray geoFenceArray = jsObj.getJSONArray(WebserviceKeys.geofence);
                Gson gson = new Gson();
                List<SpeedVtsGeofence> speedVtsGeofenceListToAdd = new ArrayList<SpeedVtsGeofence>();
                for (int i=0;i<geoFenceArray.length();i++) {
                    SpeedVtsGeofence geofenceFromAPi = gson.fromJson(geoFenceArray.get(i).toString(),
                            SpeedVtsGeofence.class);
                    if (!checkFenceAdded(geofenceFromAPi)){
                        speedVtsGeofenceListToAdd.add(geofenceFromAPi);
                    }
                }

                if (speedVtsGeofenceListToAdd.size()>0 && tag.equalsIgnoreCase(ApiMethods.geofence+"/get"))
                    SpeedVtsGeofenceController.getInstance().addGeofence(speedVtsGeofenceListToAdd,
                            geofenceControllerListener);
            }

        }catch (Exception ex){

        }

        launchHomeScreen();
    }

    private SpeedVtsGeofenceController.SpeedVtsGeofenceControllerListener
            geofenceControllerListener = new SpeedVtsGeofenceController.SpeedVtsGeofenceControllerListener() {
        @Override
        public void onGeofencesUpdated() {
            Log.d("geo", "onGeofencesUpdated() called with: " + "");
        }

        @Override
        public void onError() {
        }
    };

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {

    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    private void getGeofenceList(){
        String getGeofenceUrl = BASE_URL + WebserviceConstants.ApiMethods.geofence + querySymbol +
                WebserviceConstants.WebserviceKeys.token + equalSymbol +
                SpeedVtsPreferences.getStringValue(WelcomeActivity.this , token_key);
        String tag =  WebserviceConstants.ApiMethods.geofence+"/get";
        WebService.getInstance(this).doRequestwithGET(this, tag, getGeofenceUrl,
                new HashMap<String, String>(), this, true);
    }

    private boolean checkFenceAdded(SpeedVtsGeofence speedVtsGeofence){
        List<SpeedVtsGeofence> speedVtsGeofenceList = SpeedVtsGeofenceController.getInstance().getSpeedVtsGeofences();
        for (int i=0; i<speedVtsGeofenceList.size(); i++){
            if (speedVtsGeofence.id.equalsIgnoreCase(speedVtsGeofenceList.get(i).id)){
                return true;
            }
        }
        return false;
    }
}
