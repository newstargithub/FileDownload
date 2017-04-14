package com.halo.update.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件管理
 */
public class FileManager {
    private static final String TAG = FileManager.class.getSimpleName();

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * 外部存储可用，就返回外部存储的指定目录
     * @param dir
     * @return
     */
    public static File getExternalDir(String dir){
        if(isExternalStorageWritable()) {
            if(TextUtils.isEmpty(dir)) {
                return Environment.getExternalStorageDirectory();
            } else {
                File file = new File(Environment.getExternalStorageDirectory(), dir);
                if(file.exists() || file.mkdirs()) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Saving files that can be shared with other apps;
     * creates a directory for a new photo album in the public pictures directory:
     * @param albumName
     * @return
     */
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    public static File getExternalPublicDir(String type, String dir){
        if(isExternalStorageWritable()) {
            File file;
            if(TextUtils.isEmpty(dir)) {
                file = Environment.getExternalStoragePublicDirectory(type);
            } else {
                file = new File(Environment.getExternalStorageDirectory(), dir);
            }
            if(file.exists() || file.mkdirs()) {
                return file;
            }
        }
        return null;
    }

    public static void testAndroidDir(Context context){
        File file = Environment.getDataDirectory(); //  /data
        file = Environment.getDownloadCacheDirectory(); //  /cache
        file = Environment.getExternalStorageDirectory();   //  /storage/sdcard
        file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);    //  /storage/sdcard/Android/data/com.example.dev/files/DownloadInfo
        file = context.getExternalCacheDir();   //  /storage/sdcard/Android/data/com.example.dev/cache
        file = context.getFilesDir();   //  /data/data/com.example.dev/files
        file = context.getCacheDir();   //  /data/data/com.example.dev/cache
    }


    /**
     * 获取命名路径的硬盘缓存目录
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalStorageAvailable = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        String cachePath;
        if (externalStorageAvailable) {
            /*  /sdcard/Android/data/<application package>/cache    */
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            /*  /data/data/<application package>/cache  */
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * url对应的缓存key
     * 图片URL中可能包含一些特殊字符，这些字符有可能在命名文件时是不合法的。
     * 其实最简单的做法就是将图片的URL进行MD5编码，编码后的字符串肯定是唯一的，
     * 并且只会包含0-F这样的字符，完全符合文件的命名规则
     * @param url
     * @return
     */
    public static String hashKeyFromUrl(String url) {
        String cacheKey;
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            cacheKey = bytesToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    /**
     * 字节数组转16进制字符串
     * @param bytes
     * @return
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if(hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static void close(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 可用空间
     * @param file
     * @return
     */
    public static long getUsableSpace(File file) {
        StatFs sf = new StatFs(file.getAbsolutePath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return sf.getAvailableBytes();
        } else {
            return sf.getAvailableBlocks() * sf.getBlockSize();
        }
    }

    public static File createTempFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                context.getCacheDir()      /* directory */
        );
        return image;
    }

    /**
     *
     * @param context
     * @param dirName
     * @param isExternal
     * @return  外存可写，/download/um/目录名；否则，应用缓存目录，cache/umdownload
     */
    public static File getUMDownloadDir(Context context, String dirName, boolean[] isExternal) {
        File dir;
        String dirPath;
        if(isExternalStorageWritable()) {
            dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            dirPath = dirPath + "/download/um" + dirName;
            dir = new File(dirPath);
            dir.mkdirs();
            if(dir.exists()) {
                isExternal[0] = true;
                return dir;
            }
        }
        //下载到CacheDir有权限问题
        dirPath = context.getCacheDir().getAbsolutePath();
        (new File(dirPath)).mkdir();
        setPermissions(dirPath, 505, -1, -1);
        dirPath = dirPath + "/umdownload";
        (new File(dirPath)).mkdir();
        setPermissions(dirPath, 505, -1, -1);
        dir = new File(dirPath);
        isExternal[0] = false;
        return dir;
    }

    public static boolean setPermissions(String dirPath, int permission, int var2, int var3) {
        try {
            Class clazz = Class.forName("android.os.FileUtils");
            Method method = clazz.getMethod("setPermissions", new Class[]{String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE});
            method.invoke((Object)null, new Object[]{dirPath, Integer.valueOf(permission), Integer.valueOf(-1), Integer.valueOf(-1)});
            return true;
        } catch (ClassNotFoundException var6) {
            log("error when set permissions:", var6);
        } catch (NoSuchMethodException var7) {
            log("error when set permissions:", var7);
        } catch (IllegalArgumentException var8) {
            log("error when set permissions:", var8);
        } catch (IllegalAccessException var9) {
            log("error when set permissions:", var9);
        } catch (InvocationTargetException var10) {
            log("error when set permissions:", var10);
        }
        return false;
    }

    private static void log(String str, Exception e){
        Log.e(TAG, str + ":  [" + e + "]");
    }

    public static File getDownloadDir(Context context, String dirName){
        String state = Environment.getExternalStorageState();
        File directory;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            directory = context.getFilesDir();
        }
        File downloadDir = new File(directory.getAbsolutePath() + File.separator + dirName);
        if(!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        return downloadDir;
    }
}

