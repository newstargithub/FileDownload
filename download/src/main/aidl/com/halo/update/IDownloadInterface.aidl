// IDownloadInterface.aidl
package com.halo.update;
import com.halo.update.DownloadInfo;

// Declare any non-default types here with import statements

interface IDownloadInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void start(in String url);

    void pause(in String url);

    void resume(in String url);

    void cancel(in String url);

    void restart(in String url);

    void remove(in String url);
}
