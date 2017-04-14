package com.halo.update.update;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.halo.update.callback.DataListener;
import com.halo.update.callback.Result;
import com.halo.update.util.AppUtils;
import com.halo.update.util.GsonUtil;
import com.halo.update.util.codec.DES3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Created by zhouxin on 2016/3/24.
 * 1.下载文件位置
 * 2.存在文件，a全部下载完直接安装 b没下载完全断点续传
 * 3.不存在下载文件
 * 4.下载完成安装
 */
public class UpdateManager {

    private static final String TAG = UpdateManager.class.getSimpleName();
    /**
     * HTTP连接超时时间
     */
    private static final int TIMEOUT = 5 * 1000;
    private static final int BUFFER = 4 * 1024;
    /**
     * 参数编码
     */
    private static final String PARAMS_ENCODING = "UTF-8";

    private static int count = 0;
    private static String THREAD_NAME = "UpdateManager_";
    /**
     * 执行网络请求线程池
     * 缓存线程池，有新任务就立即执行，用空闲线程或新建线程
     */
    public static Executor mExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            /*Process.setThreadPriority()并传递THREAD_PRIORITY_BACKGROUND来设置线程的优先级为”background”。
            如果你不通过这个方式来给线程设置一个低的优先级，那么这个线程仍然会使得你的应用显得卡顿，
            因为这个线程默认与UI线程有着同样的优先级。*/
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setName(THREAD_NAME + count++);
            return thread;
        }
    });

    /**
     * 在主线程分发网络请求的数据结果
     */
    public static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void checkUpdate(Context context) {
        final String url = UpdateConfig.getUrl();
        Map<String, String> param = new HashMap<>();
//        param.put("versionName", AppUtils.getVersionName(context));
        param.put("num", AppUtils.getVersionName(context));
        param.put("type", "2");   //类型(1 农速通-货主 2 农速通-司机 3 农速通-物流公司)
        param.put("platform", "2");   //平台：1 IOS，2 Android，3 H5
        Log.d(TAG, url + "?" + GsonUtil.fromJson(param));
        String encryptStr = null;
        try {
            encryptStr = DES3.encode(GsonUtil.fromJson(param));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> map = new HashMap<>();
        map.put("param", encryptStr);
        Type typeOfT = new TypeToken<Result<VersionInfo>>(){}.getType();
        performGetRequest(url, map, new DataListener<VersionInfo>() {
            @Override
            public void onSuccess(VersionInfo info) {
                UpdateInfo bean = new UpdateInfo();
                int updateStatus = UpdateStatus.No;
                if(info != null) {
                    updateStatus = UpdateStatus.Yes;
                    bean.setHasUpdate(true);
                    bean.setPath(info.getApkAddress());
                    bean.setForce("1".equals(info.getNeedEnforce()));
                    bean.setUpdateLog(info.getRemark());
                    bean.setVersion(info.getNum());
                }
                if(UpdateAgent.getUpdateListener() != null) {
                    UpdateAgent.getUpdateListener().onUpdateReturned(updateStatus, bean);
                }
            }

            @Override
            public void onError(Result error) {
                if(UpdateAgent.getUpdateListener() != null) {
                    UpdateAgent.getUpdateListener().onUpdateReturned(UpdateStatus.Timeout, null);
                }
            }
        }, typeOfT);
    }

    public static void checkUpdateTest(Context context, final UpdateListener updateListener) {
        UpdateInfo info = new UpdateInfo();
        info.setHasUpdate(true);
        info.setPath("http://www.gdeng.cn/nsy/nsy_v1.3.0.apk");
        info.setUpdateLog("快更新吧");
        info.setVersion("1.2.8");
        info.setSize(10 * 1024 * 1024);
        int updateStatus = UpdateStatus.Yes;
        updateListener.onUpdateReturned(updateStatus, info);
    }

    private static <T> void performGetRequest(final String urlStr, final Map<String, String> params,
                                       final DataListener<T> dataListener,
                                       final Type typeOfT) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = httpGet(urlStr, params);
                    json = DES3.decode(json);
                    Log.d(TAG, urlStr + "\n" + json);
                    final Result<T> result = GsonUtil.fromJson(json, typeOfT);
                    postResponse(result, dataListener);
                } catch (Exception e) {
                    e.printStackTrace();
                    postError(new Result<T>("请求服务器数据失败", -2), dataListener);
                }
            }
        });
    }

    private static <T> void postResponse(final Result<T> result, final DataListener<T> dataListener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (result == null) {
                    dataListener.onError(new Result("数据格式错误", -1));
                } else {
                    if (result.getStatusCode() == Result.CODE_SUCCESS
                            || result.getStatusCode() == Result.CODE_NO_VERSION) {
                        dataListener.onSuccess(result.getData());
                    } else {
                        dataListener.onError(result);
                    }
                }
            }
        });
    }

    private static <T> void postError(final Result<T> result, final DataListener<T> dataListener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dataListener.onError(result);
            }
        });
    }

    public static String httpGet(String urlStr, Map<String, String> params) throws IOException {
        String encodingParams = encodingParams(params);
        if (!TextUtils.isEmpty(encodingParams)) {
            StringBuilder sb = new StringBuilder();
            sb.append(urlStr);
            sb.append("?");
            sb.append(encodingParams);
            urlStr = sb.toString();
        }
        return sendGet(urlStr);
    }

    private static String encodingParams(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            StringBuilder encodedParams = new StringBuilder();
            try {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    encodedParams.append(URLEncoder.encode(entry.getKey(), PARAMS_ENCODING));
                    encodedParams.append('=');
                    encodedParams.append(URLEncoder.encode(entry.getValue(), PARAMS_ENCODING));
                    encodedParams.append('&');
                }
                return encodedParams.toString();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Encoding not supported: " + PARAMS_ENCODING, e);
            }
        }
        return null;
    }

    public static String sendGet(String urlStr) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(TIMEOUT);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] bytes = readStream(in);
            return new String(bytes, Charset.forName(PARAMS_ENCODING));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public void httpPost(String urlStr, Map<String, String> params) throws IOException {
        sendPost(urlStr, encodingParams(params));
    }

    public String sendPost(String urlStr, String data) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(TIMEOUT);
            if (!TextUtils.isEmpty(data)) {
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                writeStream(out, data);
            }
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] bytes = readStream(in);
            return new String(bytes, Charset.forName(PARAMS_ENCODING));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static void writeStream(OutputStream out, String data) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data.getBytes()));
        byte[] buffer = new byte[BUFFER];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
    }

    private static byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStream out = new BufferedOutputStream(outputStream);
        byte[] buffer = new byte[BUFFER];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
        out.close();
        return outputStream.toByteArray();
    }
}
