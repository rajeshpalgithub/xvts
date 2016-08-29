package com.urcib.speedvts;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.urcib.speedvts.webservice.CustomHurlStack;
import com.urcib.speedvts.webservice.LruBitmapCache;
import com.urcib.speedvts.webservice.OkHttpHurlStack;

import android.net.http.AndroidHttpClient;

import java.io.File;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 09/08/16
 *
 * Application class
 */
public class SpeedVtsApplication extends MultiDexApplication{

    private static final String TAG = "SpeedVtsApplication";

    public static SpeedVtsApplication speedVtsApplication = null;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    @Override
    public void onCreate() {
        super.onCreate();
        speedVtsApplication = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    public static synchronized SpeedVtsApplication getInstance() {
        return speedVtsApplication;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            HttpStack httpStack;
            if (Build.VERSION.SDK_INT > 19){
                httpStack = new CustomHurlStack();
            } else if (Build.VERSION.SDK_INT >= 9 && Build.VERSION.SDK_INT <= 19)
            {
                httpStack = new OkHttpHurlStack();
            } else {
                httpStack = new HttpClientStack(AndroidHttpClient.newInstance("Android"));
            }
            mRequestQueue = Volley.newRequestQueue(getApplicationContext(), httpStack);
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static File getCacheDirectory() {
        return speedVtsApplication.getCacheDir();
    }
}
