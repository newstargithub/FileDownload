package com.halo.update.download;

import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.halo.update.DownloadInfo;
import com.halo.update.db.DownloadDBManager;
import com.halo.update.util.FileUtil;
import com.halo.update.util.L;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by zhouxin on 2016/6/6.
 * Description: 下载任务
 */
public class DownloadTask implements Runnable {
    private static final int TIMEOUT = 60 * 1000;
    private static final int BUFFER_SIZE = 4 * 1024;
    private final DownloadDBManager mDownloadDB;
    private final DownloadInfo mDownloadInfo;
    private final DownloadUIHandler mDownloadUIHandler;
    private Future<?> mFuture;
    private boolean isRestartTask;
    private volatile boolean isPause = false;
    private long mPreviousTime;                      //上次更新的时间，用于计算下载速度

    public DownloadTask(DownloadInfo bean, DownloadDBManager dbManager, DownloadUIHandler handler, ExecutorService executor) {
        this(bean, dbManager, handler, executor, false);
    }

    public DownloadTask(DownloadInfo downloadInfoInfo, DownloadDBManager dbManager, DownloadUIHandler uiHandler, ExecutorService executor, boolean isRestartTask) {
        this.mDownloadInfo = downloadInfoInfo;
        this.mDownloadDB = dbManager;
        this.mDownloadUIHandler = uiHandler;
        this.isRestartTask = isRestartTask;

        onPreExecute();
        mFuture = executor.submit(this);
    }

    private void onPreExecute() {
        mDownloadInfo.setNetworkSpeed(0);
        mDownloadInfo.setState(IDownloadManager.WAITING);
        postMessage(null, null);
    }

    private void cancel() {
        if (mFuture != null) {
            mFuture.cancel(true);
        }
    }

    private boolean isCancelled() {
        boolean isCancelled = false;
        if (mFuture != null) {
            isCancelled = mFuture.isCancelled();
        }
        return isCancelled;
    }

    public void pause() {
        isPause = true;
        cancel();
    }

    public void stop() {
        isPause = false;
        cancel();
    }

    @Override
    public void run() {
        download();
    }

    private DownloadInfo download() {
        //如果是重新下载，需要删除临时文件
        if (isRestartTask) {
            isRestartTask = false;
            if(!FileUtil.deleteFile(mDownloadInfo.getTargetPath())) {
                return mDownloadInfo;
            }
            mDownloadInfo.setProgress(0);
            mDownloadInfo.setDownloadLength(0);
            mDownloadInfo.setTotalLength(0);
        }

        mPreviousTime = System.currentTimeMillis();
        mDownloadInfo.setState(IDownloadManager.DOWNLOADING);
        postMessage(null, null);

        //构建下载文件路径，如果有设置，就用设置的，否则就自己创建
        String url = mDownloadInfo.getUrl();
        String fileName = mDownloadInfo.getFileName();
        if (TextUtils.isEmpty(fileName)) {
            fileName = FileUtil.getUrlFileName(url);
            mDownloadInfo.setFileName(fileName);
        }
        if (TextUtils.isEmpty(mDownloadInfo.getTargetPath())) {
            File file = new File(mDownloadInfo.getTargetFolder(), fileName);
            mDownloadInfo.setTargetPath(file.getAbsolutePath());
        }

        //检查手机上文件的有效性
        File file = new File(mDownloadInfo.getTargetPath());
        long startPos;
        long length = file.length();
        L.d("file.length=" + length + " DownloadLength=" + mDownloadInfo.getDownloadLength());
        if (file.length() != mDownloadInfo.getDownloadLength()) {
            postError("断点文件异常，需要删除后重新下载", null);    //已下载的文件与数据库记录的长度不同，可删除重下
            return mDownloadInfo;
        } else {
            //断点下载的情况
            startPos = mDownloadInfo.getDownloadLength();
        }
        //再次检查文件有效性，文件大小大于总文件大小
        if (startPos > mDownloadInfo.getTotalLength()) {
            postError("断点文件异常，需要删除后重新下载", null);
            return mDownloadInfo;
        }
        //下载完成
        if (startPos == mDownloadInfo.getTotalLength() && startPos > 0) {
            mDownloadInfo.setProgress(1.0f);
            mDownloadInfo.setNetworkSpeed(0);
            mDownloadInfo.setState(IDownloadManager.FINISH);
            postMessage(null, null);
            return mDownloadInfo;
        }
        //设置断点写文件
        ProgressRandomAccessFile out;
        try {
            out = new ProgressRandomAccessFile(file, "rw", startPos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            postError("没有找到已存在的断点文件", e);
            return mDownloadInfo;
        }
        L.e("startPos:" + startPos + "  path:" + mDownloadInfo.getTargetPath());

        //构建请求体,默认使用get请求下载,设置断点头
        InputStream input;
        long contentLength;
        HttpURLConnection urlConnection;
        try {
            urlConnection = getHttpURLConnection(mDownloadInfo.getUrl());
            //http断点续传
            urlConnection.setRequestProperty("Range", "bytes=" + startPos + "-");
            contentLength = urlConnection.getContentLength();
            input = urlConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            postError("网络异常", e);
            return mDownloadInfo;
        }
        //获取流对象，准备进行读写文件
        if (mDownloadInfo.getTotalLength() == 0) {
            mDownloadInfo.setTotalLength(contentLength);
        }
        //读写文件流
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        try {
            copyStream(in, out);
        } catch (InterruptedIOException ie) {
            ie.printStackTrace();
            //ignore
            //中断导致的io异常是停止或暂停操作
        } catch (IOException e) {
            e.printStackTrace();
            postError("文件读写异常", e);
            return mDownloadInfo;
        } finally {
            FileUtil.close(out);
            FileUtil.close(in);
            FileUtil.close(input);
            urlConnection.disconnect();
        }

        //循环结束走到这里，a.下载完成     b.停止      c.判断是否下载出错
        if (isCancelled()) {
            if (isPause) {
                mDownloadInfo.setState(IDownloadManager.PAUSE); //暂停
                L.e("state: 暂停 " + mDownloadInfo);
            } else {
                mDownloadInfo.setState(IDownloadManager.STOP); //停止
                L.e("state: 停止 " + mDownloadInfo);
            }
            mDownloadInfo.setNetworkSpeed(0);
            postMessage(null, null);
        } else if (file.length() == mDownloadInfo.getTotalLength() && mDownloadInfo.getState() == IDownloadManager.DOWNLOADING) {
            mDownloadInfo.setNetworkSpeed(0);
            mDownloadInfo.setState(IDownloadManager.FINISH); //下载完成
            postMessage(null, null);
        } else if (file.length() != mDownloadInfo.getDownloadLength()) {
            //由于不明原因，文件保存有误
            postError("未知原因", null);
        }
        return mDownloadInfo;
    }

    private void postError(String errorMsg, Exception e) {
        mDownloadInfo.setNetworkSpeed(0);
        mDownloadInfo.setState(IDownloadManager.ERROR);
        postMessage(errorMsg, e);
    }

    private void copyStream(InputStream in, RandomAccessFile out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        out.seek(out.length());
        while ((len = in.read(buffer, 0, BUFFER_SIZE)) != -1 && !Thread.interrupted()) {
            out.write(buffer, 0, len);
        }
    }

    @NonNull
    private HttpURLConnection getHttpURLConnection(String url) throws IOException {
        HttpURLConnection urlConnection;
        urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
//            urlConnection.setConnectTimeout(TIMEOUT); //默认值为0，使我们做一个阻塞连接。这并不意味着我们将永远不会超时，但这可能意味着你会几分钟后得到一个TCP超时。
        return urlConnection;
    }

    private void postMessage(String errorMsg, Exception e) {
        mDownloadDB.update(mDownloadInfo); //发消息前首先更新数据库
        DownloadUIHandler.MessageBean messageBean = new DownloadUIHandler.MessageBean();
        messageBean.downloadInfoInfo = mDownloadInfo;
        messageBean.errorMsg = errorMsg;
        messageBean.e = e;
        Message msg = mDownloadUIHandler.obtainMessage();
        msg.obj = messageBean;
        mDownloadUIHandler.sendMessage(msg);
    }

    /**
     * 文件读写
     */
    private final class ProgressRandomAccessFile extends RandomAccessFile {
        private long lastDownloadLength = 0; //总共已下载的大小
        private long curDownloadLength = 0;  //当前已下载的大小（可能分几次下载）
        private long lastRefreshUiTime;

        public ProgressRandomAccessFile(File file, String mode, long lastDownloadLength) throws FileNotFoundException {
            super(file, mode);
            this.lastDownloadLength = lastDownloadLength;
            this.lastRefreshUiTime = System.currentTimeMillis();
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
            super.write(buffer, offset, count);

            //已下载大小
            long downloadLength = lastDownloadLength + count;
            curDownloadLength += count;
            lastDownloadLength = downloadLength;
            mDownloadInfo.setDownloadLength(downloadLength);

            //计算下载速度
            long totalTime = (System.currentTimeMillis() - mPreviousTime) / 1000;
            if (totalTime == 0) {
                totalTime += 1;
            }
            long networkSpeed = curDownloadLength / totalTime;
            mDownloadInfo.setNetworkSpeed(networkSpeed);

            //下载进度
            float progress = downloadLength * 1.0f / mDownloadInfo.getTotalLength();
            mDownloadInfo.setProgress(progress);
            long curTime = System.currentTimeMillis();
            //每200毫秒刷新一次数据
            if (curTime - lastRefreshUiTime >= 200) {
                postMessage(null, null);
//                L.e(mDownloadInfo.getDownloadLength() + " " + mDownloadInfo.getTotalLength() + " " + mDownloadInfo.getProgress());
                lastRefreshUiTime = System.currentTimeMillis();
            }
        }
    }


}
