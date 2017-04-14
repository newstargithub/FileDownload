package com.halo.update.update.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.halo.update.DownloadInfo;
import com.halo.update.R;
import com.halo.update.download.IDownloadManager;
import com.halo.update.util.AppUtils;

/**
 *  定制通知视图
 * 1.更新Notification时，通知id相同，要使用原来的对象做更改后更新
 * 2.有多个延迟意图时，使用不同的REQUEST_CODE，
 * 意图已存在，替换更新extra data，设置标识PendingIntent.FLAG_UPDATE_CURRENT
 */
public class CustomUpdateNotification extends BaseUpdateNotification {

    private static final String TAG = CustomUpdateNotification.class.getSimpleName();
    private static int NOTIFY_ID = 1;

    /**
     * 显示下载通知
     * @param context
     * @param bean
     * @return
     */
    public Notification showNotification(Context context, DownloadInfo bean) {
        Log.d(TAG, bean.toString());
        RemoteViews views = getRemoteViews(context, bean);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContent(views)
                .setSmallIcon(AppUtils.getApplicationIconId(context))
                .setTicker("正在下载应用")
                .setAutoCancel(true)
                //用户清除通知时触发，可以是点击清除按钮，也可以是左右滑动删除(当然了，前提是高版本)
                .setDeleteIntent(getCancelPendingIntent(context, bean))
                //优先级，高显示在上面
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis());

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT <= 10) {
            notification.contentView = views;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            notification.bigContentView = views;
        }

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFY_ID, notification);
        return notification;
    }

    @NonNull
    private static RemoteViews getRemoteViews(Context context, DownloadInfo bean) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_download);
        if (bean.getState() == IDownloadManager.DOWNLOADING) {
            //暂停
            PendingIntent pausePendingIntent = getPausePendingIntent(context, bean);
            views.setOnClickPendingIntent(R.id.common_notification_continue, pausePendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                views.setTextViewCompoundDrawables(R.id.common_notification_continue, R.drawable.ic_pause, 0, 0, 0);
            }
            //下载标题
            views.setTextViewText(R.id.common_title, "正在下载：" + AppUtils.getApplicationLabel(context));
            views.setCharSequence(R.id.common_notification_continue, "setText", "暂停");
        } else {
            //继续
            PendingIntent resumePendingIntent = getResumePendingIntent(context, bean);
            views.setOnClickPendingIntent(R.id.common_notification_continue, resumePendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                views.setTextViewCompoundDrawables(R.id.common_notification_continue, R.drawable.ic_resume, 0, 0, 0);
            }
            //下载标题
            views.setTextViewText(R.id.common_title, "暂停：" + AppUtils.getApplicationLabel(context));
            views.setTextViewText(R.id.common_notification_continue, "继续");
        }
        //取消
        PendingIntent cancelPendingIntent = getCancelPendingIntent(context, bean);
        views.setOnClickPendingIntent(R.id.common_notification_cancel, cancelPendingIntent);
        views.setTextViewText(R.id.common_notification_cancel, "取消");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            views.setTextViewCompoundDrawables(R.id.common_notification_cancel, R.drawable.ic_stop, 0, 0, 0);
        }
        //下载应用图标
        views.setImageViewBitmap(R.id.common_icon, AppUtils.getApplicationIcon(context));
        //进度文本
        int progress = Math.round(bean.getProgress() * 10000 * 1.0f / 100);
        views.setTextViewText(R.id.common_progress_text, progress + "%");
        //进度条
        views.setProgressBar(R.id.common_progress_bar, 100, progress, false);
        return views;
    }

    /**
     * 更新下载通知
     * notify方法的id是常量，不管PendingIntent是否匹配，后面的通知都会直接替换掉前面的通知
     * @param context
     * @param notification
     * @param bean
     */
    public static void updateNotification(Context context, Notification notification, DownloadInfo bean) {
        Log.d(TAG, bean.toString());
        RemoteViews views = getRemoteViews(context, bean);
        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContent(views)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        Notification notification = builder.build();*/
        if (Build.VERSION.SDK_INT <= 10) {
            notification.contentView = views;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            notification.bigContentView = views;
        }
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFY_ID, notification);
    }


    public void cancelNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFY_ID);
    }

    public Notification showErrorNotification(Context context, DownloadInfo bean, String errorMsg) {
        String packageName = context.getPackageName();
        RemoteViews views = new RemoteViews(packageName, R.layout.notification_download);
        //下载标题
        views.setTextViewText(R.id.common_title, "下载出错：" + errorMsg);
        //重试
        PendingIntent pausePendingIntent = getResumePendingIntent(context, bean);
        views.setOnClickPendingIntent(R.id.common_notification_continue, pausePendingIntent);
        views.setCharSequence(R.id.common_notification_continue, "setText", "重试");
        //重新下载
        PendingIntent cancelPendingIntent = getRestartPendingIntent(context, bean);
        views.setOnClickPendingIntent(R.id.common_notification_cancel, cancelPendingIntent);
        views.setTextViewText(R.id.common_notification_cancel, "重新下载");
        //下载应用图标
        views.setImageViewBitmap(R.id.common_icon, AppUtils.getApplicationIcon(context));
        //进度文本
        int progress = Math.round(bean.getProgress() * 10000 * 1.0f / 100);
        views.setTextViewText(R.id.common_progress_text, progress + "%");
        //进度条
        views.setProgressBar(R.id.common_progress_bar, 100, progress, false);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContent(views)
                .setSmallIcon(AppUtils.getApplicationIconId(context))
//                .setTicker("正在下载应用")
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT <= 10) {
            notification.contentView = views;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            notification.bigContentView = views;
        }

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFY_ID, notification);
        return notification;
    }
}
