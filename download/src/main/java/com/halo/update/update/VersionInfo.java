package com.halo.update.update;

/**
 * Created by zhouxin on 2016/9/5.
 * Description:
 */
public class VersionInfo {
    /*apkAddress	apk下载地址
    needEnforce	是否强制执行(0.不强制执行;1.强制执行)
    num	版本号
    publishTime	发布时间
    remark	版本描述*/
    private String apkAddress;
    private String needEnforce;
    private String num;
    private String publishTime;
    private String remark;

    public String getApkAddress() {
        return apkAddress;
    }

    public void setApkAddress(String apkAddress) {
        this.apkAddress = apkAddress;
    }

    public String getNeedEnforce() {
        return needEnforce;
    }

    public void setNeedEnforce(String needEnforce) {
        this.needEnforce = needEnforce;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
