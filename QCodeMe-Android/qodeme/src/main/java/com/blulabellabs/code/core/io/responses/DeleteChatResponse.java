package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.io.utils.RestKeyMap;


/**
 * Created by Alex on 8/16/13.
 */
public class DeleteChatResponse extends BaseResponse {

    private long chat_id;

    @Override
    public DeleteChatResponse parse(JSONObject jsonObject) throws JSONException {
        chat_id = jsonObject.getLong(RestKeyMap.CHAT_ID);
        return this;
    }

    public long getChatId() {
        return chat_id;
    }
}
