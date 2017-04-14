package com.halo.update.db;

import android.content.Context;

import com.halo.update.DownloadInfo;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zhouxin on 2016/5/25.
 * Description: 数据库管理者
 */
public class DownloadDBManager {
    private Lock mLock;
    private DownloadDao mDao;

    public DownloadDBManager(Context context) {
        mLock = new ReentrantLock();
        mDao = new DownloadDao(context);
    }

    /**
     * 获取
     * @param url
     * @return
     */
    public DownloadInfo get(String url) {
        mLock.lock();
        try {
            return mDao.get(url);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 获取所有
     */
    public List<DownloadInfo> getAll() {
        mLock.lock();
        try {
            return mDao.getAll();
        } finally {
            mLock.unlock();
        }
    }

    public DownloadInfo replace(DownloadInfo entity) {
        mLock.lock();
        try {
            long id = mDao.replace(entity);
            entity.setId((int) id);
            return entity;
        } finally {
            mLock.unlock();
        }
    }

    public DownloadInfo insert(DownloadInfo entity) {
        mLock.lock();
        try {
            long id = mDao.insert(entity);
            entity.setId((int) id);
            return entity;
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 移除
     * @param url
     * @return 是否移除成功
     */
    public boolean delete(String url) {
        if (url == null) return true;
        mLock.lock();
        try {
            return mDao.remove(url);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 清空
     *
     * @return 是否清空成功
     */
    public boolean clear() {
        mLock.lock();
        try {
            return mDao.deleteAll() > 0;
        } finally {
            mLock.unlock();
        }
    }

    public void update(DownloadInfo bean) {
        mLock.lock();
        try {
            mDao.update(bean);
        } finally {
            mLock.unlock();
        }
    }
}
