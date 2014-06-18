package com.blulabellabs.code.core.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.blulabellabs.code.core.io.model.UserSettings;
import com.blulabellabs.code.core.io.responses.UserSettingsResponse;
import com.blulabellabs.code.utils.LatLonCity;

public class QodemePreferences extends CommonPreferences {

	private static final String PREF_INIT = "pref_init";
	private static final String PREF_APP_VERSION = "pref_app_version";
	private static final String PREF_GCM_TOKEN_EXPIRATION_TIME = "pref_gcm_token_expiration_time";
	private static final String PREF_GCM_TOKEN = "pref_gcm_token";
	private static final String PREF_REST_TOKEN = "pref_rest_token";
	private static final String PREF_QRCODE = "pref_qrcode";
	private static final String PREF_PASSWORD = "pref_password";
	private static final String PREF_GCM_TOKEN_SYCN_WITH_REST = "pref_gcm_token_sycn_with_rest";
	private static final String PREF_LOGGED = "pref_logged";
	private static final String PREF_DB_NAME = "pref_db_name";
	private static final String PREF_SYNC_ADAPTER_SETUP_COMPLETE = "pref_sync_adapter_setup_complete";
	private static final String PREF_EDIT_PUBLIC_NAME = "pref_edit_name";
	private static final String PREF_CHECKBOX_PUBLIC_NAME = "pref_checkbox_name";
	private static final String PREF_EDIT_MESSAGE = "pref_edit_message";
	private static final String PREF_CHECKBOX_MESSAGE = "pref_checkbox_message";
	private static final String PREF_CHECKBOX_SAVE_LOCATION_DATE = "pref_checkbox_save_location_date";
	private static final String PREF_CHECKBOX_AUTO_ACCEPT = "pref_checkbox_auto_accept";
	private static final String PREF_USER_SETTINGS_UPTODATE = "pref_user_settings_updated";
	private static final String PREF_LAST_LOCATION_LAT = "pref_last_location_lat";
	private static final String PREF_LAST_LOCATION_LON = "pref_last_location_lon";
	private static final String PREF_LAST_LOCATION_CITY = "pref_last_location_city";
	private static final String DEFAULT_PREF_DB_NAME = "main.db";
	private static final String DEFAULT_PREF_EDIT_PUBLIC_NAME = "User";
	private static final boolean DEFAULT_PREF_CHECKBOX_PUBLIC_NAME = false;
	private static final String DEFAULT_PREF_EDIT_MESSAGE = "Please add me to your contact list.";
	private static final boolean DEFAULT_PREF_CHECKBOX_MESSAGE = true;
	private static final boolean DEFAULT_PREF_CHECKBOX_SAVE_LOCATION_DATE = true;
	private static final boolean DEFAULT_PREF_CHECKBOX_AUTO_ACCEPT = false;
	private static final boolean DEFAULT_PREF_USER_SETTINGS_UPTODATE = false;
	private static final String PUBLIC_GROUP_CHAT_ID = "public_group_chat_id";
	private static final String PREF_EDIT_STATUS = "pref_edit_status";
	private static final String PREF_LAST_LOCATION_LATITUDE = "pref_last_location_latitude";
	private static final String PREF_LAST_LOCATION_LONGITUDE = "pref_last_location_longitude";

	private static QodemePreferences instance;
	@SuppressWarnings("unused")
	private Context context;

	/**
	 * Run initialization in Application.onCreate
	 * 
	 * @param context
	 *            - ApplicationContext
	 */
	public static void initialize(Context context) {
		if (instance == null) {
			instance = new QodemePreferences(context);
			if (!instance.isPrefInit())
				instance.resetAllSettings();
		}
	}

	public void resetAllSettings() {
		SharedPreferences.Editor ed = getEditor();
		commit(ed);
	}

	protected QodemePreferences(Context context) {
		super(context);
		this.context = context;
	}

	private boolean isPrefInit() {
		return get(PREF_INIT, false);
	}

	@Override
	public String getName() {
		return QodemePreferences.class.getSimpleName();
	}

	public static QodemePreferences getInstance() {
		return instance;
	}

	public int getAppVersion() {
		return get(PREF_APP_VERSION, -1);
	}

	public String getGcmToken() {
		return get(PREF_GCM_TOKEN, null);
	}

	public long getGcmTokenExpirationTime() {
		return get(PREF_GCM_TOKEN_EXPIRATION_TIME, -1L);
	}

	public void setAppVersion(int value) {
		set(PREF_APP_VERSION, value);
	}

	public void setGcmToken(String value) {
		set(PREF_GCM_TOKEN, value);
	}

	public void setGcmTokenExpirationTime(long value) {
		set(PREF_GCM_TOKEN_EXPIRATION_TIME, value);
	}

	public String getRestToken() {
		return get(PREF_REST_TOKEN, null);
	}

	public void setRestToken(String value) {
		set(PREF_REST_TOKEN, value);
	}

	public String getQrcode() {
		return get(PREF_QRCODE, "null");
	}

	public void setQrcode(String value) {
		set(PREF_QRCODE, value);
	}

	public String getPassword() {
		return get(PREF_PASSWORD, null);
	}

	public void setPassword(String value) {
		set(PREF_PASSWORD, value);
	}

	public boolean isGcmTokenSycnWithRest() {
		return get(PREF_GCM_TOKEN_SYCN_WITH_REST, false);
	}

	public void setGcmTokenSycnWithRest(boolean value) {
		set(PREF_GCM_TOKEN_SYCN_WITH_REST, value);
	}

	public boolean isLogged() {
		return get(PREF_LOGGED, false);
	}

	public void setLogged(boolean value) {
		set(PREF_LOGGED, value);
	}

	public void setLoggedInResult(String qrCode, String password, String restToken) {
		SharedPreferences.Editor ed = getEditor();
		ed.putBoolean(PREF_LOGGED, true);
		ed.putString(PREF_QRCODE, qrCode);
		ed.putString(PREF_PASSWORD, password);
		ed.putString(PREF_REST_TOKEN, restToken);
		commit(ed);
	}

	public String getDbName() {
		return get(PREF_DB_NAME, DEFAULT_PREF_DB_NAME);
	}

	public void setDbName(String value) {
		set(PREF_DB_NAME, value);
	}

	public boolean isSyncAdapterSetupComplete() {
		return get(PREF_SYNC_ADAPTER_SETUP_COMPLETE, false);
	}

	public void setSyncAdapterSetupComplete(boolean value) {
		set(PREF_SYNC_ADAPTER_SETUP_COMPLETE, value);
	}

	public String getPublicName() {
		return get(PREF_EDIT_PUBLIC_NAME, "");
	}

	public void setPublicName(String value) {
		set(PREF_EDIT_PUBLIC_NAME, value);
	}

	public String getStatus() {
		return get(PREF_EDIT_STATUS, "");
	}

	public void setStatus(String value) {
		set(PREF_EDIT_STATUS, value);
	}

	public boolean isPublicNameChecked() {
		return get(PREF_CHECKBOX_PUBLIC_NAME, false);
	}

	public void setPublicNameChecked(boolean value) {
		set(PREF_CHECKBOX_PUBLIC_NAME, value);
	}

	public String getMessage() {
		return get(PREF_EDIT_MESSAGE, "");
	}

	public void setMessage(String value) {
		set(PREF_EDIT_MESSAGE, value);
	}

	public boolean isMessageChecked() {
		return get(PREF_CHECKBOX_MESSAGE, false);
	}

	public void setMessageChecked(boolean value) {
		set(PREF_CHECKBOX_MESSAGE, value);
	}

	public boolean isSaveLocationDateChecked() {
		return get(PREF_CHECKBOX_SAVE_LOCATION_DATE, false);
	}

	public void setSaveLocationDateChecked(boolean value) {
		set(PREF_CHECKBOX_SAVE_LOCATION_DATE, value);
	}

	public boolean isAutoAcceptChecked() {
		return get(PREF_CHECKBOX_AUTO_ACCEPT, false);
	}

	public void setAutoAcceptChecked(boolean value) {
		set(PREF_CHECKBOX_AUTO_ACCEPT, value);
	}

	public boolean isUserSettingsUpToDate() {
		return get(PREF_USER_SETTINGS_UPTODATE, DEFAULT_PREF_USER_SETTINGS_UPTODATE);
	}

	public void setUserSettingsUpToDate(boolean value) {
		set(PREF_USER_SETTINGS_UPTODATE, value);
	}

	public void initUserSettings() {
		SharedPreferences.Editor ed = getEditor();
		ed.putString(PREF_EDIT_PUBLIC_NAME, DEFAULT_PREF_EDIT_PUBLIC_NAME);
		ed.putBoolean(PREF_CHECKBOX_PUBLIC_NAME, DEFAULT_PREF_CHECKBOX_PUBLIC_NAME);
		ed.putString(PREF_EDIT_MESSAGE, DEFAULT_PREF_EDIT_MESSAGE);
		ed.putBoolean(PREF_CHECKBOX_MESSAGE, DEFAULT_PREF_CHECKBOX_MESSAGE);
		ed.putBoolean(PREF_CHECKBOX_SAVE_LOCATION_DATE, DEFAULT_PREF_CHECKBOX_SAVE_LOCATION_DATE);
		ed.putBoolean(PREF_CHECKBOX_AUTO_ACCEPT, DEFAULT_PREF_CHECKBOX_AUTO_ACCEPT);
		ed.putBoolean(PREF_USER_SETTINGS_UPTODATE, DEFAULT_PREF_USER_SETTINGS_UPTODATE);
		commit(ed);
	}

	public void setUserSettingsResponse(UserSettingsResponse response) {
		UserSettings us = response.getSettings();
		SharedPreferences.Editor ed = getEditor();
		ed.putString(PREF_EDIT_PUBLIC_NAME, us.publicName);
		ed.putBoolean(PREF_CHECKBOX_PUBLIC_NAME, us.withPublicName == 1);
		ed.putString(PREF_EDIT_MESSAGE, us.message);
		ed.putBoolean(PREF_CHECKBOX_MESSAGE, us.withMessage == 1);
		ed.putBoolean(PREF_CHECKBOX_SAVE_LOCATION_DATE, us.seveTimeLocation == 1);
		ed.putBoolean(PREF_CHECKBOX_AUTO_ACCEPT, us.withAutoAccept == 1);
		ed.putBoolean(PREF_USER_SETTINGS_UPTODATE, true);
		ed.putString(PREF_EDIT_STATUS, us.status);
		commit(ed);
	}

	public LatLonCity getLastLocation() {
		LatLonCity result = null;
		int lat = get(PREF_LAST_LOCATION_LAT, -1);
		int lon = get(PREF_LAST_LOCATION_LAT, -1);
		String latitude = get(PREF_LAST_LOCATION_LATITUDE, "0");
		String longitude = get(PREF_LAST_LOCATION_LONGITUDE, "0");
		String city = get(PREF_LAST_LOCATION_CITY, null);
		if (lat != -1) {
			result = new LatLonCity();
			result.setLat(lat);
			result.setLon(lon);
			result.setCity(city);
			result.setLatitude(latitude);
			result.setLongitude(longitude);
		}
		return result;
	}

	public void setLastLocation(LatLonCity value) {
		SharedPreferences.Editor ed = getEditor();
		ed.putInt(PREF_LAST_LOCATION_LAT, value.getLat());
		ed.putInt(PREF_LAST_LOCATION_LON, value.getLon());
		ed.putString(PREF_LAST_LOCATION_LATITUDE, value.getLatitude());
		ed.putString(PREF_LAST_LOCATION_LONGITUDE, value.getLongitude());
		ed.putString(PREF_LAST_LOCATION_CITY, value.getCity());
		commit(ed);
	}

	public void setNewPublicGroupChatId(long chatId) {
		SharedPreferences.Editor ed = getEditor();
		ed.putLong(PUBLIC_GROUP_CHAT_ID, chatId);
		commit(ed);
	}

	public long getNewPublicGroupChatId() {
		return get(PUBLIC_GROUP_CHAT_ID, -1l);
	}
}
