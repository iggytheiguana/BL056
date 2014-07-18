package com.blulabellabs.code.utils;

/**
 * Created by Alex on 1/30/14.
 */
public class NullHelper {

    public static Integer notNull(Integer val, Integer defValue){
        return val == null ? defValue : val;
    }

    public static Long notNull(Long val, Long defValue){
        return val == null ? defValue : val;
    }
}
