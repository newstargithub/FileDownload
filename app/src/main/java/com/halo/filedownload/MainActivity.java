package com.halo.filedownload;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.halo.update.update.DialogButtonListener;
import com.halo.update.update.UpdateAgent;
import com.halo.update.update.UpdateInfo;
import com.halo.update.update.UpdateStatus;

public class MainActivity extends AppCompatActivity {

    String CHECK_APP_VERSION = "https://nstapi.gdeng.cn/v1/app/checkAppVesion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });
        findViewById(R.id.bt_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        UpdateAgent.setDefault();
        UpdateAgent.setCheckUrl(CHECK_APP_VERSION);
        UpdateAgent.setDialogListener(new DialogButtonListener() {
            @Override
            public void onClick(UpdateInfo updateResponse, int status) {
                if(status == UpdateStatus.NotNow && updateResponse.isForce()) {
                    //退出应用或提示
                    Toast.makeText(MainActivity.this, R.string.must_update_app, Toast.LENGTH_SHORT).show();
                    Process.killProcess(Process.myPid());
                    System.exit(0);
                }
            }
        });
        UpdateAgent.update(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateAgent.clearConfig();
    }

    private void startDownload() {
        Intent intent = new Intent(this, DownloadApkActivity.class);
        startActivity(intent);
    }
}
