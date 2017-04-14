package com.halo.update.update.notification;

import android.content.Context;

import com.halo.update.DownloadInfo;
import com.halo.update.download.IDownloadListener;
import com.halo.update.util.ApkUtils;
import com.halo.update.util.FileManager;
import com.halo.update.util.L;

import java.io.File;

/**
 * 通知下载状态回调
 */
public class NotificationDownloadListener implements IDownloadListener {

    private final Context mContext;
    private IUpdateNotification mUpdateNotification;

    public NotificationDownloadListener(Context context){
        mContext = context;
        mUpdateNotification = new NormalUpdateNotification();
//        mUpdateNotification = new CustomUpdateNotification();
    }

    @Override
    public void OnStart(DownloadInfo downloadInfo) {
        L.d("OnStart DownloadInfo:" + downloadInfo);
        mUpdateNotification.showNotification(mContext, downloadInfo);
    }

    @Override
    public void onProgress(DownloadInfo downloadInfo) {
        L.d("onProgress DownloadInfo:" + downloadInfo);
        mUpdateNotification.showNotification(mContext, downloadInfo);
    }

    @Override
    public void onStop(DownloadInfo downloadInfo) {
        L.d("onStop DownloadInfo:" + downloadInfo);
        mUpdateNotification.cancelNotification(mContext);
    }

    @Override
    public void onFinish(DownloadInfo downloadInfo) {
        L.d("onFinish DownloadInfo:" + downloadInfo);
        mUpdateNotification.cancelNotification(mContext);
        if(!FileManager.isExternalStorageWritable()) {
            //设置文件权限其他用户可读可运行
            FileManager.setPermissions(downloadInfo.getTargetPath(), 509, -1, -1);
        }
        ApkUtils.install(mContext, new File(downloadInfo.getTargetPath()));
    }

    @Override
    public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {
        L.d("downloadInfo:" + downloadInfo.getUrl() + " errorMsg:" + errorMsg);
        mUpdateNotification.cancelNotification(mContext);
        //TODO 显示出错误的信息
        mUpdateNotification.showErrorNotification(mContext, downloadInfo, errorMsg);
    }
}
