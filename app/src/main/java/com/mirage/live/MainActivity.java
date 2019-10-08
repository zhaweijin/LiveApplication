package com.mirage.live;



import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.utils.NetworkUtils;
import com.blankj.utilcode.utils.TimeUtils;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.mirage.live.base.BaseActivity;
import com.mirage.live.constan.Constans;
import com.mirage.live.dialog.MenuPopWindow;
import com.mirage.live.upgrade.UpgradeTipsDialog;
import com.mirage.live.utils.Utils;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity {

    private static final String TAG = "Main";

    @BindView(R.id.live)
    Button live;

    @BindView(R.id.video)
    Button video;

    @BindView(R.id.time)
    TextView txTime;

    private Disposable timeDisposable;

    private MenuPopWindow menuPopWindow;

    private UpgradeTipsDialog upgradeTipsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        live.setOnClickListener(v -> MainVideoPlayer.launch(MainActivity.this));

        video.setOnClickListener(v -> {
             FirstShowMovie.launch(MainActivity.this);
        });

        menuPopWindow = new MenuPopWindow(this);
        upgradeTipsDialog = new UpgradeTipsDialog(this, new UpgradeTipsDialog.ConfirmOnClickListener() {
            @Override
            public void onOk() {
                upgradeTipsDialog.dismiss();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onDismiss() {

            }
        });

        initHeader();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出清空缓存
        ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_MENU){
            menuPopWindow.showWindow();
//            upgradeTipsDialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }


    private void initHeader(){
        txTime.setText(TimeUtils.date2String(new Date(),new SimpleDateFormat("yyyy-MM-dd HH:mm")));
        Disposable d = Observable.interval(Constans.TIME_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignored -> {
                    txTime.setText(TimeUtils.date2String(new Date(),new SimpleDateFormat("yyyy-MM-dd HH:mm")));
                });
        addDisposable(d);
    }
}
