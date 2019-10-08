package com.mirage.live;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import android.widget.RelativeLayout;

import com.mirage.live.adapter.FirstShowPreviewAdapter;
import com.mirage.live.base.BaseActivity;
import com.mirage.live.base.ItemClickListener;

import com.mirage.live.model.FirstShowPreviewItem;
import com.mirage.live.utils.FlowRateTester;
import com.mirage.live.utils.LoadingSpaceStack;
import com.mirage.live.utils.Utils;
import com.mirage.live.widget.HiveviewVideoView;
import com.wang.avi.AVLoadingIndicatorView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class FirstShowMovie extends BaseActivity implements ItemClickListener<FirstShowPreviewItem> {

    private static final String TAG = "FirstShowMovie";
    
    @BindView(R.id.preview_recyclerview)
    RecyclerView recyclerView;


    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;

    @BindView(R.id.video)
    HiveviewVideoView videoView;

    private FirstShowPreviewAdapter firstShowPreviewAdapter;

    private String testIP = "http://192.168.31.107:80";

    private int itemSpace;


    private HiveViewVideoState hiveViewVideoState = new HiveViewVideoState();
    private Handler loadingHandler = new LoadingHandler(this);
    protected static final int BUFFER_MSG_FRESH_RATE = 500;
    protected static final int WAITING_NETWORK_CONNECT = 0x0002;
    /**
     * 需要显示toast
     */
    protected static final int LOADING_OVER_TIME_NEED_TOAST = 0X11011;

    protected  static  final int AD_OVERTIME_TOAST = 0X0003;
    /**
     * msg.what
     */
    protected static final int LOADING_MSG_FRESH = 0x0001;
    /**
     * 网络智判
     */
    protected FlowRateTester flowRateTester;
    /**
     * msg.what
     */
    public static final int BUFFER_OVERTIME = 0x120002;

    /**
     * 网络异常弹窗出现延迟时间，delay time before nerwork error dialog show
     */
    public static final int BUFFER_OVERTIME_DELAY = 10000;// 10秒

    /**
     * 网络异常Handler，network error Handler
     */
    Handler bufferOvertimeHandler = new BufferOvertimeHandler(this);

    private FirstShowPreviewItem currentItem;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, FirstShowMovie.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_show_main);

        itemSpace = 50;
        initRecyclerView();
        intRecyclerViewData();


        requestFocus();
    }


    private void requestFocus(){
        Disposable focusDisposable = Observable.timer(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if(recyclerView.getChildAt(0)!=null){
                        Utils.print(TAG,"request focus");
                        recyclerView.getChildAt(0).requestFocus();
                    }else {
                        Utils.print(TAG,"null");
                    }
                });
        addDisposable(focusDisposable);
    }

    @Override
    public void onItemClickListener(FirstShowPreviewItem item) {
        FirstShowDetail.launch(FirstShowMovie.this);
    }

    @Override
    public void onItemSelectedListener(FirstShowPreviewItem item) {
        if(item!=currentItem){
            currentItem = item;
            initPlay(currentItem);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.print(TAG,"onPause");
        if(videoView!=null && videoView.isPlaying()){
            videoView.stopPlayback();
        }
    }


    private void initRecyclerView(){
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(itemSpace, 0, 0, 0);
            }
        });
    }


    private void intRecyclerViewData(){
        List<FirstShowPreviewItem> datas = new ArrayList<>();
        FirstShowPreviewItem item1 = new FirstShowPreviewItem();
        item1.setResourceID(R.drawable.a);
        item1.setUrl(testIP+"/test.mp4");
        datas.add(item1);

        FirstShowPreviewItem item2 = new FirstShowPreviewItem();
        item2.setResourceID(R.drawable.b);
        item2.setUrl(testIP+"/test.mp4");
        datas.add(item2);

        FirstShowPreviewItem item3 = new FirstShowPreviewItem();
        item3.setResourceID(R.drawable.c);
        item3.setUrl(testIP+"/test.mp4");
        datas.add(item3);

        FirstShowPreviewItem item4 = new FirstShowPreviewItem();
        item4.setResourceID(R.drawable.d);
        item4.setUrl(testIP+"/test.mp4");
        datas.add(item4);

        FirstShowPreviewItem item5 = new FirstShowPreviewItem();
        item5.setResourceID(R.drawable.e);
        item5.setUrl(testIP+"/test.mp4");
        datas.add(item5);

        FirstShowPreviewItem item6 = new FirstShowPreviewItem();
        item6.setResourceID(R.drawable.f);
        item6.setUrl(testIP+"/test.mp4");
        datas.add(item5);

        Utils.print(TAG,"size=="+datas.size());
        //初始化适配器
        firstShowPreviewAdapter = new FirstShowPreviewAdapter(R.layout.first_show_preview_item,datas);
        recyclerView.setAdapter(firstShowPreviewAdapter);

        firstShowPreviewAdapter.setItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoStart();
    }

    private void initPlay(FirstShowPreviewItem item){
        if(item==null) return;

        Utils.print(TAG,"initplay");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        videoView.setLayoutParams(layoutParams);
        videoView.setHiveViewVideoStateListener(hiveViewVideoState);
        videoView.setVideoURI(Uri.parse(item.getUrl()));
        videoView.setOnInfoListener(mOnInfoListener);
        videoView.openVideo();

        bufferStart(true,false);

        videoStart();
    }

    private void videoStart(){
        if(videoView!=null && !videoView.isPlaying()){
            Utils.print(TAG,"video start");
            videoView.start();
        }
    }

    private class HiveViewVideoState implements HiveviewVideoView.HiveViewVideoStateListener {

        @Override
        public void onPrepared(int width,int height) {
            Utils.print(TAG,"onPrepared="+width+",h="+height);
        }

        @Override
        public void onMovieStart() {
            Utils.print(TAG,"onMovieStart");
        }

        @Override
        public void onMoviePause() {
            Utils.print(TAG,"onMoviePause");
        }

        @Override
        public void onMovieComplete() {
            Utils.print(TAG,"onMovieComplete");
        }

        @Override
        public void onMovieStop() {
            Utils.print(TAG,"onMovieStop");
        }

        @Override
        public boolean onError() {
            Utils.print(TAG,"onError");
            return false;
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Utils.print(TAG,"onBufferingUpdate");
        }

        @Override
        public void onVideoSizeChange(int width, int height) {
            Utils.print(TAG,"onVideoSizeChange="+width+",h="+height);
        }

        @Override
        public void onSeekComplete() {
            Utils.print(TAG,"onSeekComplete");
        }

        @Override
        public void onBufferStart() {
            Utils.print(TAG,"onBufferStart");
        }

        @Override
        public void onBufferEnd() {
            Utils.print(TAG,"onBufferEnd");
        }
    }

    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.i(TAG, "mOnInfoListener   what=" + what + "   extra=" + extra);
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    bufferStart(false,false);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    bufferEnd();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private static class LoadingHandler extends Handler {
        private WeakReference<FirstShowMovie> ref;

        public LoadingHandler(FirstShowMovie activity) {
            ref = new WeakReference<FirstShowMovie>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FirstShowMovie activity = ref.get();
            switch (msg.what) {
                case LOADING_MSG_FRESH:
                    if (null != activity) {
                        removeMessages(LOADING_MSG_FRESH);
                        activity.loadingHandler.sendEmptyMessageDelayed(LOADING_MSG_FRESH, BUFFER_MSG_FRESH_RATE);
                    }
                    break;
                case WAITING_NETWORK_CONNECT:
                    if (null != activity) {
                        //activity.checkNetwork();
                    }
                    break;
                case LOADING_OVER_TIME_NEED_TOAST:
                    long currentRate = activity.flowRateTester.getFlowRateLong(10000);
                    Utils.print(TAG,"currentRate=="+activity.flowRateTester.getFlowRateLong(10000));
                    LoadingSpaceStack.getInstance().definitionAdapting(currentRate);
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * @Title: MainActivity
     * @author:yupengtong
     * @Description: 缓冲开始显示加载动画方法，show loading anim when buffering
     */
    private void bufferStart(boolean isFirstStart, boolean isLauncherStart) {
        Log.d(TAG, "bufferStart: -----> "+"isFirstStart="+isFirstStart);
        if (isFirstStart) {
            avi.show();
            bufferOvertimeHandler.sendEmptyMessageDelayed(BUFFER_OVERTIME, BUFFER_OVERTIME_DELAY);
        } else {
            loadingHandler.sendEmptyMessageDelayed(LOADING_MSG_FRESH, BUFFER_MSG_FRESH_RATE);
            flowRateTester.startTest();
        }

    }

    /**
     * @Title: MainActivity
     * @author:yupengtong
     * @Description: 缓冲结束后去除加载动画方法，hide loading anim when buffer finished
     */
    private void bufferEnd() {
        Log.d(TAG, "bufferEnd: -----> ");
        avi.hide();
        if (null != bufferOvertimeHandler) {
            deleteToastDelayMsg();
            bufferOvertimeHandler.removeMessages(BUFFER_OVERTIME);
        }

        if (null != loadingHandler) {
            loadingHandler.removeMessages(LOADING_MSG_FRESH);
        }

    }

    private void deleteToastDelayMsg() {
        if (loadingHandler.hasMessages(LOADING_OVER_TIME_NEED_TOAST)) {
            loadingHandler.removeMessages(LOADING_OVER_TIME_NEED_TOAST);
        }
    }


    protected static class BufferOvertimeHandler extends Handler {
        private WeakReference<FirstShowMovie> reference;

        public BufferOvertimeHandler(FirstShowMovie activity) {
            reference = new WeakReference<>(activity);
        }

        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FirstShowMovie activity = reference.get();
            if (null != activity && !activity.isFinishing() && !activity.isDestroyed()) {
                switch (msg.what) {
                    case BUFFER_OVERTIME:
                        removeMessages(BUFFER_OVERTIME);
                        break;
                    default:
                        break;
                }

            }
        }
    }
}
