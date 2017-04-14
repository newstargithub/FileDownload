package com.halo.update.update;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 包涵App更新信息，
 * 属性:
 * updateLog 更新日志 ;
 * version 最新版本 ;
 * path 最新版（apk文件/patch包）下载链接
 * isForce  是否强制
 */
public class UpdateInfo implements Parcelable {
    /*apkAddress	apk下载地址
    needEnforce	是否强制执行(0.不强制执行;1.强制执行)
    num	版本号
    publishTime	发布时间
    remark	版本描述*/


    private boolean isForce;
    private boolean hasUpdate;
    private String updateLog;
    private String version;
    private String path;
    private String new_md5;  //MD5码
    private long size;     //安装包大小

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean force) {
        isForce = force;
    }

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNew_md5() {
//        return new_md5;
        return getVersion();
    }

    public void setNew_md5(String new_md5) {
        this.new_md5 = new_md5;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isForce ? (byte) 1 : (byte) 0);
        dest.writeByte(hasUpdate ? (byte) 1 : (byte) 0);
        dest.writeString(this.updateLog);
        dest.writeString(this.version);
        dest.writeString(this.path);
        dest.writeString(this.new_md5);
        dest.writeLong(this.size);
    }

    public UpdateInfo() {
    }

    private UpdateInfo(Parcel in) {
        this.isForce = in.readByte() != 0;
        this.hasUpdate = in.readByte() != 0;
        this.updateLog = in.readString();
        this.version = in.readString();
        this.path = in.readString();
        this.new_md5 = in.readString();
        this.size = in.readLong();
    }

    public static final Creator<UpdateInfo> CREATOR = new Creator<UpdateInfo>() {
        public UpdateInfo createFromParcel(Parcel source) {
            return new UpdateInfo(source);
        }

        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };
}
