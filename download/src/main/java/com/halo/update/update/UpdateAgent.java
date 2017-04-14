package com.halo.update.update;

import android.content.Context;
import android.content.Intent;

import com.halo.update.download.DownloadManager;
import com.halo.update.util.ApkUtils;
import com.halo.update.util.FileManager;
import com.halo.update.util.codec.Digest;

import java.io.File;

public class UpdateAgent {
    private static final String TAG = UpdateAgent.class.getSimpleName();
    private static Context mContext;
    //检查更新状态回调
    private static UpdateListener sUpdateListener;
    //选择更新对话框按钮回调
    private static DialogButtonListener sButtonListener;

    /**
     * 检查更新，用于自动检查
     * 可被忽略此版本
     * @param context
     */
    public static void update(Context context){
        UpdateConfig.setIsUpdateForce(false);
        mContext = context.getApplicationContext();
        UpdateManager.checkUpdate(mContext);
    }

    /**
     * 强制检查更新，用于手动检查
     * 1.不采用配置的忽略此版本
     * 2.不显示忽略此版本选项
     */
    public static void updateForce(final Context context){
        UpdateConfig.setIsUpdateForce(true);
        mContext = context.getApplicationContext();
        UpdateManager.checkUpdate(mContext);
    }

    /** 更新提示对话框 */
    public static void showUpdateDialog(Context context, UpdateInfo updateInfo) {
        if(isIgnore(context, updateInfo)) {
            return;
        }
        File file = downloadedFile(context, updateInfo);
        boolean fileExist = file != null;
        startUpdateActivity(context, updateInfo, fileExist, file);
    }

    /**
     * 打开Dialog样式的更新提示Activity
     */
    private static void startUpdateActivity(Context context, UpdateInfo info, boolean fileExist, File file){
        Intent intent = new Intent(context, UpdateDialogActivity.class);
        intent.putExtra(UpdateDialogActivity.EXTRA_UPDATE_INFO, info);
        if(fileExist) {
            intent.putExtra("file", file.getAbsolutePath());
        } else {
            intent.putExtra("file", (String)null);
        }
        intent.putExtra("force", UpdateConfig.isUpdateForce());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 已下载文件，且MD5校验完成，返回文件
     * @param context
     * @param updateInfo
     * @return
     */
    private static File downloadedFile(Context context, UpdateInfo updateInfo) {
        String fileName = updateInfo.getNew_md5() + ".apk";
        File dir = FileManager.getUMDownloadDir(context, "/apk", new boolean[1]);
        File file = new File(dir, fileName);
        String fileMD5 = Digest.MD5.getMessage(file);
        return file.exists() && updateInfo.getNew_md5().equalsIgnoreCase(fileMD5) ? file : null;
    }

    /** 是否忽略此版本 */
    private static boolean isIgnore(Context context, UpdateInfo updateInfo) {
        return updateInfo.getNew_md5() != null && updateInfo.getNew_md5().equalsIgnoreCase(UpdateConfig.getIgnoreMd5(context)) && !UpdateConfig.isUpdateForce();
    }

    /**
     * 设置检查更新状态回调
     */
    public static void setUpdateListener(UpdateListener updateListener){
        sUpdateListener = updateListener;
    }

    public static UpdateListener getUpdateListener() {
        return sUpdateListener;
    }

    /**
     * 您可以设置对话框按键回调接口来监听用户的按键操作
     * 该回调函数是用来告诉您用户的选择，我们已经针对用户的选择进行了相应的默认操作，
     * 请不要在这里再执行下载APK，忽略更新等操作。
     * @param buttonListener
     */
    public static void setDialogListener(DialogButtonListener buttonListener) {
        sButtonListener = buttonListener;
    }

    /** 文件下载目录 */
    public static File getDownloadDir(Context context) {
        return FileManager.getUMDownloadDir(context, "/apk", new boolean[1]);
    }

    /**
     *  处理弹窗关闭
     * @param status
     * @param context
     * @param updateResponse
     * @param file
     */
    static void onDialogDestroyUpdateStatus(int status, Context context, UpdateInfo updateResponse, File file) {
        switch(status) {
            case UpdateStatus.Update:
                startUpdate(context, updateResponse, file);
                break;
            case UpdateStatus.Ignore:
                setIgnoreUpdate(context, updateResponse);
        }
        if(sButtonListener != null) {
            sButtonListener.onClick(updateResponse, status);
        }
    }

    /**
     * 文件存在直接安装，否则开始下载
     */
    private static void startUpdate(Context context, UpdateInfo updateInfo, File file) {
        //立即更新
        if(file == null) {
            startDownload(context, updateInfo);
        } else {
            startInstall(context, file);
        }
    }

    /**
     * 安装文件
     * @param context
     * @param file
     */
    private static void startInstall(Context context, File file) {
        ApkUtils.install(context, file);
    }

    /**
     * 开启下载服务
     * @param context
     * @param updateInfo
     */
    private static void startDownload(Context context, UpdateInfo updateInfo) {
        DownloadManager.getInstance(context).downloadFileFromUrl(updateInfo.getPath());
    }

    /**
     * 设置忽略此版本更新
     * @param context
     * @param updateResponse
     */
    private static void setIgnoreUpdate(Context context, UpdateInfo updateResponse) {
        UpdateConfig.saveIgnoreMd5(context, updateResponse.getNew_md5());
    }

    public static void setCheckUrl(String url) {
        UpdateConfig.setUrl(url);
    }

    /**
     * 设置成默认配置
     */
    public static void setDefault() {
        sUpdateListener = sDefaultUpdateListener;
        sButtonListener = null;
    }

    /**
     * 清除回调，避免泄漏
     */
    public static void clearConfig() {
        setDialogListener(null);
        setUpdateListener(null);
        mContext = null;
    }

    //检查更新状态回调
    private static UpdateListener sDefaultUpdateListener = new UpdateListener() {
        @Override
        public void onUpdateReturned(int updateStatus, UpdateInfo updateInfo) {
            switch (updateStatus) {
                case UpdateStatus.Yes: // has update
                    UpdateAgent.showUpdateDialog(UpdateAgent.mContext, updateInfo);
                    break;
                case UpdateStatus.No: // has no update
//                    Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
                    break;
//                case UpdateStatus.NoneWifi: // none wifi
//                    Toast.makeText(mContext, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
//                    break;
                case UpdateStatus.Timeout: // time out
//                    Toast.makeText(mContext, "超时", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
