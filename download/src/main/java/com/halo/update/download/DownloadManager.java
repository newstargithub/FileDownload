package com.halo.update.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.halo.update.DownloadInfo;
import com.halo.update.DownloadingService;
import com.halo.update.IDownloadInterface;
import com.halo.update.db.DownloadDBManager;
import com.halo.update.update.UpdateAgent;
import com.halo.update.util.FileUtil;
import com.halo.update.util.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zhouxin on 2016/6/6.
 * Description: 下载管理器
 * should 可配置下载目录，并行下载数
 */
public class DownloadManager implements IDownloadManager{

    private static DownloadManager mInstance;
    private final Context mContext;
    private final String mTargetFolder;     //下载目录
    private final DownloadUIHandler mDownloadUIHandler;
    private final List<DownloadInfo> mDownloadInfoList;
    private final DownloadDBManager mDownloadDao;
    private DownloadThreadPool mThreadPool;          //下载的线程池

    private IDownloadInterface mService;
    private ConcurrentLinkedQueue<String> mQueue;   //启动下载的url队列
    private boolean mIsBound;   //绑定了服务
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IDownloadInterface.Stub.asInterface(service);
            Iterator iterator = mQueue.iterator();
            if(iterator.hasNext()){
                String url = (String) iterator.next();
                try {
                    mService.start(url);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                iterator.remove();
            }
//            unbindService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };


    public static DownloadManager getInstance(Context context) {
        if (null == mInstance) {
            synchronized (DownloadManager.class) {
                if (null == mInstance) {
                    mInstance = new DownloadManager(context);
                }
            }
        }
        return mInstance;
    }

    private DownloadManager(Context context) {
        mContext = context.getApplicationContext();
        mDownloadDao = new DownloadDBManager(context);    //构建下载Download的操作类
        mDownloadInfoList = Collections.synchronizedList(new ArrayList<DownloadInfo>());
        mDownloadInfoList.addAll(mDownloadDao.getAll());    //获取所有任务
        if (!mDownloadInfoList.isEmpty()) {
            synchronized (mDownloadInfoList) {
                for (DownloadInfo info : mDownloadInfoList) {
                    //校验数据的有效性，防止下载过程中退出，第二次进入的时候，由于状态没有更新导致的状态错误
                    if (info.getState() == WAITING || info.getState() == DOWNLOADING || info.getState() == PAUSE) {
                        info.setState(NONE);
                        info.setNetworkSpeed(0);
                        mDownloadDao.replace(info);
                    }
                }
            }
        }
        mTargetFolder = UpdateAgent.getDownloadDir(context).getAbsolutePath();
        mQueue = new ConcurrentLinkedQueue<>();
        mThreadPool = new DownloadThreadPool();
        /*mThreadPool.getExecutor().addOnAllTaskEndListener(new ExecutorWithListener.OnAllTaskEndListener() {
            @Override
            public void onAllTaskEnd() {
                onTaskEnd();
            }
        });*/
        mDownloadUIHandler = new DownloadUIHandler();
        mDownloadUIHandler.setGlobalDownloadListener(new IDownloadListener() {
            @Override
            public void OnStart(DownloadInfo downloadInfo) {

            }

            @Override
            public void onProgress(DownloadInfo downloadInfo) {

            }

            @Override
            public void onStop(DownloadInfo downloadInfo) {
                onTaskEnd();
            }

            @Override
            public void onFinish(DownloadInfo downloadInfo) {
                onTaskEnd();
            }

            @Override
            public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {
                onTaskEnd();
            }
        });
    }

    private void onTaskEnd() {
        boolean isAllStop = true;
        synchronized (mDownloadInfoList) {
            for(DownloadInfo bean : mDownloadInfoList) {
                if(bean.getState() != STOP
                        && bean.getState() != FINISH
                        && bean.getState() != ERROR) {
                    isAllStop = false;
                    break;
                }
            }
        }
        if(isAllStop) {
            stopService();
        }
    }

    public List<DownloadInfo> getAllTask() {
        return mDownloadInfoList;
    }

    public DownloadThreadPool getThreadPool() {
        return mThreadPool;
    }

    @Override
    public DownloadInfo startTask(@NonNull String url, IDownloadListener listener) {
        DownloadInfo downloadInfo = getTaskByUrl(url);
        if (downloadInfo == null) {
            downloadInfo = new DownloadInfo();
            downloadInfo.setUrl(url);
            downloadInfo.setState(NONE);
            downloadInfo.setTargetFolder(mTargetFolder);
            mDownloadDao.insert(downloadInfo);
            mDownloadInfoList.add(downloadInfo);
        }
        //无状态，暂停，错误才允许开始下载
        if (downloadInfo.getState() == NONE
                || downloadInfo.getState() == PAUSE
                || downloadInfo.getState() == ERROR
                || downloadInfo.getState() == STOP
                || downloadInfo.getState() == FINISH) {
            downloadInfo.setListener(listener);
            //构造即开始执行
            DownloadTask task = new DownloadTask(downloadInfo, mDownloadDao, mDownloadUIHandler, mThreadPool.getExecutor());
            downloadInfo.setDownloadTask(task);
        } else {
            L.e("DownloadManager", "任务正在下载或等待中 url:" + url);
        }
        return downloadInfo;
    }

    @Override
    public void restartTask(@NonNull String url) {
        DownloadInfo downloadInfoInfo = getTaskByUrl(url);
        if (downloadInfoInfo == null) return;
        if (downloadInfoInfo.getState() != DOWNLOADING && downloadInfoInfo.getState() != WAITING) {
            //构造即开始执行
            DownloadTask task = new DownloadTask(downloadInfoInfo, mDownloadDao, mDownloadUIHandler, mThreadPool.getExecutor(), true);
            downloadInfoInfo.setDownloadTask(task);
        }
    }

    @Override
    public void pauseTask(@NonNull String url) {
        DownloadInfo downloadInfoInfo = getTaskByUrl(url);
        if (downloadInfoInfo == null) return;
        int state = downloadInfoInfo.getState();
        //等待和下载中才允许暂停
        if ((state == DOWNLOADING || state == WAITING) && downloadInfoInfo.getDownloadTask() != null) {
            downloadInfoInfo.getDownloadTask().pause();
        }
    }

    @Override
    public void stopTask(@NonNull String url) {
        DownloadInfo downloadInfoInfo = getTaskByUrl(url);
        if (downloadInfoInfo == null) return;
        //无状态和完成状态，不允许停止
        if ((downloadInfoInfo.getState() != STOP && downloadInfoInfo.getState() != FINISH && downloadInfoInfo.getState() != ERROR)) {
            if(downloadInfoInfo.getState() == WAITING || downloadInfoInfo.getState() == DOWNLOADING) {
                downloadInfoInfo.getDownloadTask().stop();
            } else {
                downloadInfoInfo.setState(IDownloadManager.STOP);
                postMessage(downloadInfoInfo, null, null);
            }
        }
    }

    private void postMessage(DownloadInfo downloadInfoInfo, String errorMsg, Exception e) {
        mDownloadDao.update(downloadInfoInfo); //发消息前首先更新数据库
        DownloadUIHandler.MessageBean messageBean = new DownloadUIHandler.MessageBean();
        messageBean.downloadInfoInfo = downloadInfoInfo;
        messageBean.errorMsg = errorMsg;
        messageBean.e = e;
        Message msg = mDownloadUIHandler.obtainMessage();
        msg.obj = messageBean;
        mDownloadUIHandler.sendMessage(msg);
    }

    @Override
    public void removeTask(@NonNull String url) {
        final DownloadInfo downloadInfoInfo = getTaskByUrl(url);
        if (downloadInfoInfo == null) return;
        pauseTask(url);        //暂停任务
//        stopTask(url);        //停止任务  暂停还是停止任务？
        removeTaskByUrl(url);  //移除任务
        FileUtil.deleteFile(downloadInfoInfo.getTargetPath()); //删除文件
        mDownloadDao.delete(url); //清除数据库
    }

    @Override
    public void removeAllTask() {
        //集合深度拷贝，避免迭代移除报错
        List<String> urls = new ArrayList<>();
        synchronized (mDownloadInfoList) {
            for (DownloadInfo info : mDownloadInfoList) {
                urls.add(info.getUrl());
            }
        }
        for (String url : urls) {
            removeTask(url);
        }
    }

    /** 暂停全部任务,先暂停没有下载的，再暂停下载中的 */
    @Override
    public void pauseAllTask() {
        synchronized (mDownloadInfoList) {
            for (DownloadInfo info : mDownloadInfoList) {
                if (info.getState() != DOWNLOADING) pauseTask(info.getUrl());
            }
            for (DownloadInfo info : mDownloadInfoList) {
                if (info.getState() == DOWNLOADING) pauseTask(info.getUrl());
            }
        }
    }

    /** 停止全部任务,先停止没有下载的，再停止下载中的 */
    @Override
    public void stopAllTask() {
        synchronized (mDownloadInfoList) {
            for (DownloadInfo info : mDownloadInfoList) {
                if (info.getState() != DOWNLOADING) stopTask(info.getUrl());
            }
            for (DownloadInfo info : mDownloadInfoList) {
                if (info.getState() == DOWNLOADING) stopTask(info.getUrl());
            }
        }
    }

    @Override
    public void startAllTask() {
        synchronized (mDownloadInfoList) {
            for (DownloadInfo downloadInfo : mDownloadInfoList) {
                startTask(downloadInfo.getUrl(), downloadInfo.getListener());
            }
        }
    }

    /** url任务 */
    public DownloadInfo getTaskByUrl(String url) {
        synchronized (mDownloadInfoList) {
            for (DownloadInfo downloadInfoInfo : mDownloadInfoList) {
                if (url.equals(downloadInfoInfo.getUrl())) {
                    return downloadInfoInfo;
                }
            }
        }
        return null;
    }

    public void removeTaskByUrl(@NonNull String url) {
        //迭代集合元素
        synchronized (mDownloadInfoList) {
            ListIterator<DownloadInfo> iterator = mDownloadInfoList.listIterator();
            while (iterator.hasNext()) {
                DownloadInfo info = iterator.next();
                if (url.equals(info.getUrl())) {
                    IDownloadListener listener = info.getListener();
                    info.removeListener();     //清除回调监听
                    iterator.remove();         //清除任务
                    break;
                }
            }
        }
    }

    public void downloadFileFromUrl(String url){
        if(mService == null) {
            mQueue.offer(url);
            startService();
        } else {
            try {
                mService.start(url);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /** 开启服务，先绑定后启动 */
    public void startService() {
        mContext.startService(new Intent(mContext, DownloadingService.class));
        L.e("startService.");
        bindService();
    }

    /** 绑定服务 */
    private void bindService() {
        mIsBound = true;
        Intent intent = new Intent(mContext, DownloadingService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        L.e("bindService.");
    }

    /** 停止服务 */
    public void stopService() {
        unbindService();
        Intent intent = new Intent(mContext, DownloadingService.class);
        mContext.stopService(intent);
        L.e("stopService.");
    }

    /** 解绑服务 */
    private void unbindService() {
        if(mIsBound) {
            mIsBound = false;
            mContext.unbindService(mConnection);
            L.e("unbindService.");
        }
    }

}
