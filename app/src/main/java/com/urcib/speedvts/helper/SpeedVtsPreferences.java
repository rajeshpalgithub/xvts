package com.urcib.speedvts.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class SpeedVtsPreferences {
    public static final String KEY = "SpeedVtsPreferences";
    public static final String KEY_GEOFENCE = "SpeedVtsGeoFences";

    public interface PreferenceKeys{
        String token_key = "token";
        String latest_position = "latest_position";
        String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    }

    public static String getStringValue(Context context, String key){
        String user_id = "";
        if(context!=null && context.getSharedPreferences(KEY, Context.MODE_PRIVATE)!=null){
            SharedPreferences user_pref = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
            user_id = user_pref.getString(key, "");
        }
        return user_id;
    }

    public static void setStringValue(Context ctx, String key, String value){
        if(ctx!=null){
            SharedPreferences.Editor edt = ctx.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
            edt.putString(key, value);
            edt.commit();
        }
    }

    public static boolean getBooleanValue(Context context, String key){
        boolean isVal = false;
        if(context!=null && context.getSharedPreferences(KEY, Context.MODE_PRIVATE)!=null){
            SharedPreferences user_pref = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
            isVal = user_pref.getBoolean(key, false);
        }
        return isVal;
    }

    public static void setBooleanValue(Context ctx, String key, boolean value){
        if(ctx!=null){
            SharedPreferences.Editor edt = ctx.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
            edt.putBoolean(key, value);
            edt.commit();
        }
    }

    public static int getIntValue(Context context, String key){
        int value = 0;
        if(context!=null && context.getSharedPreferences(KEY, Context.MODE_PRIVATE)!=null){
            SharedPreferences user_pref = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
            value = user_pref.getInt(key, 0);
        }
        return value;
    }

    public static void setIntValue(Context ctx, String key, int value){
        if(ctx!=null){
            SharedPreferences.Editor edt = ctx.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
            edt.putInt(key, value);
            edt.commit();
        }
    }

    public static void clearPreferences(Context ctx){
        if(ctx!=null){
            SharedPreferences.Editor edt = ctx.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
            edt.remove(PreferenceKeys.token_key);
            edt.commit();

            SharedPreferences.Editor edtGeo = ctx.getSharedPreferences(KEY_GEOFENCE, Context.MODE_PRIVATE).edit();
            edt.clear();
            edt.commit();
        }
    }
}
