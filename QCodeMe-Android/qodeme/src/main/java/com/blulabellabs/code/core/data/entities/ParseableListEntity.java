package com.blulabellabs.code.core.data.entities;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Alex on 8/20/13.
 */
public interface ParseableListEntity extends ParseableEntity {
    List<? extends ParseableListEntity> parseList(JSONArray jsonArray) throws JSONException;
}
