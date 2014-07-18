package com.blulabellabs.code.core.io.responses;

import com.blulabellabs.code.core.io.model.UserSettings;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Alex on 8/16/13.
 */
public class UserSettingsResponse extends BaseResponse {

    private UserSettings mSettings;

    @Override
    public UserSettingsResponse parse(JSONObject jsonObject) throws JSONException {
        mSettings = new Gson().fromJson(jsonObject.getString(RestKeyMap.SETTINGS_OBJ), UserSettings.class);
        return this;
    }

    public UserSettings getSettings() {
        return mSettings;
    }
}
