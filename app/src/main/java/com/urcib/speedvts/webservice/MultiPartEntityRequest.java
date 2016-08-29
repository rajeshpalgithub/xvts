package com.urcib.speedvts.webservice;

/**
 *
 */

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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
public class MultiPartEntityRequest extends Request<String> {
    private final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
    private final Response.Listener<String> mListener;
    private final HashMap<String, String> mParams;
    String mFilePartName;
    File mFile;


    /**
     * @param url           string
     * @param errorListener volley response listener
     * @param listener      volley response listener
     * @param file
     * @param params        hashmap  @descrption Constructor to set string entity
     */
    public MultiPartEntityRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener, List<String> fileNames, List<File> file, HashMap<String, String> params) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mParams = params;
        try {
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
                entity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
            }
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }
        if (file != null) {

            for (int i = 0; i < file.size(); i++) {
                mFile = file.get(i);
                String filetype = mFile.getName().contains(".pdf") ? "pdf" : "jpeg";
                if (filetype.equalsIgnoreCase("pdf"))
                    entity.addPart(fileNames.get(i), new FileBody(mFile, "application/pdf"));
                else
                    entity.addPart(fileNames.get(i), new FileBody(mFile, "image/" + filetype));
            }
        }

    }

    /**
     * @param url           string
     * @param errorListener volley response listener
     * @param listener      volley response listener
     * @param params        hashmap
     * @descrption Constructor to set string entity
     */
    public MultiPartEntityRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener, HashMap<String, String> params) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mParams = params;
        buildMultipartEntity();
    }

    public MultiPartEntityRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener, String filePartName, List<File> file, HashMap<String, String> params) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mParams = params;
        if (file != null) {

            mFilePartName = filePartName;
            for (int i = 0; i < file.size(); i++) {
                mFile = file.get(i);
                entity.addPart(filePartName, new FileBody(mFile));
            }
        }

        try {
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
                entity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
            }
           /* Iterator<Map.Entry<String, String>> paramIterator = mParams.entrySet().iterator();
            while (paramIterator.hasNext()) {
                Map.Entry<String, String> entry = paramIterator.next();
                entity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
            }*/
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }

    }


    private void buildMultipartEntity() {
        try {
            /*Iterator<Map.Entry<String, String>> paramIterator = mParams.entrySet().iterator();
            while (paramIterator.hasNext()) {
                Map.Entry<String, String> entry = paramIterator.next();
                entity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
            }*/
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
                entity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
            }
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }

    }

    @Override
    public String getBodyContentType() {
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            entity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        VolleyCustomResponse res = new VolleyCustomResponse(response);
        return Response.success(res.serialize(), getCacheEntry());
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}