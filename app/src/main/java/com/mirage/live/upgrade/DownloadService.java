package com.mirage.live.upgrade;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mirage.live.R;
import com.mirage.live.utils.Utils;

import java.io.File;

import io.reactivex.functions.Consumer;


/**
 * Created by zwj on 6/4/18.
 */

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    public static final String MESSAGE_PROGRESS = "message_progress";
    public static final String FILE_NAME = "file.apk";
    private String apkUrl = "http://download.fir.im/v2/app/install/595c5959959d6901ca0004ac?download_token=1a9dfa8f248b6e45ea46bc5ed96a0a9e&source=update";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.print(TAG,"onHandleIntent");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Download")
                .setContentText("Downloading File")
                .setAutoCancel(true);

        notificationManager.notify(0, notificationBuilder.build());

        download();
    }

    private void download() {
        Utils.print(TAG,"download");
        DownloadProgressListener listener = new DownloadProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                Download download = new Download();
                download.setTotalFileSize(contentLength);
                download.setCurrentFileSize(bytesRead);
                int progress = (int) ((bytesRead * 100) / contentLength);
                download.setProgress(progress);

                sendNotification(download);
            }
        };
        File outputFile = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
        Log.v(TAG,"outfile="+outputFile.getAbsolutePath());
        String baseUrl = StringUtils.getHostName(apkUrl);

        new DownloadAPI(baseUrl, listener)
                .downloadAPK(apkUrl, outputFile, new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        downloadCompleted();
                    }
                });
    }

    private void downloadCompleted() {
        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);

        /*notificationManager.cancel(0);
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText("File Downloaded");
        notificationManager.notify(0, notificationBuilder.build());*/
    }

    private void sendNotification(Download download) {

        sendIntent(download);
        /*notificationBuilder.setProgress(100, download.getProgress(), false);
        notificationBuilder.setContentText(
                StringUtils.getDataSize(download.getCurrentFileSize()) + "/" +
                        StringUtils.getDataSize(download.getTotalFileSize()));
        notificationManager.notify(0, notificationBuilder.build());*/
    }

    private void sendIntent(Download download) {

        Intent intent = new Intent(MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }
}