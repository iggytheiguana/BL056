package biz.softtechnics.qodeme.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.io.utils.RestParcer;
import biz.softtechnics.qodeme.core.data.entities.LookupChatEntity;

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
