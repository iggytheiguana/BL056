package biz.softtechnics.qodeme.core.io.responses;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.io.model.Contact;

/**
 * Created by Alex on 8/16/13.
 */
public class ContactAddResponse extends BaseResponse {

    private Contact mContact;

    @Override
    public ContactAddResponse parse(JSONObject jsonObject) throws JSONException {
        mContact = new Gson().fromJson(jsonObject.getString(RestKeyMap.CONTACT), Contact.class);
        return this;
    }

    public Contact getContact() {
        return mContact;
    }
}
