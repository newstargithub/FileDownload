package com.halo.update.update;

/**
 * status 用户点击对话框按钮的情况：
 * UpdateStatus.Update用户选择现在更新;
 * UpdateStatus.Ignore用户选择忽略该版;
 * UpdateStatus.NotNow用户选择以后再说，点击回退键，关闭对话框。 监听下载进度
 * 已经针对用户的选择进行了相应的默认操作，请不要在这里再执行下载APK，忽略更新等操作。
 */
public interface DialogButtonListener {
    void onClick(UpdateInfo updateResponse, int status);
}
