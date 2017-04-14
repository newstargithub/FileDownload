package com.halo.update.util;

import android.util.Log;

/**
 * Created by zhouxin on 2016/5/25.
 * Description:
 */
public class L {

    private static final String TAG = L.class.getSimpleName();

    public static void e(String s) {
        Log.e(TAG, s);
    }

    public static void e(String tag, String s) {
        Log.e(tag, s);
    }

    public static void d(String s) {
        Log.d(TAG, s);
    }
}
