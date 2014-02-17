package biz.softtechnics.qodeme.core.data.entities;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.utils.RestUtils;

/**
 * Created by Alex on 8/20/13.
 */
public class ContactEntity implements ParseableEntity {

    private long id;
    private String qrcode;
    private String title;
    private int color;
    private int privateChatId;

    @Override
    public ContactEntity parse(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getLong(RestKeyMap.ID);
        qrcode = jsonObject.getString(RestKeyMap.QRCODE);
        title = RestUtils.jsonNullToObjectNull(jsonObject.getString(RestKeyMap.TITLE));
        color = jsonObject.getInt(RestKeyMap.COLOR);
        privateChatId = jsonObject.getInt(RestKeyMap.PRIVATE_CHAT_ID);
        return this;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Integer getPrivateChatId() {
        return privateChatId;
    }

    public void setPrivateChatId(int privateChatId) {
        this.privateChatId = privateChatId;
    }
}
