package biz.softtechnics.qodeme.core.io.responses;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.model.ChatLoad;

/**
 * Created by Alex on 8/16/13.
 */
public class ChatLoadResponse extends BaseResponse {

    //private DetailChatEntity chat;
    //private List<MessageEntity> messageList;
    private ChatLoad chatLoad;

    @Override
    public ChatLoadResponse parse(JSONObject jsonObject) throws JSONException {
        //chat = new DetailChatEntity().parse(jsonObject);
        //messageList = RestParcer.parceList(MessageEntity.class, jsonObject.getJSONArray(RestKeyMap.MESSAGE_LIST));
        chatLoad = new Gson().fromJson(jsonObject.toString(), ChatLoad.class);
        return this;
    }

    public ChatLoad getChatLoad() {
        return chatLoad;
    }

}
