package com.blulabellabs.code.core.io.utils;

import org.json.JSONArray;
import org.json.JSONException;

import com.blulabellabs.code.core.data.entities.ParseableEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Alex on 8/16/13.
 */
public class RestParcer {


    public static <T extends ParseableEntity> List<T> parceList(Class<T> targetClass, JSONArray jsonArray) throws JSONException{
        List<T> list = null;
        try{
            list = new ArrayList<T>();
        for (int i = 0; i < jsonArray.length(); i++) {
            T entity = (T)targetClass.newInstance().parse(jsonArray.getJSONObject(i));
            list.add(entity);
        }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return list;

    }

}
