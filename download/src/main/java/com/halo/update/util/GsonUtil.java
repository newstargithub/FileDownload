package com.halo.update.util;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * Created by zhouxin on 2016/5/25.
 * Description:
 */
public class GsonUtil {
    public static <T> T fromJson(String json, Type typeOfT){
        return GsonWrapper.gson.fromJson(json, typeOfT);
    }

    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        return GsonWrapper.gson.fromJson(json, classOfT);
    }

    public static String fromJson(Object src){
        return GsonWrapper.gson.toJson(src);
    }
}
