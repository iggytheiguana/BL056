package biz.softtechnics.qodeme.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.data.entities.DetailChatEntity;

/**
 * Created by Alex on 8/16/13.
 */
public class ChatCreateResponse extends BaseResponse {

    private DetailChatEntity chat;

    @Override
    public ChatCreateResponse parse(JSONObject jsonObject) throws JSONException {
        chat = new DetailChatEntity().parse(jsonObject);
        return this;
    }

    public DetailChatEntity getChat() {
        return chat;
    }
}
