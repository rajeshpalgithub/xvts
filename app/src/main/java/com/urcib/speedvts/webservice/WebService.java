package com.urcib.speedvts.webservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonSyntaxException;
import com.urcib.speedvts.SpeedVtsApplication;
import com.urcib.speedvts.webservice.api.WebserviceConstants;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */

public class WebService implements WebserviceConstants {

    private static final String TAG = WebService.class
            .getSimpleName();
    //Progress Dialog
    private static ProgressDialog pDialog;

    Map<String, String> header;

    /**
     * #param context is Context object
     * #return instance of this class
     * #description Method to return the instance of this class.
     */
    public static synchronized WebService getInstance(Context context) {
//        if (mInstance == null)
        return new WebService(context);
    }

    /**
     * #param context is context object.
     * #description Initialize progress dialog.
     */
    private WebService(Context context) {

//        pDialog.setCancelable(false);
    }

    /**
     * #param tag      string
     * #param url      string
     * #param params   Map
     * #param listener custom listener interface
     * #description Method to requesting server with GET method with params,
     * and it will return status code and string response.
     */
    public void doRequestwithGET(Context context, final String tag, String url, final HashMap<String, String> params, final RequestListener listener, boolean ShowDialog) {
        volleyJsonRequest(context, tag, Request.Method.GET, url, params, listener, ShowDialog);
    }

    /**
     * #param tag      string
     * #param url      string
     * #param listener custom listener interface
     * #description Method to requesting server with GET Method without params,
     * and it will return status code and string response.
     */
    public void doRequestwithGET(Context context, final String tag, String url, Map<String, String> header, final RequestListener listener, boolean ShowDialog) {
        this.header = header;
        volleyJsonRequest(context, tag, Request.Method.GET, url, null, listener, ShowDialog);
    }

    /**
     * #param tag      string
     * #param url      string
     * #param params   Map
     * #param listener custom listener interface
     * #descrption Method to requesting server with POST method with params,
     * and it will return status code and string response.
     */
    public void doRequestwithPOST(Context context, final String tag, String url, final HashMap<String, String> params, final RequestListener listener, boolean ShowDialog) {
//        doObjectRequest(url, tag, params, listener);
        volleyStringRequest(context, tag, Request.Method.POST, url, params, listener, ShowDialog);
    }

    /**
     * #param tag      string
     * #param url      string
     * #param obj      Hashmap
     * #param listener custom listener interface
     * #descrption Method to requesting server with POST method with string entity params,
     * and it will return status code and string response.
     */
    public void doMultipartObjectRequestWithPOST(Context context, final String tag, String url, HashMap<String, String> obj, final RequestListener listener, boolean showDialog) {
        volleyMultipartEntityRequest(context, tag, url, obj, new ArrayList<String>(), null, listener, showDialog);
    }


    /**
     * #param tag          string
     * #param url          string
     * #param filePartName string
     * #param file         file
     * #param obj          hashmap
     * #param listener     custom listener interface
     * #description Method to requesting server with POST method with upload file and string entity params,
     * and it will return staus code and string response.
     */
    public void doMultipartObjectRequestWithPOST(Context context, final String tag, String url, String filePartName, List<File> file, HashMap<String, String> obj, final RequestListener listener, boolean showDialog) {
        volleyMultipartEntityRequest(context, tag, url, obj, filePartName, file, listener, showDialog);
    }


    /**
     * #param tag          string
     * #param url          string
     * #param filePartName string
     * #param file         file
     * #param obj          hashmap
     * #param listener     custom listener interface
     * #description Method to requesting server with POST method with upload file and string entity params,
     * and it will return staus code and string response.
     */
    public void doMultipartObjectRequestWithPOST(Context context, final String tag, String url, List<String> fileNames, List<File> file, HashMap<String, String> obj, final RequestListener listener, boolean showDialog) {
        volleyMultipartEntityRequest(context, tag, url, obj, fileNames, file, listener, showDialog);
    }

    /**
     * #param tag      string
     * #param url      string
     * #param params   Map
     * #param listener custom listener interface
     * #descrption Method to requesting server with POST method with params,
     * and it will return status code and string response.
     */
    public void doRequestwithPUT(Context context, final String tag, String url, final HashMap<String, String> params, final RequestListener listener, boolean ShowDialog) {
//        doObjectRequest(url, tag, params, listener);
        volleyStringRequest(context, tag, Request.Method.PUT, url, params, listener, ShowDialog);
    }

    /**
     * #param tag      string
     * #param url      string
     * #param params   Map
     * #param listener custom listener interface
     * #descrption Method to requesting server with POST method with params,
     * and it will return status code and string response.
     */
    public void doRequestwithDELETE(Context context, final String tag, String url, final HashMap<String, String> params, final RequestListener listener, boolean ShowDialog) {
//        doObjectRequest(url, tag, params, listener);
        volleyStringRequest(context, tag, Request.Method.DELETE, url, params, listener, ShowDialog);
    }

    private void volleyMultipartEntityRequest(Context context, final String tag, String url, HashMap<String, String> obj, List<String> fileNames, List<File> file, final RequestListener listener, boolean showDialog) {
        if (showDialog)
            showpDialog(context);

        MultiPartEntityRequest request = new MultiPartEntityRequest(url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hidepDialog();
                Log.e(TAG, "Error:" + volleyError.networkResponse);
                NetworkResponse networkResponse = volleyError.networkResponse;
                if (listener != null) {
                    try {
                        listener.onErrorResponse(tag, networkResponse.statusCode, new String(networkResponse.data));
                    } catch (Exception e) {
                        listener.onErrorResponse(tag, HttpStatus.SC_NO_CONTENT, "");
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                hidepDialog();
                if (listener != null) {
                    VolleyCustomResponse response = VolleyCustomResponse.create(s);
                    if (response.getStatuscode() == 200) {
                        if (new String(response.getResponseData()).trim().length() == 0) {
                            listener.onErrorResponse(tag, response.getStatuscode(), "");
                        } else
                            try {
                                listener.onSuccessResponse(tag, response.getStatuscode(), new String(response.getResponseData()));
                            } catch (Exception jse) {
                                jse.printStackTrace();
                            }
                    } else {
                        try {
                            listener.onErrorResponse(tag, response.getStatuscode(), new String(response.getResponseData()));
                        } catch (Exception e) {
                            listener.onErrorResponse(tag, HttpStatus.SC_NO_CONTENT, new String(response.getResponseData()));
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, fileNames, file, obj);
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SpeedVtsApplication.getInstance().addToRequestQueue(request);
    }

    /**
     * #param tag      string
     * #param method   int
     * #param url      string
     * #param params   hashmap
     * #param listener custom listener interface
     * #desscription It's a common method for executing json server request.
     */
    private void volleyJsonRequest(Context context, final String tag, int method, String url, final HashMap<String, String> params, final RequestListener listener, boolean showDialog) {
        if (showDialog)
            showpDialog(context);
        Log.e("Webservice : TAG", tag);
        Log.e("Webservice : URL", url);


        VolleyJsonRequest jsonObjReq = new VolleyJsonRequest(method, url, params, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hidepDialog();
                NetworkResponse networkResponse = volleyError.networkResponse;
                if (listener != null && networkResponse!=null) {
                    try {
                        listener.onErrorResponse(tag, networkResponse.statusCode, new String(networkResponse.data));
                    } catch (Exception e) {
                        listener.onErrorResponse(tag, HttpStatus.SC_NO_CONTENT, "");
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                hidepDialog();
                if (listener != null) {
                    VolleyCustomResponse response = VolleyCustomResponse.create(s);
                    Log.e("Webservice : STATUSCODE", "" + response.getStatuscode());
                    Log.e("Webservice : DATA", new String(response.getResponseData()));
                    if (response.getStatuscode() == 200) {
                        try {
                            listener.onSuccessResponse(tag, response.getStatuscode(), new String(response.getResponseData()));
                        } catch (Exception jse) {
                            jse.printStackTrace();
                        }
                    } else {
                        try {
                            listener.onErrorResponse(tag, response.getStatuscode(), new String(response.getResponseData()));
                        } catch (Exception e) {
                            listener.onErrorResponse(tag, HttpStatus.SC_NO_CONTENT, "");
                            e.printStackTrace();
                        }
                    }
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (header == null) {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/x-www-form-urlencoded");
                    return headers;
                } else {
                    return header;
                }
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Log.v("params","Inside params");
                if (params!=null){
                    Log.v("params",params.toString());
                    return params;
                }
                return super.getParams();
            }
        };
        jsonObjReq.setShouldCache(false);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SpeedVtsApplication.getInstance().addToRequestQueue(jsonObjReq, tag);
    }

    /**
     * #param tag      string
     * #param method   int
     * #param url      string
     * #param params   hashmap
     * #param listener custom listener interface
     * #desscription It's a common method for executing json server request.
     */
    private void volleyStringRequest(Context context, final String tag, int method, String url, final HashMap<String, String> params, final RequestListener listener, boolean showDialog) {
        if (showDialog)
            showpDialog(context);
        Log.e("Webservice : TAG", tag);
        Log.e("Webservice : URL", url);


        VolleyStringRequest stringRequest = new VolleyStringRequest(method, url, params, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hidepDialog();
                NetworkResponse networkResponse = volleyError.networkResponse;
                if (listener != null && networkResponse!=null) {
                    try {
                        listener.onErrorResponse(tag, networkResponse.statusCode, new String(networkResponse.data));
                    } catch (Exception e) {
                        listener.onErrorResponse(tag, HttpStatus.SC_NO_CONTENT, "");
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                hidepDialog();
                if (listener != null) {
                    VolleyCustomResponse response = VolleyCustomResponse.create(s);
                    Log.e("Webservice : STATUSCODE", "" + response.getStatuscode());
                    Log.e("Webservice : DATA", new String(response.getResponseData()));
                    if (response.getStatuscode() == 200) {
                        try {
                            listener.onSuccessResponse(tag, response.getStatuscode(), new String(response.getResponseData()));
                        } catch (Exception jse) {
                            jse.printStackTrace();
                        }
                    } else {
                        try {
                            listener.onErrorResponse(tag, response.getStatuscode(), new String(response.getResponseData()));
                        } catch (Exception e) {
                            listener.onErrorResponse(tag, HttpStatus.SC_NO_CONTENT, "");
                            e.printStackTrace();
                        }
                    }
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (header == null) {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/x-www-form-urlencoded");
                    return headers;
                } else {
                    return header;
                }
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Log.v("params","Inside params");
                if (params!=null){
                    Log.v("params",params.toString());
                    return params;
                }
                return super.getParams();
            }
        };
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SpeedVtsApplication.getInstance().addToRequestQueue(stringRequest, tag);
    }

    public void doObjectRequest(Context context, String URL, final String Tag, JSONObject params, final RequestListener listener, boolean showDialog) {
        try {
            if (showDialog)
                showpDialog(context);
            Log.e("Webservice : TAG", Tag);
            Log.e("Webservice : URL", URL);

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hidepDialog();
                    try {
                        if (listener != null)
                            listener.onSuccessResponse(Tag, 0, response.toString());
                    } catch (JsonSyntaxException jse) {
                        try {
                            listener.onSuccessResponse(Tag, 0, new JSONArray("[]").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

                    , new Response.ErrorListener()

            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hidepDialog();

                    NetworkResponse networkResponse = error.networkResponse;
                    if (listener != null)
                        try {
                            listener.onErrorResponse(Tag, 0, Integer.toString(networkResponse.statusCode));
                        } catch (Exception e) {
                            listener.onErrorResponse(Tag, 0, Integer.toString(HttpStatus.SC_NO_CONTENT));
                            e.printStackTrace();
                        }
                }
            }

            );
            jsArrayRequest.setShouldCache(false);
            jsArrayRequest.setRetryPolicy(new

                    DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

            );
            SpeedVtsApplication.getInstance().
                    addToRequestQueue(jsArrayRequest, Tag);

        } catch (IllegalStateException ise) {
            Log.v(TAG, ise.toString());
        }
    }

    /**
     * #param tag          string
     * #param url          string
     * #param obj          hashmap
     * #param filePartname string
     * #param file         file
     * #param listener     custom listener interface
     * #description Method to requesting server with POST method with upload file and string entity params,
     * and it will return staus code and string response.
     */

    private void volleyMultipartEntityRequest(Context context, final String tag, String url, HashMap<String, String> obj, final String filePartname, final List<File> file, final RequestListener listener, boolean showDialog) {
        if (showDialog)
            showpDialog(context);
        MultiPartEntityRequest request = new MultiPartEntityRequest(url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hidepDialog();
                Log.e(TAG, "Error:" + volleyError.networkResponse);
                NetworkResponse networkResponse = volleyError.networkResponse;
                if (listener != null) {
                    try {
                        listener.onErrorResponse(tag, networkResponse.statusCode, new String(networkResponse.data));
                    } catch (Exception e) {
                        listener.onErrorResponse(tag, HttpStatus.SC_NO_CONTENT, new String(networkResponse.data));
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                hidepDialog();
                if (listener != null) {
                    VolleyCustomResponse response = VolleyCustomResponse.create(s);
                    if (response.getStatuscode() == 200) {

                        try {
                            listener.onSuccessResponse(tag, response.getStatuscode(), new String(response.getResponseData()));
                        } catch (Exception jse) {
                            jse.printStackTrace();
                        }
                    } else {
                        try {
                            listener.onErrorResponse(tag, response.getStatuscode(), new String(response.getResponseData()));
                        } catch (Exception e) {
                            listener.onErrorResponse(tag, HttpStatus.SC_NO_CONTENT, new String(response.getResponseData()));
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, filePartname, file, obj);
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SpeedVtsApplication.getInstance().addToRequestQueue(request, tag);
    }


    private void showpDialog(Context context) {
        if (pDialog == null || !pDialog.getContext().equals(context)) {
            pDialog = new ProgressDialog(context);
            pDialog.setCancelable(false);
            pDialog.setMessage("Please wait...");
        }
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog != null)
            pDialog.dismiss();
    }

}
