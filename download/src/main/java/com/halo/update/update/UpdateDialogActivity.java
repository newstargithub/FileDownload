package com.halo.update.update;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.halo.update.R;

import java.io.File;

public class UpdateDialogActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_UPDATE_INFO = "EXTRA_UPDATE_INFO";
    private static final String TAG = UpdateDialogActivity.class.getSimpleName();

    TextView tv_update_title;
    TextView tv_update_content;
    private CheckBox cb_ignore;
    Button bt_update_cancel;
    Button bt_update_ok;
    private UpdateInfo mUpdateInfo;
    int status = UpdateStatus.NotNow;
    boolean isIgnore = false;
    File file;

    public static Intent newIntent(Context context, UpdateInfo info, String filePath, boolean force){
        Intent intent = new Intent(context, UpdateDialogActivity.class);
        intent.putExtra(EXTRA_UPDATE_INFO, info);
        intent.putExtra("file", filePath);
        intent.putExtra("force", force);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_dialog);
        mUpdateInfo = getIntent().getParcelableExtra(EXTRA_UPDATE_INFO);
        String filePath = getIntent().getExtras().getString("file");
        boolean force = getIntent().getExtras().getBoolean("force");
        boolean fileExist = filePath != null;
        if(fileExist) {
            file = new File(filePath);
        }
        tv_update_title = (TextView) findViewById(R.id.tv_update_title);
        tv_update_content = (TextView) findViewById(R.id.tv_update_content);
        cb_ignore = (CheckBox)findViewById(R.id.cb_ignore);
        bt_update_cancel = (Button) findViewById(R.id.bt_update_cancel);
        bt_update_ok = (Button) findViewById(R.id.bt_update_ok);
        bt_update_cancel.setOnClickListener(this);
        bt_update_ok.setOnClickListener(this);
        if(force) {
            cb_ignore.setVisibility(View.GONE);
        }
        cb_ignore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isIgnore = isChecked;
            }
        });
        initData();
    }

    private void initData() {
        tv_update_title.setText(R.string.new_version);
//        String fileSize = Formatter.formatFileSize(this, mUpdateInfo.getSize());
        CharSequence concat = TextUtils.concat(mUpdateInfo.getUpdateLog());
        tv_update_content.setText(concat);
        if(mUpdateInfo.isForce()){
            //强制更新隐藏忽略和暂不更新
            cb_ignore.setVisibility(View.GONE);
            bt_update_cancel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_update_cancel) {
            Log.d(TAG, "暂不更新");
            status = isIgnore ? UpdateStatus.Ignore : UpdateStatus.NotNow;
            finish();
        } else if (v.getId() == R.id.bt_update_ok) {
            Log.d(TAG, "立即更新");
            status = UpdateStatus.Update;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateAgent.onDialogDestroyUpdateStatus(status, this, mUpdateInfo, file);
    }
}
