package biz.softtechnics.qodeme.core.io;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.ApplicationConstants;
import biz.softtechnics.qodeme.core.io.utils.RequestPackage;
import biz.softtechnics.qodeme.core.data.entities.ChatType;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.gcm.GcmController;
import biz.softtechnics.qodeme.core.io.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.io.responses.BaseResponse;
import biz.softtechnics.qodeme.core.io.responses.ResponseFactory;
import biz.softtechnics.qodeme.core.io.utils.RequestParams;
import biz.softtechnics.qodeme.core.io.utils.RequestType;
import biz.softtechnics.qodeme.core.io.utils.RestClient;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestErrorType;
import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.io.utils.RestListener;
import biz.softtechnics.qodeme.core.io.utils.RestRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/15/13
 * Time: 6:24 PM
 */
public class RestAsyncHelper implements RestClient {

    private static final int MY_SOCKET_TIMEOUT_MS = 10*1000;

    private static final String TAG = RestAsyncHelper.class.getName();

    private static RestAsyncHelper resrHelper;
    private Context context;
    private RequestQueue queue;
    private QodemePreferences pref;

    private RestAsyncHelper(Context c){
        context = c;

        queue = Volley.newRequestQueue(context);
        pref = QodemePreferences.getInstance();
    }

    public static RestClient getInstance(){
        return resrHelper;
    }

    public static void initialize(Context c){
        if (resrHelper == null)
            resrHelper = new RestAsyncHelper(c);
    }

    /**
     * Rest service call
     * @param requestType - RequestType
     * @param params  - request params
     * @param callback - UI callback
     */
    private void post(RequestType requestType,
                      RequestParams params, RestListener callback){
        RequestPackage pack = new RequestPackage(JsonObjectRequest.Method.POST, requestType, params, callback);
        if (ApplicationConstants.DEVELOP_MODE)
            Log.i(TAG, String.format("Request type:%s, params: %s", pack.getRequestType(), pack.getParams() == null ? "null" : pack.getParams().toString()));
        doRequest(pack);
    }

    /**
     * Request implementation
     * @param packege - RequestPackage container
     */
    private void doRequest(RequestPackage packege){
        packege.countTries();
        RestRequest restRequest = new RestRequest(context, packege, new RestRequest.Listener(){

            @Override
            public void onResponse(JSONObject jsonObject, RequestPackage packege) {
                if (ApplicationConstants.DEVELOP_MODE)
                    Log.i(TAG, String.format("Response type:%s   %s", packege.getRequestType(), jsonObject.toString()));
                if (!packege.getCallback().isCanceled()){
                    int status = -1;
                    try {
                        status = jsonObject.getInt(RestKeyMap.STATUS);
                    } catch (JSONException e) {
                        RestErrorType errorType = RestErrorType.REST_PARSE_STATUS;
                        Log.e(TAG, RestErrorType.getMessage(context, errorType));
                        packege.getCallback().onServiceError(new RestError().setErrorType(errorType));
                        return;
                    }
                    if (status == 0){
                        // Status ok
                        try {
                            BaseResponse response = ResponseFactory.getResponse(packege.getRequestType(), jsonObject);
                            switch (packege.getRequestType()){
                                case ACCOUNT_LOGIN:
                                    pref.setQrcode(packege.getParams().get(RestKeyMap.QRCODE));
                                    pref.setPassword(packege.getParams().get(RestKeyMap.PASSWORD));
                                    pref.setLogged(true);
                                    pref.setRestToken(((AccountLoginResponse)response).getRestToken());
                                    GcmController.getInstance(context).updateToken();
                                    break;
                                case ACCOUNT_LOGOUT:
                                    pref.setLogged(false);
                                    break;
                                case REGISTER_TOKEN:
                                    pref.setGcmTokenSycnWithRest(!TextUtils.isEmpty(packege.getParams().get(RestKeyMap.PUSH_TOKEN)));
                                    break;
                                default:
                                    break;
                            }
                            packege.getCallback().onResponse(response);
                        } catch (JSONException e) {
                            RestErrorType errorType = RestErrorType.REST_PARSE_RESULT;
                            Log.e(TAG, String.format("%s; type= %s; jsonObject=[%s]", RestErrorType.getMessage(context, errorType), packege.getRequestType().toString(), jsonObject), e);
                            packege.getCallback().onServiceError(new RestError().setErrorType(errorType).setServerMsg(RestErrorType.getMessage(context, errorType)));
                            return;
                        }
                    } else {
                        // Rest error
                        RestErrorType errorType = RestErrorType.gerRestError(status);
                        Log.e(TAG, jsonObject.toString());
                        //Log.e(TAG, RestErrorType.getMessage(context, errorType));
                        Log.e(TAG, errorType.toString());
                        switch (errorType){
                            case REST_INVALID_TOKEN:
                                if (packege.getTries() < 2){
                                    updateTokenAndRetry(packege);
                                    return;
                                }
                                break;
                        }
                        String errorStr = null;
                        try {
                            errorStr = jsonObject.getString(RestKeyMap.ERROR);
                        } catch (JSONException e) {
                            Log.e(TAG, RestErrorType.getMessage(context, RestErrorType.REST_PARSE_STATUS));
                        }
                        packege.getCallback().onServiceError(new RestError(errorType, RestErrorType.getMessage(context, errorType)));
                        return;
                    }
                }
            }

            @Override
            public void onError(VolleyError volleyError, RequestPackage packege) {
                if (!packege.getCallback().isCanceled()){
                    packege.getCallback().onNetworkError(volleyError);
                }
            }
        });


        restRequest.getVolleyRequest().setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(restRequest.getVolleyRequest());
    }

    private void updateTokenAndRetry(final RequestPackage packege){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.QRCODE, pref.getQrcode());
        params.put(RestKeyMap.PASSWORD, pref.getPassword());
        post(RequestType.ACCOUNT_LOGIN, params, new RestListener() {

            @Override
            public void onResponse(BaseResponse response) {
                doRequest(packege);
            }

            @Override
            public void onServiceError(RestError error) {
                packege.getCallback().onServiceError(error);
            }

            @Override
            public void onNetworkError(VolleyError error) {
                super.onNetworkError(error);
                packege.getCallback().onNetworkError(error);
            }
        });

    }

    /**
     * Create account
     * @param passwordMd5 - should be md5 of plain text password
     * @param callback
     * return service token in callback
     */
    public void accountCreate(String passwordMd5, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PASSWORD, passwordMd5);
        post(RequestType.ACCOUNT_CREATE, params, callback);
    }

    /**
     * Login to service
     * @param qrcode - login
     * @param passwordMd5 - should be md5 of plain text password
     * @param callback
     * return service token in callback
     */
    public void accountLogin(String qrcode, String passwordMd5, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.QRCODE, qrcode);
        params.put(RestKeyMap.PASSWORD, passwordMd5);
        post(RequestType.ACCOUNT_LOGIN, params, callback);
    }

    /**
     * Login with saved credentials
     * @param callback
     */
    public void accountLogin(RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.QRCODE, pref.getQrcode());
        params.put(RestKeyMap.PASSWORD, pref.getPassword());
        post(RequestType.ACCOUNT_LOGIN, params, callback);
    }

    /**
     * Logout
     * @param callback
     */
    public void accountLogout(RestListener callback){
        post(RequestType.ACCOUNT_LOGOUT, null, callback);
    }

    /**
     * Get contacts and chats
     * @param callback
     */
    public void accountContacts(RestListener callback){
        post(RequestType.ACCOUNT_CONTACTS, null, callback);
    }

    /**
     * Method is using to create group chats
     * @param chatType - type=1 (PRIVATE_GROUP) или 2 (PUBLIC_GROUP)
     * @param title
     * @param tags
     * @param color
     * @param callback
     */
    public void chatCreate(ChatType chatType, String title, String tags, int color, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.CHAT_TYPE, String.valueOf(chatType.getValue()));
        params.put(RestKeyMap.TITLE, title);
        params.put(RestKeyMap.TAGS, tags);
        params.put(RestKeyMap.COLOR, String.valueOf(color));
        post(RequestType.CHAT_CREATE, params, callback);
    }

    /**
     * Set chat info
     * This settings are storing individually for each user.
     * @param chatId
     * @param title
     * @param color
     * @param callback
     */
    public void chatSetInfo(long chatId, String title, int color, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.ID, String.valueOf(chatId));
        params.put(RestKeyMap.TITLE, title);
        params.put(RestKeyMap.COLOR, String.valueOf(color));
        post(RequestType.CHAT_SET_INFO, params, callback);
    }

    /**
     * Add member to the chat
     * @param chatId
     * @param qrcode
     * @param callback
     */
    public void chatAddMember(long chatId, String qrcode, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
        params.put(RestKeyMap.MEMBER_QRCODE, qrcode);
        post(RequestType.CHAT_ADD_MEMBER, params, callback);
    }

    /**
     * Send message to the chat
     * @param chatId
     * @param message
     * @param callback
     */
    public void chatMessage(long chatId, String message, long unixTimeStamp, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
        params.put(RestKeyMap.MESSAGE, message);
        params.put(RestKeyMap.DATETIME, String.valueOf(unixTimeStamp));
        post(RequestType.CHAT_MESSAGE, params, callback);
    }

    /**
     * Load chat details
     * @param chatId
     * @param page - (default=0)
     * @param limit - limit on the page (default=25)
     * @param callback
     */
    public void chatLoad(long chatId, int page, int limit, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
        post(RequestType.CHAT_LOAD, params, callback);
    }

    /**
     * Add contact (by QR code) and create private chat for both members
     * @param contactCqroce
     * @param callback
     */
    public void contactAdd(String contactCqroce, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PARTNER_QRCODE, contactCqroce);
        post(RequestType.CONTACT_ADD, params, callback);
    }

    /**
     * Remove contact
     * @param contactId
     * @param callback
     */
    public void contactRemove(long contactId, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.CONTACT_ID, String.valueOf(contactId));
        post(RequestType.CONTACT_REMOVE, params, callback);
    }

    /**
     * Drop member from chat
     * @param chatId
     * @param memberQrcode
     * @param callback
     */
    public void chatDropMember(long chatId, String memberQrcode, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
        params.put(RestKeyMap.QRCODE, memberQrcode);
        post(RequestType.CHAT_DROP_MEMBER, params, callback);
    }

    /**
     * Searching public chats
     * @param searchQuery - by title and hash tags
     * @param callback
     */
    public void lookup(String searchQuery, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.SEARCH_QUERY, searchQuery);
        post(RequestType.LOOKUP, params, callback);
    }

    /**
     * Send GCM token to the server
     * @param gcmToken
     * @param callback
     */
    public void registerToken(String gcmToken, RestListener callback){
        RequestParams params = new RequestParams();
        params.put(RestKeyMap.PUSH_TOKEN, gcmToken);
        post(RequestType.REGISTER_TOKEN, params, callback);
    }


}
