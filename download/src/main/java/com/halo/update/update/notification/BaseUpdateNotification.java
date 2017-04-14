package com.halo.update.update.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.halo.update.DownloadInfo;
import com.halo.update.DownloadingService;

/**
 * Created by zhouxin on 2016/7/15.
 * Description:
 */
public abstract class BaseUpdateNotification implements IUpdateNotification {

    private static final int REQUEST_CODE_PAUSE_OR_RESUME = 0;
    private static final int REQUEST_CODE_CANCEL = 1;

    protected static PendingIntent getRestartPendingIntent(Context context, DownloadInfo bean) {
        Intent pauseIntent = new Intent(context, DownloadingService.class);
        pauseIntent.putExtra(DownloadingService.EXTRA_CODE, DownloadingService.CODE_RESTART);
        pauseIntent.putExtra(DownloadingService.EXTRA_URL, bean.getUrl());
        return PendingIntent.getService(context, REQUEST_CODE_PAUSE_OR_RESUME,
                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected static PendingIntent getResumePendingIntent(Context context, DownloadInfo bean) {
        Intent resumeIntent = new Intent(context, DownloadingService.class);
        resumeIntent.putExtra(DownloadingService.EXTRA_CODE, DownloadingService.CODE_RESUME);
        resumeIntent.putExtra(DownloadingService.EXTRA_URL, bean.getUrl());
        return PendingIntent.getService(context, REQUEST_CODE_PAUSE_OR_RESUME,
                resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected static PendingIntent getCancelPendingIntent(Context context, DownloadInfo bean) {
        Intent cancelIntent = new Intent(context, DownloadingService.class);
        cancelIntent.putExtra(DownloadingService.EXTRA_CODE, DownloadingService.CODE_CANCEL);
        cancelIntent.putExtra(DownloadingService.EXTRA_URL, bean.getUrl());
        return PendingIntent.getService(context, REQUEST_CODE_CANCEL,
                cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected static PendingIntent getPausePendingIntent(Context context, DownloadInfo bean) {
        Intent pauseIntent = new Intent(context, DownloadingService.class);
        pauseIntent.putExtra(DownloadingService.EXTRA_CODE, DownloadingService.CODE_PAUSE);
        pauseIntent.putExtra(DownloadingService.EXTRA_URL, bean.getUrl());
        return PendingIntent.getService(context, REQUEST_CODE_PAUSE_OR_RESUME,
                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
