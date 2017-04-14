package com.halo.update;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.halo.update.download.DownloadManager;
import com.halo.update.update.notification.NotificationDownloadListener;
import com.halo.update.util.L;

public class DownloadingService extends Service {
    public static final String EXTRA_CODE = "extra_code";
    public static final String EXTRA_URL = "extra_url";
    public static int CODE_PAUSE = 1;
    public static int CODE_RESUME = 2;
    public static int CODE_CANCEL = 3;
    public static int CODE_RESTART = 4;
    private DownloadManager mDownloadManager;
    private NotificationDownloadListener mNotificationListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloadManager = DownloadManager.getInstance(this);
        mNotificationListener = new NotificationDownloadListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        L.d("onStartCommand startId =" + startId);
        if (intent != null) {
            try {
                int code = intent.getIntExtra(EXTRA_CODE, -1);
                String url = intent.getStringExtra(EXTRA_URL);
                if (code == CODE_CANCEL) {
                    IDownloadInterface.Stub.asInterface(mBinder).cancel(url);
                } else if (code == CODE_PAUSE) {
                    IDownloadInterface.Stub.asInterface(mBinder).pause(url);
                } else if (code == CODE_RESUME) {
                    IDownloadInterface.Stub.asInterface(mBinder).resume(url);
                } else if (code == CODE_RESTART) {
                    IDownloadInterface.Stub.asInterface(mBinder).restart(url);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    IDownloadInterface.Stub mBinder = new IDownloadInterface.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public synchronized void start(String url) throws RemoteException {
            L.d("start");
            mDownloadManager.startTask(url, mNotificationListener);
        }

        @Override
        public synchronized void pause(String url) throws RemoteException {
            L.d("pause");
            mDownloadManager.pauseTask(url);
        }

        @Override
        public synchronized void resume(String url) throws RemoteException {
            L.d("resume");
            mDownloadManager.startTask(url, mNotificationListener);
        }

        @Override
        public synchronized void cancel(String url) throws RemoteException {
            L.d("cancel");
            mDownloadManager.stopTask(url);
        }

        @Override
        public synchronized void restart(String url) throws RemoteException {
            L.d("restart");
            mDownloadManager.restartTask(url);
        }

        @Override
        public synchronized void remove(String url) throws RemoteException {
            L.d("remove");
            mDownloadManager.removeTask(url);
        }
    };
}
