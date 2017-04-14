package com.halo.update.update.notification;

import android.app.Notification;
import android.content.Context;

import com.halo.update.DownloadInfo;

/**
 * Created by zhouxin on 2016/7/15.
 * Description: 通知下载状态
 */
public interface IUpdateNotification {
    Notification showNotification(Context context, DownloadInfo bean);

    Notification showErrorNotification(Context context, DownloadInfo bean, String errorMsg);

    void cancelNotification(Context context);
}
