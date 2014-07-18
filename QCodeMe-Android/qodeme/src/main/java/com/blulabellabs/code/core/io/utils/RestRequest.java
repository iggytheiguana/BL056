package com.blulabellabs.code.core.io.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.utils.RestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Alex on 8/16/13.
 */
public class RestRequest{

    private static final String TAG = RestRequest.class.getName();

    private RequestPackage packege;
    private Listener listener;
    private StringRequest volleyRequest;

    public RestRequest(final Context context, RequestPackage pack, Listener lisn) {
        this.packege = pack;
        this.listener =lisn;
        String absoluteUrl = RestUtils.getAbsoluteUrl(packege.getRequestType());

        if (ApplicationConstants.DEVELOP_MODE)
            Log.i(TAG,String.format("absoluteUrl:%s",absoluteUrl));
        Response.Listener<String> l = new Response.Listener<String>(){

            @Override
            public void onResponse(String result) {
                JSONObject json = null;
                try {
                    json = new JSONObject(result);
                    listener.onResponse(json, packege);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener e = new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {

                listener.onError(error, packege);
            }
        };

        volleyRequest = new StringRequest(Request.Method.POST, absoluteUrl, l, e){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                RequestParams params = packege.getParams();
                Map<String, String> map = null;
                if (params == null){
                    map = new HashMap<String, String>();
                    map.put("void", "void");
                }
                else
                    map = packege.getParams().getMap();
                return map;

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                if (packege.getRequestType() != RequestType.ACCOUNT_LOGIN
                        && packege.getRequestType() != RequestType.ACCOUNT_CREATE){
                    QodemePreferences pref = QodemePreferences.getInstance();
                    String restToken = pref.getRestToken();
                    map.put(RestKeyMap.X_AUTHTOKEN, restToken);
                }
                return map;
            }
        };

    }

    public interface Listener{
        void onResponse(JSONObject jsonObject, RequestPackage packege);
        void onError(VolleyError error, RequestPackage packege);
    }


    public RequestPackage getPackege() {
        return packege;
    }

    public StringRequest getVolleyRequest() {
        return volleyRequest;
    }
}
