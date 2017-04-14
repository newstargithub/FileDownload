package com.halo.update.download;

import android.support.annotation.NonNull;

import com.halo.update.DownloadInfo;

/**
 * Created by zhouxin on 2016/6/6.
 * Description: 下载操作的接口
 */
public interface IDownloadManager {
    //定义下载状态常量
    int NONE = 0;         //无状态  --> 等待
    int WAITING = 1;      //等待    --> 下载，暂停
    int DOWNLOADING = 2;  //下载中  --> 暂停，停止，完成，错误
    int PAUSE = 3;        //暂停    --> 停止，下载
    int STOP = 4;         //停止    --> 等待
    int FINISH = 5;       //完成    --> 重新下载
    int ERROR = 6;        //错误    --> 等待
    
    DownloadInfo startTask(@NonNull String url, IDownloadListener listener);

    void pauseTask(@NonNull String url);

    void stopTask(@NonNull String url);

    void restartTask(@NonNull String url);

    void removeTask(@NonNull String url);

    /** 删除所有任务 */
    void removeAllTask();

    void pauseAllTask();

    void stopAllTask();

    /** 开始所有任务的方法 */
    void startAllTask();
}
