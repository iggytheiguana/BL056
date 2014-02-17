package biz.softtechnics.qodeme.core.io.responses;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.io.utils.RestParcer;
import biz.softtechnics.qodeme.core.data.entities.ChatEntity;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Contacts;

/**
 * Created by Alex on 8/16/13.
 */
public class AccountContactsResponse extends BaseResponse {

    private List<ChatEntity> mChatList;
    public Contacts mContacts;

    @Override
    public AccountContactsResponse parse(JSONObject jsonObject) throws JSONException {
        mChatList = RestParcer.parceList(ChatEntity.class, jsonObject.getJSONArray(RestKeyMap.CHAT_LIST));
        mContacts = new Gson().fromJson(jsonObject.toString(), Contacts.class);
        return this;
    }

    public List<ChatEntity> getChatList() {
        return mChatList;
    }

    public List<Contact> getContactList() { return mContacts.getList(); }
}
