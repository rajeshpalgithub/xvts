package com.urcib.speedvts.helper;

import com.urcib.speedvts.model.SpeedVtsGeofence;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 17/08/16
 *
 * Add description about the class
 */
public interface SpeedVtsGeofenceAddListener {
    void onAddGeofenceClick(SpeedVtsGeofence geofence);
    void onCancelClick();
}
