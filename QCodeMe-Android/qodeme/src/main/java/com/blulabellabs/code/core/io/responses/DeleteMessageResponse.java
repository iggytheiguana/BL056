package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.io.utils.RestKeyMap;


/**
 * Created by Alex on 8/16/13.
 */
public class DeleteMessageResponse extends BaseResponse {

    private long messageId;

    @Override
    public DeleteMessageResponse parse(JSONObject jsonObject) throws JSONException {
        messageId = jsonObject.getLong(RestKeyMap.MESSAGE_ID);
        return this;
    }

    public long getMessageId() {
        return messageId;
    }
}
