package com.halo.update.update;

import android.content.Context;

import com.halo.update.util.SharedWrapper;


/**
 * Created by Administrator on 2016/4/7.
 */
public class UpdateConfig {
    private static final String TAG = "UpdateConfig";
    private static boolean isUpdateForce;
    private static boolean sDebug;
    private static final String DEFAULT_STRING = "";
    private static final String KEY_IGNORE_VERSION = "KEY_IGNORE_VERSION";
    private static final String KEY_IGNORE_MD5 = "KEY_IGNORE_MD5";
    private static String url;

    private UpdateConfig() {}

    /** 来打开日志输出，发布应用时请去掉 */
    public static void setDebug(boolean debug){
        sDebug = debug;
    }

    public static boolean isUpdateForce() {
        return isUpdateForce;
    }

    public static void setIsUpdateForce(boolean isUpdateForce) {
        UpdateConfig.isUpdateForce = isUpdateForce;
    }

    public static String getIgnoreVersion(Context context) {
        return SharedWrapper.with(context, TAG).getString(KEY_IGNORE_VERSION, DEFAULT_STRING);
    }

    public static void setIgnoreVersion(Context context, String content) {
        SharedWrapper.with(context, TAG).setString(KEY_IGNORE_VERSION, content);
    }

    public static String getIgnoreMd5(Context context) {
        return SharedWrapper.with(context, TAG).getString(KEY_IGNORE_MD5, DEFAULT_STRING);
    }

    public static void saveIgnoreMd5(Context context, String content) {
        SharedWrapper.with(context, TAG).setString(KEY_IGNORE_MD5, content);
    }

    public static void setUrl(String url) {
        UpdateConfig.url = url;
    }

    public static String getUrl() {
        return url;
    }

}
