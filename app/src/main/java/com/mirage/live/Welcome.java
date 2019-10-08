package com.mirage.live;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.mirage.live.base.BaseActivity;
import com.mirage.live.upgrade.Download;
import com.mirage.live.upgrade.DownloadService;
import com.mirage.live.upgrade.UpgradeProgressDialog;
import com.mirage.live.utils.FileProvider8;


import java.io.File;

import butterknife.BindView;


public class Welcome extends BaseActivity{

    @BindView(R.id.test)
    Button test;

    private static final String TAG = "Welcome";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    LocalBroadcastManager bManager;
    private UpgradeProgressDialog upgradeProgressDialog;

    File apkfile = new File(Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DOWNLOADS), DownloadService.FILE_NAME);

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(DownloadService.MESSAGE_PROGRESS)) {
                Download download = intent.getParcelableExtra("download");
                Log.v(TAG, "download......" + download.getProgress());
                if (download.getProgress() == 100) {
                    if (upgradeProgressDialog.isShowing()) {
                        upgradeProgressDialog.setTitle("下载完成");
                        upgradeProgressDialog.dismiss();

                        installApk();
                    }
                } else {
                    if (upgradeProgressDialog.isShowing()) {
                        upgradeProgressDialog.setMax((int) download.getTotalFileSize());
                        upgradeProgressDialog.setProgress(download.getProgress());
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        upgradeProgressDialog = new UpgradeProgressDialog(this);
        verifyStoragePermissions(this);

        registerReceiver();

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();

            }
        });
    }


    private void startDownload(){
        Intent intent = new Intent(Welcome.this, DownloadService.class);
        startService(intent);
        //upgradeProgressDialog.setMessage("正在下载");
        upgradeProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        upgradeProgressDialog.setCanceledOnTouchOutside(false);
        upgradeProgressDialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bManager.unregisterReceiver(broadcastReceiver);
    }

    private void registerReceiver() {

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_MENU){
            startDownload();
        }
        return super.onKeyDown(keyCode, event);
    }





    public void installApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 仅需改变这一行
        FileProvider8.setIntentDataAndType(this,
                intent, "application/vnd.android.package-archive", apkfile, true);
        startActivity(intent);
    }
}
