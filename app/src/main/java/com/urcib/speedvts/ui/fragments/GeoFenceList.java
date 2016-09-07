package com.urcib.speedvts.ui.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Switch;

import com.google.android.gms.common.api.Api;
import com.google.gson.Gson;
import com.urcib.speedvts.R;
import com.urcib.speedvts.adapters.GeofenceListAdapter;
import com.urcib.speedvts.adapters.GeofenceListViewAdapter;
import com.urcib.speedvts.adapters.OnLoadMoreListener;
import com.urcib.speedvts.app.SpeedVtsFragmentBase;
import com.urcib.speedvts.helper.BundleKeys;
import com.urcib.speedvts.helper.SpeedVtsGeofenceController;
import com.urcib.speedvts.helper.SpeedVtsPreferences;
import com.urcib.speedvts.model.SpeedVtsGeofence;
import com.urcib.speedvts.ui.AddGeofence;
import com.urcib.speedvts.ui.LoginScreen;
import com.urcib.speedvts.webservice.WebService;
import com.urcib.speedvts.widgets.LoadMoreListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 15/08/16
 * <p>
 * Application class
 */
public class GeoFenceList extends SpeedVtsFragmentBase implements View.OnClickListener {

    private View rootView;


    private final int ADD_GEOFENCE = 123;
    private SpeedVtsGeofence speedVtsGeofence;
    private SpeedVtsGeofence speedVtsGeofenceToRemove;

    private List<SpeedVtsGeofence> arrListSpeedGeofences = new ArrayList<SpeedVtsGeofence>();
    private int page = 1;
    private int record = 5;
    private SearchView searchView;

    private int editPosition = 0;
    private int deletePosition = 0;

    private FrameLayout lnrFrameContainer;

    private LoadMoreListView loadMoreListView;
    int limit = 10;
    int offset = 0;
    boolean loadingMore = false;
    private GeofenceListViewAdapter geofenceListViewAdapter;

    String searchText = "";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.geofence_list, container, false);
        setHasOptionsMenu(true);
        init();
        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                page = 1;
                arrListSpeedGeofences.clear();
                notifyToAdapter();
                searchText = query;
                getGeofenceList(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                page = 1;
                arrListSpeedGeofences.clear();
                notifyToAdapter();
                searchText = "";
                getGeofenceList(true);
                return false;
            }
        });

    }

    private void init() {
        lnrFrameContainer = (FrameLayout) rootView.findViewById(R.id.lnrFrameContainer);
        getGeofenceList(true);

        loadMoreListView = (LoadMoreListView) rootView.findViewById(R.id.loadMoreListView);
        loadMoreListView.setFocusable(false);

        loadMoreListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadingMore = true;

                if (loadingMore) {
                    page = page + 1;
                    getGeofenceList(false);

                }
            }
        });

        rootView.findViewById(R.id.fabAddGeoFence).setOnClickListener(this);
    }

    private void getGeofenceList(boolean loading) {
        String getGeofenceUrl = BASE_URL + ApiMethods.geofence + querySymbol +
                WebserviceKeys.token + equalSymbol + SpeedVtsPreferences.getStringValue(getActivity(), token_key)
                + andSymbol + WebserviceKeys.page + equalSymbol + page + andSymbol
                + WebserviceKeys.record + equalSymbol + record;
        logD(getGeofenceUrl);
        String tag = ApiMethods.geofence + "/get";
        if (searchText != null && searchText.length() > 0) {
            tag = ApiMethods.geofence + "/search";
            getGeofenceUrl = getGeofenceUrl + andSymbol + WebserviceKeys.search_title + equalSymbol + searchText;
        }

        WebService.getInstance(getActivity()).doRequestwithGET(getActivity(), tag, getGeofenceUrl,
                new HashMap<String, String>(), this, loading);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.fabAddGeoFence:
                Intent intentAddFence = new Intent(getActivity(), AddGeofence.class);
                startActivityForResult(intentAddFence, ADD_GEOFENCE);
                break;
            case R.id.btnEdit:
                SpeedVtsGeofence speedVtsGeofence = (SpeedVtsGeofence) view.getTag(R.id.lblRadius);
                int position = (int) view.getTag(R.id.lblName);
                Intent intentEditFence = new Intent(getActivity(), AddGeofence.class);
                intentEditFence.putExtra(BundleKeys.geofence_details, speedVtsGeofence);
                intentEditFence.putExtra(BundleKeys.position, position);
                startActivityForResult(intentEditFence, ADD_GEOFENCE);
                break;
            case R.id.btnDelete:
                new AlertDialog.Builder(getActivity())
                        .setMessage("Are you sure want to delete this geofence?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                speedVtsGeofenceToRemove = (SpeedVtsGeofence) view.getTag(R.id.lblRadius);
                                deletePosition = (int) view.getTag(R.id.lblName);
                                HashMap<String, String> params = new HashMap<String, String>();
                                String tag = ApiMethods.geofence + "/delete";
                                params.put(WebserviceKeys.token, SpeedVtsPreferences.getStringValue(getActivity(), token_key));
                                params.put(WebserviceKeys.id, speedVtsGeofenceToRemove.id);
                                String deleteGeoFenceUrl = BASE_URL + ApiMethods.geofence;

                                logD(params + "");

                                WebService.getInstance(getActivity()).doRequestwithDELETE(getActivity(),
                                        tag,
                                        deleteGeoFenceUrl, params, GeoFenceList.this, true);
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
            case R.id.lnrGeofenceList:
                SpeedVtsGeofence vtsGeofence = (SpeedVtsGeofence) view.getTag();
                GeofenceDetailView geofenceDetailViewfragment = new GeofenceDetailView();
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.geofence_details_id, vtsGeofence.id);
                geofenceDetailViewfragment.setArguments(bundle);
                switchFrags(geofenceDetailViewfragment,"Geo Fence Details",true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ADD_GEOFENCE:

                    if (data == null)
                        return;

                    Bundle extras = data.getExtras();
                    if (extras == null)
                        return;

                    if (extras.containsKey(BundleKeys.geofence_details)) {

                        SpeedVtsGeofence geofence = (SpeedVtsGeofence) extras.getSerializable(BundleKeys.geofence_details);

                        logD(extras + "");
                        logD(new Gson().toJson(geofence));

                        geofence.alert_when = "IN";
                        if (!geofence.whenEnter)
                            geofence.alert_when = "OUT";

                        HashMap<String, String> params = new HashMap<String, String>();
                        String tag = ApiMethods.geofence + "/insert";
                        params.put(WebserviceKeys.token, SpeedVtsPreferences.getStringValue(getActivity(), token_key));
                        params.put(WebserviceKeys.title, geofence.title);
                        params.put(WebserviceKeys.message, geofence.message);
//                        params.put(WebserviceKeys.meta, "");
                        if (geofence.meta != null)
                            params.put(WebserviceKeys.meta, geofence.meta);

                        String insertGeoFenceUrl = BASE_URL + ApiMethods.geofence;
                        if (geofence.id != null && geofence.id.length() > 0) {
                            params.put(WebserviceKeys.id, "" + geofence.id);
                            tag = ApiMethods.geofence + "/update";
                            editPosition = extras.getInt(BundleKeys.position);
                            WebService.getInstance(getActivity()).doRequestwithPUT(getActivity(),
                                    tag,
                                    insertGeoFenceUrl, params, this, true);
                        } else {
                            params.put(WebserviceKeys.radius, "" + geofence.radius);
                            params.put(WebserviceKeys.latitude, "" + geofence.latitude);
                            params.put(WebserviceKeys.longitude, "" + geofence.longitude);
                            params.put(WebserviceKeys.alert_when, geofence.alert_when);
                            params.put(WebserviceKeys.unit, "M");

                            logD(params + "");

                            WebService.getInstance(getActivity()).doRequestwithPOST(getActivity(),
                                    tag,
                                    insertGeoFenceUrl, params, this, true);
                        }
                        speedVtsGeofence = geofence;
                    }

                    break;
                default:
                    break;
            }
        }
    }

    private SpeedVtsGeofenceController.SpeedVtsGeofenceControllerListener
            geofenceControllerListener = new SpeedVtsGeofenceController.SpeedVtsGeofenceControllerListener() {
        @Override
        public void onGeofencesUpdated() {
            Log.d("geo", "onGeofencesUpdated() called with: " + "");
            notifyToAdapter();
        }

        @Override
        public void onError() {
        }
    };

    private void notifyToAdapter() {
        if (geofenceListViewAdapter!=null){
            geofenceListViewAdapter.notifyDataSetChanged();

            if (geofenceListViewAdapter.getCount() > 0) {
                rootView.findViewById(R.id.lblNoGeofence).setVisibility(View.GONE);
            } else {
                rootView.findViewById(R.id.lblNoGeofence).setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onSuccessResponse(String tag, int responseCode, String responseMsg) {
        super.onSuccessResponse(tag, responseCode, responseMsg);
        try {
            logD(tag);
            JSONObject jsObj = new JSONObject(responseMsg);
            if (tag.equalsIgnoreCase(ApiMethods.geofence + "/insert")) {
                if (jsObj.has(WebserviceKeys.insertId)) {
                    String id = getJsonObjectValueForString(jsObj, WebserviceKeys.insertId);
                    speedVtsGeofence.id = id;
                }
                List<SpeedVtsGeofence> speedVtsGeofenceListToAdd = new ArrayList<SpeedVtsGeofence>();
                speedVtsGeofenceListToAdd.add(speedVtsGeofence);
                arrListSpeedGeofences.add(speedVtsGeofence);
                geofenceListViewAdapter.notifyDataSetChanged();
                loadMoreListView.onLoadMoreComplete();
                SpeedVtsGeofenceController.getInstance().addGeofence(speedVtsGeofenceListToAdd,
                        geofenceControllerListener);
                Snackbar.make(lnrFrameContainer, "\"" + speedVtsGeofence.title + "\" Geofence added successfully.", Snackbar.LENGTH_LONG).show();
            } else if (tag.equalsIgnoreCase(ApiMethods.geofence + "/update")) {
                if (speedVtsGeofence.id != null && speedVtsGeofence.id.length() > 0) {
                    List<SpeedVtsGeofence> speedVtsGeofenceList =
                            SpeedVtsGeofenceController.getInstance().getSpeedVtsGeofences();
                    for (int i = 0; i < speedVtsGeofenceList.size(); i++) {
                        if (speedVtsGeofence.id.equalsIgnoreCase(speedVtsGeofenceList.get(i).id)) {
                            Gson gson = new Gson();
                            String json = gson.toJson(speedVtsGeofence);
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences
                                    (SpeedVtsPreferences.KEY_GEOFENCE, Context.MODE_PRIVATE).edit();
                            logD(json);
                            editor.putString(speedVtsGeofence.geofenceId, json);
                            editor.apply();
                            arrListSpeedGeofences.set(editPosition, speedVtsGeofence);
                            geofenceListViewAdapter.notifyDataSetChanged();
                            loadMoreListView.onLoadMoreComplete();
                            break;
                        }
                    }
                    Snackbar.make(lnrFrameContainer, "\"" + speedVtsGeofence.title + "\" Geofence updated successfully.", Snackbar.LENGTH_LONG).show();
                }
            } else if (tag.equalsIgnoreCase(ApiMethods.geofence + "/delete")) {
                List<SpeedVtsGeofence> speedVtsGeofenceListToRemove =
                        new ArrayList<SpeedVtsGeofence>();
                logD(tag);
                if (speedVtsGeofenceToRemove != null) {
                    logD("speedVtsGeofenceToRemove not null");
                    speedVtsGeofenceListToRemove.add(speedVtsGeofenceToRemove);
                    SpeedVtsGeofenceController.getInstance().removeGeofences(speedVtsGeofenceListToRemove,
                            geofenceControllerListener);
                    arrListSpeedGeofences.remove(deletePosition);
                    geofenceListViewAdapter.notifyDataSetChanged();
                    loadMoreListView.onLoadMoreComplete();

                    Snackbar.make(lnrFrameContainer, "\"" + speedVtsGeofence.title + "\" Geofence deleted successfully.", Snackbar.LENGTH_LONG).show();

                }
            } else {
                if (jsObj.has(WebserviceKeys.geofence)) {
                    JSONArray geoFenceArray = jsObj.getJSONArray(WebserviceKeys.geofence);
                    if (geoFenceArray.length() <= 0)
                        page = page - 1;
                    Gson gson = new Gson();
                    List<SpeedVtsGeofence> speedVtsGeofenceListToAdd = new ArrayList<SpeedVtsGeofence>();
                    for (int i = 0; i < geoFenceArray.length(); i++) {
                        SpeedVtsGeofence geofenceFromAPi = gson.fromJson(geoFenceArray.get(i).toString(),
                                SpeedVtsGeofence.class);

                        logD(geoFenceArray.get(i).toString());

                        if (tag.equalsIgnoreCase(ApiMethods.geofence + "/search")) {
                            arrListSpeedGeofences.add(geofenceFromAPi);
                        } else if (tag.equalsIgnoreCase(ApiMethods.geofence + "/get")) {
                            if (!checkFenceAdded(geofenceFromAPi)) {
                                logD(geofenceFromAPi.title);
                                speedVtsGeofenceListToAdd.add(geofenceFromAPi);
                            }
                            arrListSpeedGeofences.add(geofenceFromAPi);
                        }
                    }

                    if (speedVtsGeofenceListToAdd.size() > 0 && tag.equalsIgnoreCase(ApiMethods.geofence + "/get"))
                        SpeedVtsGeofenceController.getInstance().addGeofence(speedVtsGeofenceListToAdd,
                                geofenceControllerListener);

                    loadingMore = false;
                }

                if (geofenceListViewAdapter !=null && loadMoreListView!=null){
                    geofenceListViewAdapter.notifyDataSetChanged();
                }else if (loadMoreListView!=null){
                    geofenceListViewAdapter = new GeofenceListViewAdapter(arrListSpeedGeofences, this, getActivity());
                    loadMoreListView.setAdapter(geofenceListViewAdapter);
                }
                loadMoreListView.onLoadMoreComplete();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(String tag, int responseCode, String responseMsg) {
        super.onErrorResponse(tag, responseCode, responseMsg);
        Snackbar.make(lnrFrameContainer, "Error processing with your request. Please try after some time.", Snackbar.LENGTH_LONG).show();

    }


    private boolean checkFenceAdded(SpeedVtsGeofence speedVtsGeofence) {
        List<SpeedVtsGeofence> speedVtsGeofenceList = SpeedVtsGeofenceController.getInstance().getSpeedVtsGeofences();
        for (int i = 0; i < speedVtsGeofenceList.size(); i++) {
            logD(speedVtsGeofence.id + " is NULL");
            logD(speedVtsGeofenceList.get(i).id + " is NULL");
            if (speedVtsGeofence.id.equalsIgnoreCase(speedVtsGeofenceList.get(i).id)) {
                return true;
            }
        }
        return false;
    }
}
