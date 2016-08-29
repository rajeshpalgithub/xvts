package com.urcib.speedvts.helper;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.urcib.speedvts.model.SpeedVtsGeofence;
import com.urcib.speedvts.webservice.WebService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SpeedVtsGeofenceController {

  private final String TAG = SpeedVtsGeofenceController.class.getName();

  private Context context;
  private GoogleApiClient googleApiClient;
  private Gson gson;
  private SharedPreferences prefs;
  private SpeedVtsGeofenceControllerListener listener;

  private List<SpeedVtsGeofence> speedVtsGeofences;

  public List<SpeedVtsGeofence> getSpeedVtsGeofences() {
    return speedVtsGeofences;
  }

  private List<SpeedVtsGeofence> speedVtsGeofencesToRemove;

  private List<Geofence> geofenceToAddList;
  private List<SpeedVtsGeofence> speedVtsGeofenceListToAdd;

  private static SpeedVtsGeofenceController INSTANCE;

  public static SpeedVtsGeofenceController getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new SpeedVtsGeofenceController();
    }
    return INSTANCE;
  }


  public void init(Context context) {
    this.context = context.getApplicationContext();

    gson = new Gson();
    speedVtsGeofences = new ArrayList<>();
    speedVtsGeofencesToRemove = new ArrayList<>();
    prefs = this.context.getSharedPreferences(SpeedVtsPreferences.KEY_GEOFENCE, Context.MODE_PRIVATE);

    loadGeofences();
  }

  public void addGeofence(List<SpeedVtsGeofence> speedVtsGeofencesList, SpeedVtsGeofenceControllerListener listener) {
    this.speedVtsGeofenceListToAdd = speedVtsGeofencesList;
    this.listener = listener;
    geofenceToAddList = new ArrayList<Geofence>();
    for (int i=0; i<speedVtsGeofenceListToAdd.size(); i++){
      geofenceToAddList.add(speedVtsGeofencesList.get(i).geofence());
    }

    connectWithCallbacks(connectionAddListener);
  }


  public void removeGeofences(List<SpeedVtsGeofence> speedVtsGeofencesToRemove, SpeedVtsGeofenceControllerListener listener) {
    this.speedVtsGeofencesToRemove = speedVtsGeofencesToRemove;
    this.listener = listener;

    connectWithCallbacks(connectionRemoveListener);
  }

  public void removeAllGeofences(SpeedVtsGeofenceControllerListener listener) {
    speedVtsGeofencesToRemove = new ArrayList<>();
    for (SpeedVtsGeofence speedVtsGeofence : speedVtsGeofences) {
      speedVtsGeofencesToRemove.add(speedVtsGeofence);
    }
    this.listener = listener;

    connectWithCallbacks(connectionRemoveListener);
  }

  private void loadGeofences() {
    Map<String, ?> keys = prefs.getAll();
    for (Map.Entry<String, ?> entry : keys.entrySet()) {
      String jsonString = prefs.getString(entry.getKey(), null);
      SpeedVtsGeofence speedVtsGeofence = gson.fromJson(jsonString, SpeedVtsGeofence.class);
      speedVtsGeofences.add(speedVtsGeofence);
    }

//    Collections.sort(speedVtsGeofences);

  }


  private void connectWithCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
    googleApiClient = new GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(callbacks)
            .addOnConnectionFailedListener(connectionFailedListener)
            .build();
    googleApiClient.connect();

  }

  private GeofencingRequest getAddGeofencingRequest() {
    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
    builder.addGeofences(geofenceToAddList);
    return builder.build();
  }

  private void saveGeofence() {
    for (int i=0; i<speedVtsGeofenceListToAdd.size(); i++){
      SpeedVtsGeofence speedVtsGeofenceToAdd = speedVtsGeofenceListToAdd.get(i);
      speedVtsGeofences.add(speedVtsGeofenceToAdd);
      if (listener != null) {
        listener.onGeofencesUpdated();
      }

      String json = gson.toJson(speedVtsGeofenceToAdd);
      SharedPreferences.Editor editor = prefs.edit();
      Log.d(TAG, "saveGeofence: "+json);
      editor.putString(speedVtsGeofenceToAdd.geofenceId, json);
      editor.apply();
    }

  }

  private void removeSavedGeofences() {
    SharedPreferences.Editor editor = prefs.edit();

    for (SpeedVtsGeofence speedVtsGeofence : speedVtsGeofencesToRemove) {
      int index = speedVtsGeofences.indexOf(speedVtsGeofence);
      editor.remove(speedVtsGeofence.geofenceId);
      speedVtsGeofences.remove(index);
      editor.apply();
    }

    if (listener != null) {
      listener.onGeofencesUpdated();
    }
  }

  private void sendError() {
    if (listener != null) {
      listener.onError();
    }
  }

  private GoogleApiClient.ConnectionCallbacks connectionAddListener = new GoogleApiClient.ConnectionCallbacks() {
    @Override
    public void onConnected(Bundle bundle) {
      addGeofence();
    }

    @Override
    public void onConnectionSuspended(int i) {
      Log.e(TAG, "Connecting to GoogleApiClient suspended.");
      sendError();
    }
  };

  private GoogleApiClient.ConnectionCallbacks connectionRemoveListener = new GoogleApiClient.ConnectionCallbacks() {
    @Override
    public void onConnected(Bundle bundle) {
      List<String> removeIds = new ArrayList<>();
      for (SpeedVtsGeofence speedVtsGeofence : speedVtsGeofencesToRemove) {
        removeIds.add(speedVtsGeofence.geofenceId);
      }

      if (removeIds.size() > 0) {
        PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(googleApiClient, removeIds);
        result.setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(Status status) {
            if (status.isSuccess()) {
              removeSavedGeofences();
              Log.e(TAG, "Removing geofence Success ");
            } else {
              Log.e(TAG, "Removing geofence failed: " + status.getStatusMessage());
              sendError();
            }
          }
        });
      }
    }

    @Override
    public void onConnectionSuspended(int i) {
      Log.e(TAG, "Connecting to GoogleApiClient suspended.");
      sendError();
    }
  };

  private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
      Log.e(TAG, "Connecting to GoogleApiClient failed.");
      sendError();
    }
  };

  public interface SpeedVtsGeofenceControllerListener {
    void onGeofencesUpdated();
    void onError();
  }

  private void  addGeofence(){
    Intent intent = new Intent(context, SpeedVtsIntentService.class);
    PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    Log.d(TAG, "addGeofence: "+googleApiClient.isConnected());
    PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences
            (googleApiClient, getAddGeofencingRequest(), pendingIntent);
    result.setResultCallback(new ResultCallback<Status>() {
      @Override
      public void onResult(Status status) {
        if (status.isSuccess()) {
          saveGeofence();
        } else {
          Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() + " : " + status.getStatusCode());
          sendError();
        }
      }
    });
  }

  // end region

}