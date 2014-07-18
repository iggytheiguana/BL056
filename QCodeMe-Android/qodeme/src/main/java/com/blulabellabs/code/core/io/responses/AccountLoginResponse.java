package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.data.entities.ContactEntity;
import com.blulabellabs.code.core.data.entities.InfoChatEntity;
import com.blulabellabs.code.core.io.utils.RestKeyMap;

import java.util.List;


/**
 * Created by Alex on 8/16/13.
 */
public class AccountLoginResponse extends BaseResponse {

    private long userId;
    private String restToken;
    private List<InfoChatEntity> chatList;
    private List<ContactEntity> contactList;


    @Override
    public AccountLoginResponse parse(JSONObject jsonObject) throws JSONException {
        userId = jsonObject.getLong(RestKeyMap.ID);
        restToken = jsonObject.getString(RestKeyMap.REST_TOKEN);
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public String getRestToken() {
        return restToken;
    }


}
