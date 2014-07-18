package com.blulabellabs.code.core.data.entities;

import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.io.utils.RestKeyMap;


/**
 * Created by Alex on 8/20/13.
 */
public class ChatEntity implements ParseableEntity {

    private long id;
    private String qrcode;


    @Override
    public ChatEntity parse(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getLong(RestKeyMap.ID);
        qrcode = jsonObject.getString(RestKeyMap.QRCODE);
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

}
