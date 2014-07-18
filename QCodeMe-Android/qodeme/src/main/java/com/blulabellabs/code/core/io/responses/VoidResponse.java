package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alex on 8/16/13.
 * Void response
 */
public final class VoidResponse extends BaseResponse {

    @Override
    public VoidResponse parse(JSONObject jsonObject) throws JSONException {
        return this;
    }

}
