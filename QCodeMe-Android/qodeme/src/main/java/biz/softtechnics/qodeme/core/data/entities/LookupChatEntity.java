package biz.softtechnics.qodeme.core.data.entities;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;

/**
 * Created by Alex on 8/21/13.
 */
public class LookupChatEntity extends ChatEntity{
    private String title;
    private String tags;

    @Override
    public LookupChatEntity parse(JSONObject jsonObject) throws JSONException {
        super.parse(jsonObject);
        title = jsonObject.getString(RestKeyMap.TITLE);
        tags = jsonObject.getString(RestKeyMap.TAGS);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getTags() {
        return tags;
    }
}
