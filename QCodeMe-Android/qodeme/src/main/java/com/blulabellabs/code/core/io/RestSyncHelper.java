package com.blulabellabs.code.core.io;

import static com.blulabellabs.code.utils.LogUtils.LOGI;
import static com.blulabellabs.code.utils.LogUtils.makeLogTag;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.responses.AccountContactsResponse;
import com.blulabellabs.code.core.io.responses.AccountCreateResponse;
import com.blulabellabs.code.core.io.responses.AccountLoginResponse;
import com.blulabellabs.code.core.io.responses.ChatLoadResponse;
import com.blulabellabs.code.core.io.responses.ChatMessageResponse;
import com.blulabellabs.code.core.io.responses.ContactAddResponse;
import com.blulabellabs.code.core.io.responses.DeleteMessageResponse;
import com.blulabellabs.code.core.io.responses.SetFavoriteResponse;
import com.blulabellabs.code.core.io.responses.SetFlaggedResponse;
import com.blulabellabs.code.core.io.responses.UploadImageResponse1;
import com.blulabellabs.code.core.io.responses.UserSettingsResponse;
import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.core.io.utils.RequestParams;
import com.blulabellabs.code.core.io.utils.RequestType;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestErrorType;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.LatLonCity;
import com.blulabellabs.code.utils.RestUtils;

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

	private RestSyncHelper(Context context) {
		mQueue = new Volley().newRequestQueue(context);
	}

	public AccountCreateResponse accountCreate(String passwordMd5) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.PASSWORD, passwordMd5);
		JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_CREATE, params);
		return new AccountCreateResponse().parse(jsonResult);
	}

	public AccountLoginResponse accountLogin(String qrcode, String passwordMd5)
			throws InterruptedException, ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.QRCODE, qrcode);
		params.put(RestKeyMap.PASSWORD, passwordMd5);
		JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_LOGIN, params);
		return new AccountLoginResponse().parse(jsonResult);
	}

	public VoidResponse accountLogout() throws InterruptedException, ExecutionException,
			JSONException, RestError {
		JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_LOGOUT, null);
		return new VoidResponse().parse(jsonResult);
	}

	public ContactAddResponse contactAdd(String qrcode, String publicName, String message,
			String location) throws InterruptedException, ExecutionException, JSONException,
			RestError {
		RequestParams params = new RequestParams();
		// FIXME later
		params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
		params.put(RestKeyMap.MESSAGE, message);
		params.put(RestKeyMap.PUBLIC_NAME, publicName);
		params.put(RestKeyMap.LOCATION, location);
		params.put(RestKeyMap.LATITUDE, "");
		params.put(RestKeyMap.LONGITUDE, "");

		JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.CONTACT_ADD, params);
		return new ContactAddResponse().parse(jsonResult);
	}

	public AccountContactsResponse accountContacts() throws InterruptedException,
			ExecutionException, JSONException, RestError {
		JSONObject jsonResult = newSyncJsonObjectRequest(RequestType.ACCOUNT_CONTACTS, null);
		return new AccountContactsResponse().parse(jsonResult);
	}

	public ChatMessageResponse chatMessage(long chatId, String message, long unixTimeStamp,
			String photoUrl, int hashPhoto, long replyTo_Id, String latitude, String longitude,
			String senderName, String dateString, int is_search) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.MESSAGE, message);
		params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
		// params.put(RestKeyMap.DATETIME, String.valueOf(unixTimeStamp));
		params.put(RestKeyMap.DATETIME, dateString);
		params.put(RestKeyMap.PHOTURL, photoUrl);
		params.put(RestKeyMap.HAS_PHOTO, String.valueOf(hashPhoto));
		params.put(RestKeyMap.REPLY_TO_ID, String.valueOf(replyTo_Id));
		params.put(RestKeyMap.LATITUDE, latitude);
		params.put(RestKeyMap.LONGITUDE, longitude);
		params.put(RestKeyMap.SENDER_NAME, senderName);
		params.put("is_search", String.valueOf(is_search));
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CHAT_MESSAGE, params);
		return new ChatMessageResponse().parse(jsonObject);
	}

	public UploadImageResponse1 chatImage(long messageId, String imageString)
			throws InterruptedException, ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.IMAGE, imageString);
		params.put(RestKeyMap.MESSAGE_ID, String.valueOf(messageId));
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.UPLOAD_IMAGE, params);
		return new UploadImageResponse1().parse(jsonObject);
	}

	public ChatLoadResponse chatLoad(long chatId, int page, int limit) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.CHAT_ID, String.valueOf(chatId));
		params.put(RestKeyMap.PAGE, String.valueOf(page));
		params.put(RestKeyMap.LIMIT, String.valueOf(limit));
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CHAT_LOAD, params);
		return new ChatLoadResponse().parse(jsonObject);
	}

	public VoidResponse contactAccept(String qrcode) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_ACCEPT, params);
		return new VoidResponse().parse(jsonObject);
	}

	public VoidResponse contactReject(String qrcode) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_REJECT, params);
		return new VoidResponse().parse(jsonObject);
	}

	public VoidResponse contactBlock(String qrcode) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.PARTNER_QRCODE, qrcode);
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CONTACT_BLOCK, params);
		return new VoidResponse().parse(jsonObject);
	}

	public UserSettingsResponse getUserSettings() throws InterruptedException, ExecutionException,
			JSONException, RestError {
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.GET_USER_SETTINGS, null);
		return new UserSettingsResponse().parse(jsonObject);
	}

	public VoidResponse setUserSettings(String message, boolean withMessage, String publicName,
			boolean withPublicName, boolean withAutoAccept, String location, String latitude,
			String longitude, boolean withSaveDateTime, String status) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.MESSAGE, message);
		params.put(RestKeyMap.WITH_MESSAGE, Converter.booleanToIntString(withMessage));
		params.put(RestKeyMap.PUBLIC_NAME, publicName);
		params.put(RestKeyMap.WITH_PUBNAME, Converter.booleanToIntString(withPublicName));
		params.put(RestKeyMap.AUTO_ACCEPT, Converter.booleanToIntString(withAutoAccept));
		params.put(RestKeyMap.LOCATION, location);
		params.put(RestKeyMap.LATITUDE, String.valueOf(latitude));
		params.put(RestKeyMap.LONGITUDE, String.valueOf(longitude));
		params.put(RestKeyMap.SET_TIMELOC, Converter.booleanToIntString(withSaveDateTime));
		params.put("status", status);
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.SET_USER_SETTINGS, params);
		return new VoidResponse().parse(jsonObject);
	}

	public VoidResponse setUserSettings() throws InterruptedException, ExecutionException,
			JSONException, RestError {
		QodemePreferences pref = QodemePreferences.getInstance();
		String message = pref.getMessage();
		boolean withMessage = pref.isMessageChecked();
		String publicName = pref.getPublicName();
		boolean withPublicName = pref.isPublicNameChecked();
		boolean withAutoAccept = pref.isAutoAcceptChecked();
		String location = "location";
		boolean withSaveDateTime = pref.isSaveLocationDateChecked();
		LatLonCity latLonCity = pref.getLastLocation();
		String latitude = "0";
		String longitude = "0";
		if (latLonCity != null) {
			latitude = latLonCity.getLatitude();
			longitude = latLonCity.getLongitude();
			location = latLonCity.getCity();
		}

		String status = pref.getStatus();

		return setUserSettings(message, withMessage, publicName, withPublicName, withAutoAccept,
				location, latitude, longitude, withSaveDateTime, status);
	}

	public VoidResponse chatSetInfo(long chatId, String title, Integer color, Integer height,
			String description, int is_locked, String status, String tags, Integer chat_color,
			String lat, String lng) throws InterruptedException, ExecutionException, JSONException,
			RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.ID, String.valueOf(chatId));
		if (title != null)
			params.put(RestKeyMap.TITLE, title);
		if (color != null)
			params.put(RestKeyMap.COLOR, String.valueOf(color));
		if (height != null)
			params.put(RestKeyMap.CHAT_HEIGHT, String.valueOf(height));
		if (description != null)
			params.put(RestKeyMap.DESCRIPTION, description);
		params.put(RestKeyMap.IS_LOCKED, String.valueOf(is_locked));
		if (status != null)
			params.put(RestKeyMap.STATUS, status);
		if (tags != null)
			params.put(RestKeyMap.TAGS, tags);
		params.put(RestKeyMap.CHAT_TYPE, String.valueOf(0));
		params.put("chat_color", String.valueOf(chat_color));
		params.put(RestKeyMap.LATITUDE, lat);
		params.put(RestKeyMap.LONGITUDE, lng);
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.CHAT_SET_INFO, params);
		return new VoidResponse().parse(jsonObject);
	}

	public VoidResponse messageRead(long messageId) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.MESSAGE_ID, String.valueOf(messageId));
		newSyncJsonObjectRequest(RequestType.MESSAGE_READ, params);
		return new VoidResponse();
	}

	/**
	 * toggle the Flagged message
	 */
	public SetFlaggedResponse setFlagged(long message_id, int is_flagged, long chat_id)
			throws InterruptedException, ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.MESSAGE_ID, String.valueOf(message_id));
		params.put(RestKeyMap.IS_FLAGGED, String.valueOf(is_flagged));
		params.put(RestKeyMap.CHAT_ID, String.valueOf(chat_id));
		// post(RequestType.SET_FLAGGED, params, callback);
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.SET_FLAGGED, params);
		return new SetFlaggedResponse().parse(jsonObject);
	}

	/**
	 * toggle the Favorite message
	 */
	public SetFavoriteResponse setFavorite(String date, int is_favorite, long chat_id)
			throws InterruptedException, ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put("is_favorite", String.valueOf(is_favorite));
		params.put(RestKeyMap.CHAT_ID, String.valueOf(chat_id));
		params.put("date_time", date);
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.SET_FAVORITE, params);
		return new SetFavoriteResponse().parse(jsonObject);
	}

	/**
	 * delete message
	 */
	public DeleteMessageResponse deleteMessage(long messageId, long chat_id) throws InterruptedException,
			ExecutionException, JSONException, RestError {
		RequestParams params = new RequestParams();
		params.put(RestKeyMap.MESSAGE_ID, String.valueOf(messageId));
		params.put(RestKeyMap.CHAT_ID, String.valueOf(chat_id));
		JSONObject jsonObject = newSyncJsonObjectRequest(RequestType.DELETE_MESSAGE, params);
		return new DeleteMessageResponse().parse(jsonObject);
	}

	/**
	 * Synchronous volley request
	 * 
	 * @param requestType
	 * @param params
	 * @return
	 */
	private JSONObject newSyncJsonObjectRequest(final RequestType requestType,
			final RequestParams params) throws ExecutionException, InterruptedException,
			JSONException, RestError {
		LOGI(TAG, String.format("Server request, type:%s, with params:[%s]", requestType,
				params != null ? params.toString() : ""));
		final RequestQueue requestQueue = mQueue;
		int method = Request.Method.POST;
		RequestFuture<String> future = RequestFuture.newFuture();
		String absoluteUrl = RestUtils.getAbsoluteUrl(requestType);
		Log.d("Image Url", RestUtils.getAbsoluteUrl(requestType) + "");

		StringRequest request = new StringRequest(method, absoluteUrl, future, future) {
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
						&& requestType != RequestType.ACCOUNT_CREATE) {
					String restToken = QodemePreferences.getInstance().getRestToken();
					map.put(RestKeyMap.X_AUTHTOKEN, restToken);
				}
				return map;
			}
		};

		request.setRetryPolicy(new DefaultRetryPolicy(ApplicationConstants.REST_SOCKET_TIMEOUT_MS,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		requestQueue.add(request);

		String json = future.get();

		LOGI(TAG, String.format("Server response, type:%s:[%s]", requestType, json));

		JSONObject jsonObject = new JSONObject(json);
		int status = RestUtils.getRestStatus(jsonObject);
		if (status != 0) {
			RestErrorType errorType = RestErrorType.gerRestError(status);

			// Restore token and retry
			if (errorType == RestErrorType.REST_INVALID_TOKEN) {
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
