package com.urcib.speedvts.helper;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.urcib.speedvts.R;
import com.urcib.speedvts.model.SpeedVtsGeofence;
import com.urcib.speedvts.model.VehiclePosition;
import com.urcib.speedvts.ui.SplashScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpeedVtsIntentService extends IntentService {

  // region Properties

  private final String TAG = SpeedVtsIntentService.class.getName();

  private SharedPreferences prefs;
  private Gson gson;

  // endregion

  // region Constructors

  public SpeedVtsIntentService() {
    super("SpeedVtsIntentService");
  }

  // endregion

  // region Overrides

  @Override
  protected void onHandleIntent(Intent intent) {
    prefs = getApplicationContext().getSharedPreferences(SpeedVtsPreferences.KEY_GEOFENCE, Context.MODE_PRIVATE);
    gson = new Gson();

    GeofencingEvent event = GeofencingEvent.fromIntent(intent);
    if (event != null) {
      if (event.hasError()) {
        onError(event.getErrorCode());
      } else {
        int transition = event.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
          List<String> geofenceIds = new ArrayList<>();
          for (Geofence geofence : event.getTriggeringGeofences()) {
            geofenceIds.add(geofence.getRequestId());
          }
          if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            onEnteredGeofences(geofenceIds);
          }
        }
      }
    }
  }

  // endregion

  // region Private

  private void onEnteredGeofences(List<String> geofenceIds) {
    for (String geofenceId : geofenceIds) {
      String geofenceName = "";
      Gson gson = new Gson();
      // Loop over all geofence keys in prefs and retrieve NamedGeofence from SharedPreference
      Map<String, ?> keys = prefs.getAll();
      boolean isValidRadius = false;

      for (Map.Entry<String, ?> entry : keys.entrySet()) {
        String jsonString = prefs.getString(entry.getKey(), null);
        SpeedVtsGeofence speedVtsGeofence = gson.fromJson(jsonString, SpeedVtsGeofence.class);
        Log.d(TAG, "onEnteredGeofences: "+geofenceId + "=="+speedVtsGeofence.geofenceId);
        if (speedVtsGeofence!=null &&speedVtsGeofence.geofenceId!=null
                && speedVtsGeofence.geofenceId.equals(geofenceId)) {
          geofenceName = speedVtsGeofence.title;
          float radiusForFence = speedVtsGeofence.radius;
          String strPing = SpeedVtsPreferences.getStringValue(getApplicationContext(), SpeedVtsPreferences.PreferenceKeys.latest_position);
          VehiclePosition vehiclePosition = gson.fromJson(strPing, VehiclePosition.class);
          if (vehiclePosition!=null){
            double lat = Double.parseDouble(vehiclePosition.latitude);
            double lon = Double.parseDouble(vehiclePosition.longitude);
            double distance = distance(lat, lon, speedVtsGeofence.latitude, speedVtsGeofence.longitude);
            Log.d(TAG, "onEnteredGeofences: "+distance);
            Log.d(TAG, "onEnteredGeofences: "+lat+","+lon+"==="+speedVtsGeofence.latitude+","+speedVtsGeofence.longitude);
            if (distance > radiusForFence){
              isValidRadius = false;
            }else {
              isValidRadius = true;
            }
          }

          break;
        }




      }

      if (!isValidRadius){
        return;
      }

      // Set the notification text and send the notification
      String contextText = this.getResources().getString(R.string.enter_geo_fence)+" "+geofenceName;

      NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
      Intent intent = new Intent(this, SplashScreen.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

      Notification notification = new NotificationCompat.Builder(this)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setContentTitle(this.getResources().getString(R.string.app_name))
              .setContentText(contextText)
              .setContentIntent(pendingNotificationIntent)
              .setStyle(new NotificationCompat.BigTextStyle().bigText(contextText))
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setAutoCancel(true)
              .build();
      notificationManager.notify(0, notification);

    }
  }

  private void onError(int i) {
    Log.e(TAG, "Geofencing Error: " + i);
  }

  private double distance(double lat1, double lon1, double lat2, double lon2) {
    double theta = lon1 - lon2;
    double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    dist = Math.acos(dist);
    dist = rad2deg(dist);
    dist = dist * 60 * 1.1515;
    return (dist * 1000);
  }

  private double deg2rad(double deg) {
    return (deg * Math.PI / 180.0);
  }
  private double rad2deg(double rad) {
    return (rad * 180.0 / Math.PI);
  }

  // endregion
}

