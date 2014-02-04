package biz.softtechnics.qodeme.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.data.entities.ContactEntity;
import biz.softtechnics.qodeme.core.data.entities.InfoChatEntity;

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
