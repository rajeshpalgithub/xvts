package com.urcib.speedvts.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.Geofence;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 10/08/16
 *
 * Application class
 */
public class SpeedVtsGeofence implements Comparable, Serializable{

    public String geofenceId;
    public String title;
    public String message;
    public double latitude;
    public double longitude;
    public float radius;
    public boolean whenEnter;
    public String meta;
    public String alert_when;
    public String id;

    @Override
    public int compareTo(Object newSpeedGeofence) {
        SpeedVtsGeofence previousSpeedGeofence = (SpeedVtsGeofence) newSpeedGeofence;
        if (previousSpeedGeofence!=null && title!=null){
            return title.compareTo(previousSpeedGeofence.title);
        }else{
            return 0;
        }

    }

    public Geofence geofence(){
        geofenceId = UUID.randomUUID().toString();
        int transitionType = Geofence.GEOFENCE_TRANSITION_ENTER;
        if (alert_when!=null && alert_when.equalsIgnoreCase("OUT")){
            transitionType = Geofence.GEOFENCE_TRANSITION_EXIT;
        }
        return new Geofence.Builder()
                .setRequestId(geofenceId)
                .setTransitionTypes(transitionType)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }
}
