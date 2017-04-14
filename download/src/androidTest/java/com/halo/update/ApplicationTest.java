package com.halo.update;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.halo.update.db.DownloadDBManager;
import com.halo.update.util.L;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testDownloadDB(){
        DownloadDBManager db = new DownloadDBManager(getContext());
        String url = "http://www.gdeng.cn/nsy/nsy_v1.2.8.apk";
        DownloadInfo bean = new DownloadInfo();
        bean.setState(6);
        bean.setUrl(url);
        bean.setTargetFolder("/storage/sdcard/DownloadInfo/apk");
        bean.setTargetPath("/storage/sdcard/DownloadInfo/apk/nsy_v1.2.8.apk");
        bean.setFileName("nsy_v1.2.8.apk");
        bean.setProgress(0.1f);
        bean.setTotalLength(100);
        bean.setDownloadLength(10);
        db.insert(bean);
        DownloadInfo downloadInfo = db.get(url);
        L.d(downloadInfo.toString());
        System.out.print(downloadInfo);
        downloadInfo.setDownloadLength(20);
        db.update(downloadInfo);
        DownloadInfo downloadInfo1 = db.get(url);
        L.d(downloadInfo1.toString());
        System.out.print(downloadInfo1);
    }

    public void testClearDB(){
        DownloadDBManager db = new DownloadDBManager(getContext());
        db.clear();
    }

    public void testGetDB(){
        DownloadDBManager db = new DownloadDBManager(getContext());
        String url = "http://www.gdeng.cn/nsy/nsy_v1.2.8.apk";
        DownloadInfo downloadInfo = db.get(url);
        System.out.print(downloadInfo);
    }
}