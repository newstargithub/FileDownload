package com.halo.update.callback;

/**
 * Created by zhouxin on 2016/3/24.
 * 获取数据回调
 */
public interface DataListener<T> {
    void onSuccess(T t);

    void onError(Result error);
}
