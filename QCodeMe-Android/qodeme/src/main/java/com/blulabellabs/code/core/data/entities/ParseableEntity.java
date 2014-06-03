package com.blulabellabs.code.core.data.entities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alex on 8/20/13.
 */
public interface ParseableEntity {
    ParseableEntity parse(JSONObject jsonObject) throws JSONException;

}
