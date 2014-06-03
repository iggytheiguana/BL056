package com.blulabellabs.code.core.io.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 8/16/13.
 */
public class RequestParams {

    private Map<String, String> mParams = new HashMap<String, String>();

    public void put(String key, String value){
        mParams.put(key, value);
    }

    public String get(String key){
        return mParams.get(key);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (String key: mParams.keySet()){
            if (b.length() > 1)
                b.append("&");
            b.append(String.format("%s=%s", key, mParams.get(key)));
        }
        return b.toString().replace(' ', '+');
    }

    public Map getMap(){
        return mParams;
    }
}
