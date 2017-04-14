package com.halo.update;

import android.os.Parcel;
import android.os.Parcelable;

import com.halo.update.download.IDownloadListener;
import com.halo.update.download.DownloadTask;

/**
 * Created by zhouxin on 2016/5/24.
 * Description: 下载对象
 */
public class DownloadInfo implements Parcelable {
    private int id;               //id自增长
    private String url;           //文件URL
    private String targetFolder;  //保存文件夹
    private String targetPath;    //保存文件地址
    private String fileName;      //保存的文件名
    private float progress;       //下载进度(0~1)
    private long totalLength;     //总大小
    private long downloadLength;  //已下载大小
    private long networkSpeed;    //下载速度
    private int state;            //当前状态

    private IDownloadListener listener;  //进度监听
    private DownloadTask downloadTask;  //执行下载的任务

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public long getDownloadLength() {
        return downloadLength;
    }

    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }

    public long getNetworkSpeed() {
        return networkSpeed;
    }

    public void setNetworkSpeed(long networkSpeed) {
        this.networkSpeed = networkSpeed;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public IDownloadListener getListener() {
        return listener;
    }

    public void setListener(IDownloadListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    public DownloadInfo() {
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "state=" + state +
                ", id=" + id +
                ", url='" + url + '\'' +
                ", targetFolder='" + targetFolder + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", progress=" + progress +
                ", totalLength=" + totalLength +
                ", downloadLength=" + downloadLength +
                '}';
    }


    public void setDownloadTask(DownloadTask downloadTask) {
        this.downloadTask = downloadTask;
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.url);
        dest.writeString(this.targetFolder);
        dest.writeString(this.targetPath);
        dest.writeString(this.fileName);
        dest.writeFloat(this.progress);
        dest.writeLong(this.totalLength);
        dest.writeLong(this.downloadLength);
        dest.writeLong(this.networkSpeed);
        dest.writeInt(this.state);
    }

    private DownloadInfo(Parcel in) {
        this.id = in.readInt();
        this.url = in.readString();
        this.targetFolder = in.readString();
        this.targetPath = in.readString();
        this.fileName = in.readString();
        this.progress = in.readFloat();
        this.totalLength = in.readLong();
        this.downloadLength = in.readLong();
        this.networkSpeed = in.readLong();
        this.state = in.readInt();
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        public DownloadInfo createFromParcel(Parcel source) {
            return new DownloadInfo(source);
        }

        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };
}
