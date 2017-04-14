package com.halo.update.download;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhouxin on 2016/6/6.
 * Description:
 */
public class DownloadThreadPool {
    private static final int MAX_IMUM_POOL_SIZE = 5;
    private ExecutorWithListener executor;               //线程池执行器
    private static final long KEEP_ALIVE_TIME = 60L;        //存活的时间
    private int corePoolSize = 3;   //核心线程池的数量，同时能执行的线程数量，默认3个

    /** 核心线程为1的无界队列线程池*/
    public ExecutorWithListener getExecutor() {
        if(executor == null) {
            synchronized (DownloadThreadPool.class) {
                if(executor == null) {
                    executor = new ExecutorWithListener(corePoolSize,
                            MAX_IMUM_POOL_SIZE,
                            KEEP_ALIVE_TIME,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>());
                }
            }
        }
        return executor;
    }

    /** 必须在首次执行前设置，否者无效 ,范围1-5之间 */
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize <= 0) {
            corePoolSize = 1;
        }
        if (corePoolSize > MAX_IMUM_POOL_SIZE) {
            corePoolSize = MAX_IMUM_POOL_SIZE;
        }
        this.corePoolSize = corePoolSize;
    }

}
