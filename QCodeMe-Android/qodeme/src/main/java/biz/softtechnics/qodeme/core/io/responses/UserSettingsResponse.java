package biz.softtechnics.qodeme.core.io.responses;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.io.model.UserSettings;

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
