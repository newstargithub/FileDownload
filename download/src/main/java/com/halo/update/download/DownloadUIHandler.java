package com.halo.update.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.halo.update.DownloadInfo;
import com.halo.update.util.L;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2016/1/19
 * 描    述：用于在主线程回调下载UI
 * 修订历史：
 * ================================================
 */
public class DownloadUIHandler extends Handler {

    private IDownloadListener mGlobalDownloadListener;

    public DownloadUIHandler(){
        super(Looper.getMainLooper());
    }

    @Override
    public void handleMessage(Message msg) {
        MessageBean messageBean = (MessageBean) msg.obj;
        if (messageBean != null) {
            DownloadInfo info = messageBean.downloadInfoInfo;
            String errorMsg = messageBean.errorMsg;
            Exception e = messageBean.e;
            if (mGlobalDownloadListener != null) {
                executeListener(mGlobalDownloadListener, info, errorMsg, e);
            }
            IDownloadListener listener = info.getListener();
            if (listener != null) {
                L.e("info.getListener is null." + info);
                executeListener(listener, info, errorMsg, e);
            }
        } else {
            L.e("DownloadUIHandler DownloadInfo null");
        }
    }

    private void executeListener(IDownloadListener listener, DownloadInfo info, String errorMsg, Exception e) {
        int state = info.getState();
        switch (state) {
            case IDownloadManager.WAITING:
                listener.OnStart(info);
                break;
            case IDownloadManager.STOP:
                listener.onStop(info);
                break;
            case IDownloadManager.NONE:
            case IDownloadManager.DOWNLOADING:
            case IDownloadManager.PAUSE:
                listener.onProgress(info);
                break;
            case IDownloadManager.FINISH:
                listener.onProgress(info);   //结束前再次回调进度，避免最后一点数据没有刷新
                listener.onFinish(info);
                break;
            case IDownloadManager.ERROR:
                listener.onProgress(info);   //结束前再次回调进度，避免最后一点数据没有刷新
                listener.onError(info, errorMsg, e);
                break;
        }
    }

    public void setGlobalDownloadListener(IDownloadListener downloadListener) {
        this.mGlobalDownloadListener = downloadListener;
    }

    public static class MessageBean {
        public DownloadInfo downloadInfoInfo;
        public String errorMsg;
        public Exception e;
    }
}
