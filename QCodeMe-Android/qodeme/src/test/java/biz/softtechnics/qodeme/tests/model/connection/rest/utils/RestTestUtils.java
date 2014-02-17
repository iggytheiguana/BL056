package biz.softtechnics.qodeme.tests.model.connection.rest.utils;

import android.test.AndroidTestCase;
import android.util.Log;

import com.android.volley.VolleyError;

import biz.softtechnics.qodeme.core.connection.rest.RestError;
import biz.softtechnics.qodeme.core.connection.rest.RestErrorType;

import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/27/13
 * Time: 3:16 PM
 */
public class RestTestUtils {

    private static final String TAG = makeLogTag(RestTestUtils.class);

    public static void logRequest(AndroidTestCase itc, String message){
        Log.i(itc.getClass().getSimpleName(), String.format("send request", message));
    }

    public static void logPassed(AndroidTestCase itc, String message){
        Log.i(itc.getClass().getSimpleName(), String.format("%s::%s - passed, ", itc.getClass().getSimpleName(), message));
    }

    public static void logFailed(AndroidTestCase itc,RestError error){
        Log.e(itc.getClass().getSimpleName(), String.format("%s - failed ", itc.getClass().getSimpleName()) + RestErrorType.getMessage(itc.getContext(), error.getErrorType()));
    }

    public static void logFailed(AndroidTestCase itc, VolleyError error) {
        Log.e(itc.getClass().getSimpleName(), String.format("%s - failed, error:%s, response:%s", itc.getClass().getSimpleName(), error.getMessage(), error.networkResponse.toString()));
    }

}
