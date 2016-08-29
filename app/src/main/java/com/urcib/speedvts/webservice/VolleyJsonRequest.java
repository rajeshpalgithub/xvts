package com.urcib.speedvts.webservice;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class VolleyJsonRequest extends JsonRequest<String> {

    private final Response.Listener<String> mListener;
    private final String BOUNDARY = "content";
    private HashMap<String, String> params;

    public VolleyJsonRequest(int method, String url, Response.ErrorListener errorListener, Response.Listener<String> listener, JSONObject params) {

        super(method, url, params.toString(), listener, errorListener);
        mListener = listener;
    }

    public VolleyJsonRequest(int method, String url, HashMap<String, String> params, Response.ErrorListener errorListener, Response.Listener<String> listener) {
        super(method, url, null, listener, errorListener);
        mListener = listener;
        this.params = params;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
        try {
            VolleyCustomResponse res = new VolleyCustomResponse(networkResponse);
//            String je = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
            return Response.success(res.serialize(), getCacheEntry());
//            JSONObject resss = new JSONObject(je);
//            Cache.Entry resp = HttpHeaderParser.parseCacheHeaders(networkResponse);
//            VolleyCustomResponse res = new VolleyCustomResponse(networkResponse);
//            return Response.success(res.toString(), getCacheEntry());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




/*
	@Override
	public String getBodyContentType() {
		return "multipart/form-data; boundary=" + BOUNDARY + "; charset=utf-8";
	}


	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "multipart/form-data; boundary=" + BOUNDARY + "; charset=utf-8");
		return params;
	}
*/



    @Override
    protected void deliverResponse(String s) {
        mListener.onResponse(s);
    }
}
