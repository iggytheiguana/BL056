package biz.softtechnics.qodeme.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import biz.softtechnics.qodeme.ApplicationConstants;
import biz.softtechnics.qodeme.core.io.utils.RequestParams;
import biz.softtechnics.qodeme.core.io.utils.RequestType;
import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;

import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 10/17/13.
 */
public class RestUtils {

    private static final String TAG = makeLogTag(RestUtils.class);


    private static final String JSON_NULL = "null";
    private static RequestQueue mRequestQueue;

    public static String getMd5(String string){
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
            m.update(string.getBytes(), 0, string.length());
            return new BigInteger(1,m.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert JSON to null object
     * @param value -
     * @return null in case "null"
     */
    public static String jsonNullToObjectNull(String value){
        return value.equals(JSON_NULL) ? null : value;
    }

    /**
     * Get absolute url
     * @param requestType
     * @return
     */
    public static String getAbsoluteUrl(RequestType requestType) {
        String result = String.format("%s/%s", ApplicationConstants.BASE_URL, requestType);
        return result;
    }

    /**
     * Get body for request
     * @param requestType
     * @param params
     * @return
     */
    public static String getBody(RequestType requestType, RequestParams params) {
        String result = (params == null) ? "" : params.toString();
        if (requestType != RequestType.ACCOUNT_LOGOUT && requestType != RequestType.ACCOUNT_CREATE){
            QodemePreferences pref = QodemePreferences.getInstance();
            String restToken = pref.getRestToken();
            String addStr = "";
            if (result.contains("&"))
                addStr = "&";
            result = String.format("%s%s%s=%s",result, addStr, RestKeyMap.X_AUTHTOKEN, restToken);
        }
        return result;
    }



    public static JSONObject getRestResult(JSONObject jsonObject) throws JSONException {
        try {
            return jsonObject.getJSONObject(RestKeyMap.RESULT);
        } catch (JSONException e) {
            return null;
        }

    }

    public static int getRestStatus(JSONObject jsonObject) throws JSONException {
        return jsonObject.getInt(RestKeyMap.STATUS);
    }

    public static RequestQueue getVolleyRequestQueueInstance(Context context){
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(context);
        return mRequestQueue;
    }

}
