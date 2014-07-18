package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.data.entities.DetailChatEntity;


/**
 * Created by Alex on 8/16/13.
 */
public class ChatAddMemberResponse extends BaseResponse {

    private DetailChatEntity chat;


    @Override
    public ChatAddMemberResponse parse(JSONObject jsonObject) throws JSONException {
        chat = new DetailChatEntity().parse(jsonObject);
        return this;
    }

    public DetailChatEntity getChat() {
        return chat;
    }
}
