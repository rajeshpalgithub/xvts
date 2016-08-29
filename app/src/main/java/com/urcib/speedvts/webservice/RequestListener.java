package com.urcib.speedvts.webservice;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public interface RequestListener {
    void onSuccessResponse(String tag, int responseCode, String responseMsg);
    void onErrorResponse(String tag, int responseCode, String responseMsg);
}
