package com.halo.update.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonWrapper {

    private GsonWrapper() {}

    public static final Gson gson = new GsonBuilder()
//            .registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
            .create();

}
