package com.halo.update.download;


import com.halo.update.DownloadInfo;

/**
 * Author: zx
 * Date: 2017/4/13
 * Description: 下载监听
 */

public abstract class DownloadListener implements IDownloadListener {
    private Object userTag;

    @Override
    public void OnStart(DownloadInfo downloadInfo) {

    }

    @Override
    public void onStop(DownloadInfo downloadInfo) {

    }

    /** 类似View的Tag功能，主要用在listView更新数据的时候，防止数据错乱 */
    public Object getUserTag() {
        return userTag;
    }

    /** 类似View的Tag功能，主要用在listView更新数据的时候，防止数据错乱 */
    public void setUserTag(Object userTag) {
        this.userTag = userTag;
    }

}
