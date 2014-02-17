package biz.softtechnics.qodeme.tests.model.connection.rest.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import biz.softtechnics.qodeme.ApplicationConstants;
import biz.softtechnics.qodeme.core.connection.rest.RequestParams;
import biz.softtechnics.qodeme.core.connection.rest.RequestType;
import biz.softtechnics.qodeme.core.connection.rest.RestError;
import biz.softtechnics.qodeme.core.connection.rest.RestErrorType;
import biz.softtechnics.qodeme.core.connection.rest.RestKeyMap;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountContactsResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.ChatLoadResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.ChatMessageResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.ContactAddResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.UserSettingsResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.VoidResponse;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.RestUtils;

import static biz.softtechnics.qodeme.utils.LogUtils.LOGI;
import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 12/1/13.
 */
public class RestSyncTestHelper {

    private static final String TAG = makeLogTag(RestTestUtils.class);

    private static volatile RestSyncTestHelper instance;
    private RequestQueue mQueue;

    public static RestSyncTestHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (RequestQueue.class) {
                if (instance == null) {
                    instance = new RestSyncTestHelper(context);
                }
            }
        }
        return instance;

    }

    private RestSyncTestHelper(Context context){
        mQueue = new Volley().newRequestQueue(context);
    }

    public AccountCreateResponse accountCreate(String passwordMd5) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PASSWORD, passwordMd5);
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_CREATE, params, mQueue, null);
        return new AccountCreateResponse().parse(jsonResult);
    }

    public AccountLoginResponse accountLogin(String qrcode, String passwordMd5) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.QRCODE, qrcode);
        params.put(RestKeyMap.PASSWORD, passwordMd5);
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_LOGIN, params, mQueue, null);
        return new AccountLoginResponse().parse(jsonResult);
    }

    public VoidResponse accountLogout(String restToken) throws InterruptedException, ExecutionException, JSONException {
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_LOGOUT, null, mQueue, restToken);
        return new VoidResponse().parse(jsonResult);
    }

    public ContactAddResponse contactAdd(String qrcode, String message, String publicName, String location, String restToken) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
        params.put(RestKeyMap.MESSAGE, message);
        params.put(RestKeyMap.PUBLIC_NAME, publicName);
        params.put(RestKeyMap.LOCATION, location);
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.CONTACT_ADD, params, mQueue, restToken);
        return new ContactAddResponse().parse(jsonResult);
    }

    public AccountContactsResponse accountContacts(String restToken) throws InterruptedException, ExecutionException, JSONException {
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_CONTACTS, null, mQueue, restToken);
        return new AccountContactsResponse().parse(jsonResult);
    }

    public ChatMessageResponse chatMessage(long chatId, String message, long unixTimeStamp, String token) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.MESSAGE, message);
        params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
        params.put(RestKeyMap.DATETIME, String.valueOf(unixTimeStamp));
        JSONObject jsonObject =  newSyncJsonObjectRequest(RequestType.CHAT_MESSAGE, params, mQueue, token);
        return new ChatMessageResponse().parse(jsonObject);
    }

    public ChatLoadResponse chatLoad(long chatId, int page, int limit, String token) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
        params.put(RestKeyMap.PAGE, String.valueOf(page));
        params.put(RestKeyMap.LIMIT, String.valueOf(limit));
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CHAT_LOAD, params, mQueue, token);
        return new ChatLoadResponse().parse(jsonObject);
    }

    public VoidResponse contactAccept(String qrcode, String token) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_ACCEPT, params, mQueue, token);
        return new VoidResponse().parse(jsonObject);
    }

    public VoidResponse contactReject(String qrcode, String token) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_REJECT, params, mQueue, token);
        return new VoidResponse().parse(jsonObject);
    }

    public VoidResponse contactBlock(String qrcode, String token) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_BLOCK, params, mQueue, token);
        return new VoidResponse().parse(jsonObject);
    }

    public UserSettingsResponse getUserSettings(String token) throws InterruptedException, ExecutionException, JSONException {
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.GET_USER_SETTINGS, null, mQueue, token);
        return new UserSettingsResponse().parse(jsonObject);
    }

    public VoidResponse setUserSettings(String message,
                                                   boolean withMessage,
                                                   String publicName,
                                                   boolean withPublicName,
                                                   boolean withAotoAccept,
                                                   String location,
                                                   boolean withSaveDateTime,
                                                   String token) throws InterruptedException, ExecutionException, JSONException {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.MESSAGE, message);
        params.put(RestKeyMap.WITH_MESSAGE, Converter.booleanToIntString(withMessage));
        params.put(RestKeyMap.PUBLIC_NAME, publicName);
        params.put(RestKeyMap.WITH_PUBNAME, Converter.booleanToIntString(withPublicName));
        params.put(RestKeyMap.AUTO_ACCEPT, Converter.booleanToIntString(withAotoAccept));
        params.put(RestKeyMap.LOCATION, location);
        params.put(RestKeyMap.SET_TIMELOC, Converter.booleanToIntString(withSaveDateTime));
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.SET_USER_SETTINGS, params, mQueue, token);
        return new VoidResponse().parse(jsonObject);
    }

    public VoidResponse chatSetInfo(long chatId, String title, Integer color, Integer height, String token) throws InterruptedException, ExecutionException, JSONException{
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.ID, String.valueOf(chatId));
        if (title != null) params.put(RestKeyMap.TITLE, title);
        if (color != null) params.put(RestKeyMap.COLOR, String.valueOf(color));
        if (height != null) params.put(RestKeyMap.CHAT_HEIGHT, String.valueOf(height));
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CHAT_SET_INFO, params, mQueue, token);
        return new VoidResponse().parse(jsonObject);
    }

/*
    RequestType.ACCOUNT_CREATE, params, getQueue(), null

    void accountCreate(String passwordMd5, RestListener callback);
    void accountLogin(String qrcode, String passwordMd5, RestListener callback);
    void accountLogin(RestListener callback);
    void accountLogout(RestListener callback);
    void accountContacts(RestListener callback);
    void chatCreate(ChatType chatType, String title, String tags, int color, RestListener callback);
    void chatSetInfo(long chatId, String title, int color, RestListener callback);
    void chatAddMember(long chatId, String qrcode, RestListener callback);
    void chatMessage(long chatId, String message, RestListener callback);
    void chatLoad(long chatId, int page, int limit, RestListener callback);
    void contactAdd(String contactCqroce, RestListener callback);
    void contactRemove(long contactId, RestListener callback);
    void chatDropMember(long chatId, String memberQrcode, RestListener callback);
    void lookup(String searchQuery, RestListener callback);
    void registerToken(String gcmToken, RestListener callback);*/

    /**
     * Synchronous volley request
     *
     * @param requestType
     * @param params
     * @return
     */
    public static JSONObject newSyncJsonObjectRequest(final RequestType requestType, final RequestParams params, RequestQueue requestQueue, final String restToken) throws ExecutionException, InterruptedException, JSONException {

        LOGI(TAG, String.format("Server request, type:%s, with params:[%s]", requestType, params != null ? params.toString() : ""));

        int method = Request.Method.POST;
        RequestFuture<String> future = RequestFuture.newFuture();
        String absoluteUrl = RestUtils.getAbsoluteUrl(requestType);

        StringRequest request = new StringRequest(method, absoluteUrl, future, future){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = null;
                if (params == null)
                    map = new HashMap<String, String>();
                    map.put("void", "void");
                else
                    map = params.getMap();
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                if (requestType != RequestType.ACCOUNT_LOGIN
                        && requestType != RequestType.ACCOUNT_CREATE){
                    map.put(RestKeyMap.X_AUTHTOKEN, restToken);
                }
                return map;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                ApplicationConstants.REST_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);

        String json = future.get();

        LOGI(TAG, String.format("Server response, type:%s:[%s]", requestType, json));

        JSONObject jsonObject = new JSONObject(json);
        int status = RestUtils.getRestStatus(jsonObject);
        if (status != 0) {
            throw new RestError(RestErrorType.gerRestError(status), null);
        }
        return RestUtils.getRestResult(jsonObject);
    }



}
