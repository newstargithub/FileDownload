package com.halo.filedownload;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.halo.filedownload.bean.ApkInfo;
import com.halo.filedownload.util.AppCacheUtils;
import com.halo.update.DownloadInfo;
import com.halo.update.DownloadService;
import com.halo.update.download.DownloadListener;
import com.halo.update.download.DownloadManager;
import com.halo.update.download.IDownloadManager;
import com.halo.update.util.ApkUtils;
import com.halo.update.util.L;

import java.io.File;
import java.util.ArrayList;

public class DownloadApkActivity extends AppCompatActivity {

    private ArrayList<ApkInfo> apks;
    private DownloadApkAdapter adapter;
    private DownloadManager downloadManager;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_download_apk);
        initData();
        downloadManager = DownloadService.getDownloadManager(this);
        findViewById(R.id.openManager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, DownloadManagerActivity.class));
            }
        });
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApkInfo apkInfo = adapter.getItem(position);
                openDetail(apkInfo);
            }
        });
        adapter = new DownloadApkAdapter();
        listView.setAdapter(adapter);
    }

    /**
     * 当前Activity显示的回调
     */
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private void openDetail(ApkInfo apkInfo) {
        Intent intent = new Intent(this, DesActivity.class);
        intent.putExtra("apk", apkInfo);
        startActivity(intent);
    }

    private class DownloadApkAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return apks.size();
        }

        @Override
        public ApkInfo getItem(int position) {
            return apks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ApkInfo apk = getItem(position);
            DownloadInfo downloadInfo = downloadManager.getTaskByUrl(apk.getUrl());
            L.e("getView " + apk.getName() + " downloadInfo=" + downloadInfo);
            final ViewHolder holder;
            if(convertView == null) {
                convertView = View.inflate(mContext, R.layout.item_download_app, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(apk.getName());
            Glide.with(mContext).load(apk.getIconUrl()).error(R.mipmap.ic_launcher).into(holder.icon);
            holder.refresh(downloadInfo, apk);
            if(downloadInfo != null) {
                DownloadListener downloadListener = new MyDownloadListener();
                downloadListener.setUserTag(holder);
                L.e("downloadInfo.setListener(downloadListener);" + downloadInfo);
                downloadInfo.setListener(downloadListener);
            }
            holder.button.setOnClickListener(holder);
            return convertView;
        }
    }

    private class ViewHolder implements View.OnClickListener {

        private final ImageView icon;
        private final TextView name;
        private final TextView netSpeed;
        private final TextView tvProgress;
        private final ProgressBar pbProgress;
        private final Button button;
        private DownloadInfo downloadInfo;
        private ApkInfo apk;

        public ViewHolder(View convertView) {
            icon = (ImageView) convertView.findViewById(R.id.icon);
            name = (TextView) convertView.findViewById(R.id.name);
            netSpeed = (TextView) convertView.findViewById(R.id.netSpeed);
            tvProgress = (TextView) convertView.findViewById(R.id.tvProgress);
            pbProgress = (ProgressBar) convertView.findViewById(R.id.pbProgress);
            button = (Button) convertView.findViewById(R.id.start);
        }

        public void refresh(@Nullable DownloadInfo downloadInfo, ApkInfo apk) {
            this.downloadInfo = downloadInfo;
            this.apk = apk;
            refresh();
        }

        private void refresh() {
            button.setEnabled(true);
            netSpeed.setText("--KB/s");
            if(this.downloadInfo == null) {
                pbProgress.setVisibility(View.INVISIBLE);
                tvProgress.setText("0.0%");

                button.setText("下载");
            } else {
                pbProgress.setVisibility(View.VISIBLE);
                pbProgress.setMax((int) this.downloadInfo.getTotalLength());
                pbProgress.setProgress((int) this.downloadInfo.getDownloadLength());
                tvProgress.setText((Math.round(downloadInfo.getProgress() * 10000) * 1.0f / 100) + "%");

                if(this.downloadInfo.getState() == IDownloadManager.NONE) {
                    button.setText("下载");
                } else if(this.downloadInfo.getState() == IDownloadManager.WAITING) {
                    button.setText("等待中");
                    button.setEnabled(false);
                } else if(this.downloadInfo.getState() == IDownloadManager.DOWNLOADING) {
                    String networkSpeed = Formatter.formatFileSize(mContext, downloadInfo.getNetworkSpeed());
                    netSpeed.setText(networkSpeed + "/s");
                    button.setText("暂停");
                } else if(this.downloadInfo.getState() == IDownloadManager.FINISH) {
                    if (ApkUtils.isAvailable(mContext, new File(this.downloadInfo.getTargetPath()))) {
                        button.setText("卸载");
                    } else {
                        button.setText("安装");
                    }
                } else if(this.downloadInfo.getState() == IDownloadManager.PAUSE
                        || this.downloadInfo.getState() == IDownloadManager.STOP) {
                    button.setText("继续");
                } else {
                    button.setText("重装");
                }
            }
        }

        @Override
        public void onClick(View v) {
            if(downloadInfo == null) {
                AppCacheUtils.getInstance(mContext).put(apk.getUrl(), apk);
                downloadManager.startTask(apk.getUrl(), null);
                adapter.notifyDataSetChanged();
            } else {
                switch (downloadInfo.getState()) {
                    case DownloadManager.NONE:
                    case DownloadManager.PAUSE:
                    case DownloadManager.STOP:
                    case DownloadManager.ERROR:
                        downloadManager.startTask(downloadInfo.getUrl(), downloadInfo.getListener());
                        break;
                    case DownloadManager.DOWNLOADING:
                        downloadManager.pauseTask(downloadInfo.getUrl());
                        break;
                    case DownloadManager.FINISH:
                        if (ApkUtils.isAvailable(mContext, new File(downloadInfo.getTargetPath()))) {
                            ApkUtils.uninstall(mContext, ApkUtils.getPackageName(mContext, downloadInfo.getTargetPath()));
                        } else {
                            ApkUtils.install(mContext, new File(downloadInfo.getTargetPath()));
                        }
                        break;
                }
                refresh();
            }
        }
    }

    private class MyDownloadListener extends DownloadListener {

        @Override
        public void onProgress(DownloadInfo downloadInfo) {
            if (getUserTag() == null) return;
            ViewHolder holder = (ViewHolder) getUserTag();
            holder.refresh();  //这里不能使用传递进来的 DownloadInfo，否者会出现条目错乱的问题
        }

        @Override
        public void onFinish(DownloadInfo downloadInfo) {
            Toast.makeText(mContext, "下载完成:" + downloadInfo.getTargetPath(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {
            if (errorMsg != null)
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void initData() {
        apks = new ArrayList<>();
        ApkInfo apkInfo1 = new ApkInfo();
        apkInfo1.setName("美丽加");
        apkInfo1.setIconUrl("http://pic3.apk8.com/small2/14325422596306671.png");
        apkInfo1.setUrl("http://download.apk8.com/d2/soft/meilijia.apk");
        apks.add(apkInfo1);
        ApkInfo apkInfo2 = new ApkInfo();
        apkInfo2.setName("果然方便");
        apkInfo2.setIconUrl("http://pic3.apk8.com/small2/14313175771828369.png");
        apkInfo2.setUrl("http://download.apk8.com/d2/soft/guoranfangbian.apk");
        apks.add(apkInfo2);
        ApkInfo apkInfo3 = new ApkInfo();
        apkInfo3.setName("薄荷");
        apkInfo3.setIconUrl("http://pic3.apk8.com/small2/14308183888151824.png");
        apkInfo3.setUrl("http://download.apk8.com/d2/soft/bohe.apk");
        apks.add(apkInfo3);
        ApkInfo apkInfo4 = new ApkInfo();
        apkInfo4.setName("GG助手");
        apkInfo4.setIconUrl("http://pic3.apk8.com/small2/14302008166714263.png");
        apkInfo4.setUrl("http://download.apk8.com/d2/soft/GGzhushou.apk");
        apks.add(apkInfo4);
        ApkInfo apkInfo5 = new ApkInfo();
        apkInfo5.setName("红包惠锁屏");
        apkInfo5.setIconUrl("http://pic3.apk8.com/small2/14307106593913848.png");
        apkInfo5.setUrl("http://download.apk8.com/d2/soft/hongbaohuisuoping.apk");
        apks.add(apkInfo5);
        ApkInfo apkInfo6 = new ApkInfo();
        apkInfo6.setName("快的打车");
        apkInfo6.setIconUrl("http://up.apk8.com/small1/1439955061264.png");
        apkInfo6.setUrl("http://download.apk8.com/soft/2015/%E5%BF%AB%E7%9A%84%E6%89%93%E8%BD%A6.apk");
        apks.add(apkInfo6);
        ApkInfo apkInfo7 = new ApkInfo();
        apkInfo7.setName("叮当快药");
        apkInfo7.setIconUrl("http://pic3.apk8.com/small2/14315954626414886.png");
        apkInfo7.setUrl("http://d2.apk8.com:8020/soft/dingdangkuaiyao.apk");
        apks.add(apkInfo7);
        ApkInfo apkInfo8 = new ApkInfo();
        apkInfo8.setName("悦跑圈");
        apkInfo8.setIconUrl("http://pic3.apk8.com/small2/14298490191525146.jpg");
        apkInfo8.setUrl("http://d2.apk8.com:8020/soft/yuepaoquan.apk");
        apks.add(apkInfo8);
        ApkInfo apkInfo9 = new ApkInfo();
        apkInfo9.setName("悠悠导航");
        apkInfo9.setIconUrl("http://pic3.apk8.com/small2/14152456988840667.png");
        apkInfo9.setUrl("http://d2.apk8.com:8020/soft/%E6%82%A0%E6%82%A0%E5%AF%BC%E8%88%AA2.3.32.1.apk");
        apks.add(apkInfo9);
        ApkInfo apkInfo10 = new ApkInfo();
        apkInfo10.setName("虎牙直播");
        apkInfo10.setIconUrl("http://up.apk8.com/small1/1439892235841.jpg");
        apkInfo10.setUrl("http://download.apk8.com/down4/soft/hyzb.apk");
        apks.add(apkInfo10);
    }
}
