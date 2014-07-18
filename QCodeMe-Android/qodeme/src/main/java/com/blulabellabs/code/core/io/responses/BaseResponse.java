package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alex on 8/16/13.
 */
public abstract class BaseResponse {

    public abstract BaseResponse parse(JSONObject jsonObject) throws JSONException;

}
