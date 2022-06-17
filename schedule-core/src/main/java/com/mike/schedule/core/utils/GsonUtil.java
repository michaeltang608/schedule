package com.mike.schedule.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/13.
 */
public class GsonUtil {
    private static Gson gson = null;
    static {
        gson= new GsonBuilder().create();
    }

    public static String toJson(Object src) {
        return gson.toJson(src);
    }
}
