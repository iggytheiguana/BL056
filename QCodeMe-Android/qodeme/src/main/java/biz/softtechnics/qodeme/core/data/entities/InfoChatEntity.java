package biz.softtechnics.qodeme.core.data.entities;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;

/**
 * Created by Alex on 8/20/13.
 */
public class InfoChatEntity extends ChatEntity{

    private String title;
    private String tags;
    private int color;

    @Override
    public InfoChatEntity parse(JSONObject jsonObject) throws JSONException {
        super.parse(jsonObject);
        title = jsonObject.getString(RestKeyMap.TITLE);
        tags = jsonObject.getString(RestKeyMap.TAGS);
        color = jsonObject.getInt(RestKeyMap.COLOR);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
