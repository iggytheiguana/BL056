package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.data.entities.LookupChatEntity;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.blulabellabs.code.core.io.utils.RestParcer;

import java.util.List;


/**
 * Created by Alex on 8/16/13.
 */
public class LookupResponse extends BaseResponse {

    private List<LookupChatEntity> chatList;

    @Override
    public LookupResponse parse(JSONObject jsonObject) throws JSONException {
        chatList = RestParcer.parceList(LookupChatEntity.class, jsonObject.getJSONArray(RestKeyMap.SEARCH_RESULT));
        return this;
    }

    public List<LookupChatEntity> getChatList() {
        return chatList;
    }
}
