package biz.softtechnics.qodeme.core.io;

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
import biz.softtechnics.qodeme.core.io.utils.RequestParams;
import biz.softtechnics.qodeme.core.io.utils.RequestType;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestErrorType;
import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.io.responses.AccountContactsResponse;
import biz.softtechnics.qodeme.core.io.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.io.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.io.responses.ChatLoadResponse;
import biz.softtechnics.qodeme.core.io.responses.ChatMessageResponse;
import biz.softtechnics.qodeme.core.io.responses.ContactAddResponse;
import biz.softtechnics.qodeme.core.io.responses.UserSettingsResponse;
import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.RestUtils;

import static biz.softtechnics.qodeme.utils.LogUtils.LOGI;
import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 12/1/13.
 */
public class RestSyncHelper {

    private static final String TAG = makeLogTag(RestSyncHelper.class);

    private static volatile RestSyncHelper instance;
    private RequestQueue mQueue;

    public static RestSyncHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (RequestQueue.class) {
                if (instance == null) {
                    instance = new RestSyncHelper(context);
                }
            }
        }
        return instance;

    }

    private RestSyncHelper(Context context){
        mQueue = new Volley().newRequestQueue(context);
    }

    public AccountCreateResponse accountCreate(String passwordMd5) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PASSWORD, passwordMd5);
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_CREATE, params);
        return new AccountCreateResponse().parse(jsonResult);
    }

    public AccountLoginResponse accountLogin(String qrcode, String passwordMd5) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.QRCODE, qrcode);
        params.put(RestKeyMap.PASSWORD, passwordMd5);
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_LOGIN, params);
        return new AccountLoginResponse().parse(jsonResult);
    }

    public VoidResponse accountLogout() throws InterruptedException, ExecutionException, JSONException, RestError {
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_LOGOUT, null);
        return new VoidResponse().parse(jsonResult);
    }

    public ContactAddResponse contactAdd(String qrcode, String publicName, String message, String location) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        //FIXME later
        params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
        //params.put(RestKeyMap.MESSAGE, message);
        //params.put(RestKeyMap.PUBLIC_NAME, publicName);
        params.put(RestKeyMap.LOCATION, location);
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.CONTACT_ADD, params);
        return new ContactAddResponse().parse(jsonResult);
    }

    public AccountContactsResponse accountContacts() throws InterruptedException, ExecutionException, JSONException, RestError {
        JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_CONTACTS, null);
        return new AccountContactsResponse().parse(jsonResult);
    }

    public ChatMessageResponse chatMessage(long chatId, String message, long unixTimeStamp) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.MESSAGE, message);
        params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
        params.put(RestKeyMap.DATETIME, String.valueOf(unixTimeStamp));
        JSONObject jsonObject =  newSyncJsonObjectRequest(RequestType.CHAT_MESSAGE, params);
        return new ChatMessageResponse().parse(jsonObject);
    }

    public ChatLoadResponse chatLoad(long chatId, int page, int limit) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
        params.put(RestKeyMap.PAGE, String.valueOf(page));
        params.put(RestKeyMap.LIMIT, String.valueOf(limit));
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CHAT_LOAD, params);
        return new ChatLoadResponse().parse(jsonObject);
    }

    public VoidResponse contactAccept(String qrcode) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_ACCEPT, params);
        return new VoidResponse().parse(jsonObject);
    }

    public VoidResponse contactReject(String qrcode) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_REJECT, params);
        return new VoidResponse().parse(jsonObject);
    }

    public VoidResponse contactBlock(String qrcode) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_BLOCK, params);
        return new VoidResponse().parse(jsonObject);
    }

    public UserSettingsResponse getUserSettings() throws InterruptedException, ExecutionException, JSONException, RestError {
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.GET_USER_SETTINGS, null);
        return new UserSettingsResponse().parse(jsonObject);
    }

    public VoidResponse setUserSettings(String message,
                                        boolean withMessage,
                                        String publicName,
                                        boolean withPublicName,
                                        boolean withAutoAccept,
                                        String location,
                                        boolean withSaveDateTime) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.MESSAGE, message);
        params.put(RestKeyMap.WITH_MESSAGE, Converter.booleanToIntString(withMessage));
        params.put(RestKeyMap.PUBLIC_NAME, publicName);
        params.put(RestKeyMap.WITH_PUBNAME, Converter.booleanToIntString(withPublicName));
        params.put(RestKeyMap.AUTO_ACCEPT, Converter.booleanToIntString(withAutoAccept));
        params.put(RestKeyMap.LOCATION, location);
        params.put(RestKeyMap.SET_TIMELOC, Converter.booleanToIntString(withSaveDateTime));
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.SET_USER_SETTINGS, params);
        return new VoidResponse().parse(jsonObject);
    }

    public VoidResponse setUserSettings() throws InterruptedException, ExecutionException, JSONException, RestError {
        QodemePreferences pref = QodemePreferences.getInstance();
        String message = pref.getMessage();
        boolean withMessage = pref.isMessageChecked();
        String publicName = pref.getPublicName();
        boolean withPublicName = pref.isPublicNameChecked();
        boolean withAutoAccept = pref.isAutoAcceptChecked();
        String location = "location";
        boolean withSaveDateTime = pref.isSaveLocationDateChecked();
        return setUserSettings(message, withMessage, publicName, withPublicName, withAutoAccept, location, withSaveDateTime);
    }

    public VoidResponse chatSetInfo(long chatId, String title, Integer color, Integer height) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.ID, String.valueOf(chatId));
        if (title != null) params.put(RestKeyMap.TITLE, title);
        if (color != null) params.put(RestKeyMap.COLOR, String.valueOf(color));
        if (height != null) params.put(RestKeyMap.CHAT_HEIGHT, String.valueOf(height));
        JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CHAT_SET_INFO, params);
        return new VoidResponse().parse(jsonObject);
    }

    public VoidResponse messageRead(long messageId) throws InterruptedException, ExecutionException, JSONException, RestError {
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.MESSAGE_ID, String.valueOf(messageId));
        newSyncJsonObjectRequest(RequestType.MESSAGE_READ, params);
        return new VoidResponse();
    }

    /**
     * Synchronous volley request
     *
     * @param requestType
     * @param params
     * @return
     */
    private JSONObject newSyncJsonObjectRequest(final RequestType requestType, final RequestParams params) throws ExecutionException, InterruptedException, JSONException, RestError {
        LOGI(TAG, String.format("Server request, type:%s, with params:[%s]", requestType, params != null ? params.toString() : ""));
        final RequestQueue requestQueue = mQueue;
        int method = Request.Method.POST;
        RequestFuture<String> future = RequestFuture.newFuture();
        String absoluteUrl = RestUtils.getAbsoluteUrl(requestType);

        StringRequest request = new StringRequest(method, absoluteUrl, future, future){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = null;
                if (params == null) {
                    map = new HashMap<String, String>();
                    map.put("void", "void");
                } else
                    map = params.getMap();
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                if (requestType != RequestType.ACCOUNT_LOGIN
                        && requestType != RequestType.ACCOUNT_CREATE){
                    String restToken = QodemePreferences.getInstance().getRestToken();
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
            RestErrorType errorType = RestErrorType.gerRestError(status);

            // Restore token and retry
            if (errorType == RestErrorType.REST_INVALID_TOKEN){
                RequestParams rParams = new RequestParams();
                rParams.put(RestKeyMap.QRCODE, QodemePreferences.getInstance().getQrcode());
                rParams.put(RestKeyMap.PASSWORD, QodemePreferences.getInstance().getPassword());
                JSONObject jo = newSyncJsonObjectRequest(RequestType.ACCOUNT_LOGIN, rParams);
                AccountLoginResponse accountLoginResponse = new AccountLoginResponse();
                accountLoginResponse.parse(jo);
                QodemePreferences.getInstance().setRestToken(accountLoginResponse.getRestToken());
                return newSyncJsonObjectRequest(requestType, params);
            }
            throw new RestError(RestErrorType.gerRestError(status), null);
        }
        return RestUtils.getRestResult(jsonObject);
    }


}
