package com.halo.update.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

/**
 * Created by zhouxin on 2016/5/25.
 * Description: app有关信息
 */
public class AppUtils {

    /**
     * get application version name
     *
     * @return String
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    /**
     * 应用版本号 version code
     */
    public static int getVersionCode(Context context){
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static CharSequence getApplicationLabel(Context context){
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info = context.getApplicationInfo();
        CharSequence appLabel = pm.getApplicationLabel(info);
        return appLabel;
    }

    public static Bitmap getApplicationIcon(Context context){
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info = context.getApplicationInfo();
        return ResUtil.drawableToBitmap(pm.getApplicationIcon(info));
    }

    public static int getApplicationIconId(Context context){
        ApplicationInfo info = context.getApplicationInfo();
        return info.icon;
    }
}
