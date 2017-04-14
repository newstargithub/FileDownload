package com.halo.update.update;

/**
 * statusCode 更新回调接口返回状态：
 * UpdateStatus.Yes 有更新;
 * UpdateStatus.No 没有更新;
 * UpdateStatus.NoneWifi 非wifi状态(在设置仅wifi下更新时才会有此状态);
 * UpdateStatus.Timeout 超时;
 * updateInfo 更新回调返回数据，包涵App更新信息，属性: updateLog 更新日志 ; version 最新版本 ;
 * path 最新版（apk文件/patch包）下载链接
 */
public interface UpdateListener {
    void onUpdateReturned(int updateStatus, UpdateInfo updateInfo);
}
