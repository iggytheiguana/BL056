package com.blulabellabs.code.core.io.responses;

import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;


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
