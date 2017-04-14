package com.halo.update.update.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.halo.update.DownloadInfo;
import com.halo.update.download.IDownloadManager;
import com.halo.update.util.AppUtils;

/**
 * Created by zhouxin on 2016/7/15.
 * Description: 正常样式的通知栏
 */
public class NormalUpdateNotification extends BaseUpdateNotification {
    private static final int NOTIFY_ID = 1;
    private Notification mNotification;

    @Override
    public Notification showNotification(Context context, DownloadInfo bean) {
        mNotification = create(context, bean);
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFY_ID, mNotification);
        return mNotification;
    }

    private Notification create(Context context, DownloadInfo bean) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (bean.getState() == IDownloadManager.DOWNLOADING) {
            builder.setContentTitle("正在下载：" + AppUtils.getApplicationLabel(context));
        } else {
            builder.setContentTitle("暂停：" + AppUtils.getApplicationLabel(context));
        }
        int progress = Math.round(bean.getProgress() * 10000 * 1.0f / 100);
        builder.setContentText(progress + "%");
        builder.setProgress(100, progress, false);
        builder.setSmallIcon(AppUtils.getApplicationIconId(context))
                .setTicker("正在下载应用")
                .setAutoCancel(true)
//                .setWhen(System.currentTimeMillis())
                //优先级，高显示在上面
//                .setPriority(NotificationCompat.PRIORITY_MAX)
                //用户清除通知时触发，可以是点击清除按钮，也可以是左右滑动删除(当然了，前提是高版本)
                .setDeleteIntent(getCancelPendingIntent(context, bean));
        return builder.build();
    }

    @Override
    public Notification showErrorNotification(Context context, DownloadInfo bean, String errorMsg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(AppUtils.getApplicationIconId(context))
                .setTicker("下载出错")
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("下载出错:"+ errorMsg)
                .setContentText("点击重新下载")
                .setContentIntent(getRestartPendingIntent(context, bean));
        Notification notification = builder.build();
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFY_ID, notification);
        return notification;
    }

    @Override
    public void cancelNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFY_ID);
        mNotification = null;
    }
}
