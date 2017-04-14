package com.halo.update.download;

import com.halo.update.DownloadInfo;

/**
 * Created by zhouxin on 2016/5/25.
 * Description: 下载回调接口
 */
public interface IDownloadListener {
    void OnStart(DownloadInfo downloadInfo);

    void onProgress(DownloadInfo downloadInfo);

    void onStop(DownloadInfo downloadInfo);

    void onFinish(DownloadInfo downloadInfo);

    void onError(DownloadInfo downloadInfo, String errorMsg, Exception e);

}
