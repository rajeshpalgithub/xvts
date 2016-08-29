package com.urcib.speedvts.webservice;

import com.android.volley.NetworkResponse;
import com.google.gson.Gson;

import java.util.Map;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */

public class VolleyCustomResponse {
    private int statuscode;
    private byte[] data;
    private Map<String, String> headers;
    private boolean notModified;

    public VolleyCustomResponse(int statuscode, byte[] data, Map<String, String> headers, boolean notModified){
        this.statuscode = statuscode;
        this.data = data;
        this.headers = headers;
        this.notModified = notModified;
    }

    public VolleyCustomResponse(NetworkResponse response){
        this.statuscode = response.statusCode;
        this.data = response.data;
        this.headers = response.headers;
        this.notModified = response.notModified;
    }

    public int getStatuscode(){
        return this.statuscode;
    }

    public void setStatuscode(int statuscode){
        this.statuscode = statuscode;
    }

    public byte[] getResponseData(){
        return  this.data;
    }

    public void setResponseData(byte[] data){
        this.data = data;
    }

    public Map<String, String> getHeaders(){
        return  this.headers;
    }

    public void setHeaders(Map<String, String> headers){
        this.headers = headers;
    }

    public boolean getNotModified(){
        return  this.notModified;
    }

    public void setNotModified(boolean notModified){
        this.notModified = notModified;
    }

    public String serialize() {
        // Serialize this class into a JSON string using GSON
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    static public VolleyCustomResponse create(String serializedData) {
        // Use GSON to instantiate this class using the JSON representation of the state
        if (serializedData == null)
            return null;
        Gson gson = new Gson();
        return gson.fromJson(serializedData, VolleyCustomResponse.class);
    }
}