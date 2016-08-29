package com.urcib.speedvts.webservice.api;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */

public interface WebserviceConstants {

    String BASE_URL = "http://speedvts.com:3000/api/";
    String querySymbol = "?";
    String andSymbol = "&";
    String equalSymbol = "=";

    interface ApiMethods{
        String login = "login";
        String position = "position";
        String ping = "ping";
        String account = "account";
        String password = "password";
        String geofence = "geofence";
    }

    interface ApiParameters{
        String user = "user";
        String pwd = "pwd";

    }

    interface WebserviceKeys{
        String token = "token";
        String ping = "ping";
        String account = "account";
        String password = "password";
        String radius = "radius";
        String longitude = "longitude";
        String latitude = "latitude";
        String alert_when = "alert_when";
        String title = "title";
        String message = "message";
        String meta = "meta";
        String unit = "unit";
        String geofence = "geofence";
        String insertId = "insertId";
        String id = "id";
        String page = "page";
        String record = "record";
        String search_title = "search_title";
    }
}
