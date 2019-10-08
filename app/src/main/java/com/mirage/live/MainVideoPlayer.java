package com.mirage.live;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.mirage.live.adapter.LiveTwoListAdapter;
import com.mirage.live.base.BaseActivity;
import com.mirage.live.base.ItemClickListener;
import com.mirage.live.constan.Constans;
import com.mirage.live.dialog.ChannelPopWindow;
import com.mirage.live.model.ChannelTwoItem;
import com.mirage.live.model.LiveListItem;
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
import io.reactivex.functions.Consumer;


public class MainVideoPlayer extends BaseActivity implements ItemClickListener<ChannelTwoItem>{

    @BindView(R.id.video)
    HiveviewVideoView videoView;

    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;


    @BindView(R.id.playbackCurrentTime)
    TextView playbackCurrentTime;

    @BindView(R.id.playbackEndTime)
    TextView playbackEndTime;

    @BindView(R.id.playbackSeekBar)
    SeekBar playbackSeekBar;


    @BindView(R.id.bottomControls)
    LinearLayout bottomControls;

    @BindView(R.id.stop_status)
    ImageView stop_status;


    private static final String TAG = "live";
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


    protected Disposable progressUpdateReactor;

    public boolean isSeeking = false;
    protected int seekTo = -1;
    protected  final int UPDATE_PROGRESS = 1;
    protected  final int MSG_SEEK = 2;
    private int seekCount = 0;
    private final int FIRST_SEEK_NUM = 1;
    private final int SECONDE_SEEK_NUM = 2;
    protected final SeekBarHandler mSeekBarHandler = new SeekBarHandler();


    private boolean isLive = true;


    private ChannelPopWindow channelPopWindow;
    private ChannelTwoItem currentItem;

    private Disposable bottomControlDisposable;
    private final int BUTTOM_CONTROL_SHOW_TIMEOUT = 8;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, MainVideoPlayer.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_main);


        flowRateTester = new FlowRateTester(this);
        channelPopWindow = new ChannelPopWindow(this,this);

        currentItem = channelPopWindow.getDefaultPlayItem();
        initPlay(currentItem);
    }



    @Override
    protected void onResume() {
        super.onResume();
        Utils.print(TAG,"onResume");
        videoStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.print(TAG,"onPause");
        if(videoView!=null && videoView.isPlaying()){
            videoView.stopPlayback();
        }
        stopProgressLoop();
    }

    private void initPlay(ChannelTwoItem item){
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
        /*videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Utils.print(TAG,"what=="+what);
                return true;
            }
        });*/



        /*videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Utils.print(TAG,"prepare");
                playbackSeekBar.setMax((int)videoView.getDuration());
                playbackEndTime.setText(Utils.getTimeString((int) videoView.getDuration()));
                playbackSeekBar.setKeyProgressIncrement(5);
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Utils.print(TAG,"play finish");
                //finish();
                //stopProgressLoop();
            }
        });*/
        bufferStart(true,false);

        videoStart();
        startProgressLoop();
        resetBottomControlsTimer();
    }


    @Override
    public void onItemClickListener(ChannelTwoItem item) {
        Utils.print(TAG,">>>");
        if(item!=currentItem){
            initPlay(item);
        }
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

            playbackSeekBar.setMax((int)videoView.getDuration());
            playbackEndTime.setText(Utils.getTimeString((int) videoView.getDuration()));
            playbackSeekBar.setKeyProgressIncrement(5);
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
            //mVideoWidth = width;
            //mVideoHeight = height;
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
        private WeakReference<MainVideoPlayer> ref;

        public LoadingHandler(MainVideoPlayer activity) {
            ref = new WeakReference<MainVideoPlayer>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainVideoPlayer activity = ref.get();
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
        private WeakReference<MainVideoPlayer> reference;

        public BufferOvertimeHandler(MainVideoPlayer activity) {
            reference = new WeakReference<>(activity);
        }

        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainVideoPlayer activity = reference.get();
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


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER){
                Utils.print(TAG,"onclick center");
                if(isLive){
                    if(!channelPopWindow.isShowing()){
                        channelPopWindow.showWindow();
                        channelPopWindow.resetFocus();
                    }
                }else{
                    bottomControls.setVisibility(View.VISIBLE);
                    resetBottomControlsTimer();

                    if(videoView.isPlaying()){
                        videoView.pause();
                        stop_status.setVisibility(View.VISIBLE);
                        stop_status.bringToFront();
                    }else{
                        videoView.start();
                        stop_status.setVisibility(View.INVISIBLE);
                    }
                }
            }else if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
                Utils.print(TAG,"back....");
                /*if(channelPopWindow.isShowing()){
                    channelPopWindow.dismiss();
                    return true;
                }*/
            }else if(event.getKeyCode()==KeyEvent.KEYCODE_MENU){
                Utils.print(TAG,"menu");
                videoView.setScreenMode(Constans.MODE_4TO3);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                isSeeking = true;
                stopProgressLoop();
                //playerImpl.onStartTrackingTouch(playerImpl.playbackSeekBar);
                if (event.getRepeatCount() > 60) {
                    seekCount = FIRST_SEEK_NUM;
                } else if (event.getRepeatCount() > 120) {
                    seekCount = SECONDE_SEEK_NUM;
                } else {
                    seekCount = 0;
                }
                onPlayReverse();
            }else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
                if (videoView.getCurrentPosition() + 10000 >= videoView.getDuration()) {
                    return false;
                }

                isSeeking = true;
                stopProgressLoop();
                //playerImpl.onStartTrackingTouch(playerImpl.playbackSeekBar);
                if (event.getRepeatCount() > 60) {
                    seekCount = FIRST_SEEK_NUM;
                } else if (event.getRepeatCount() > 120) {
                    seekCount = SECONDE_SEEK_NUM;
                } else {
                    seekCount = 0;
                }
                onPlaySpeed();
            }
        }else if(event.getAction()==KeyEvent.ACTION_UP){
            if ((event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) && isSeeking) {
                mSeekBarHandler.sendEmptyMessageAtTime(MSG_SEEK, 100);
            } else {
                seekTo = -1;
            }
            Log.i(TAG, "isSeeking:ACTION_UP");
        }

        return super.dispatchKeyEvent(event);
    }




    protected void startProgressLoop() {
        if (progressUpdateReactor != null) progressUpdateReactor.dispose();
        progressUpdateReactor = getProgressReactor();
    }

    protected void stopProgressLoop() {
        if (progressUpdateReactor != null) progressUpdateReactor.dispose();
        progressUpdateReactor = null;
    }

    private Disposable getProgressReactor() {
        return Observable.interval(Constans.PROGRESS_LOOP_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignored -> triggerProgressUpdate());
    }


    private void triggerProgressUpdate(){
        /*if (duration != playbackSeekBar.getMax()) {
            playbackEndTime.setText(getTimeString(duration));
            initSeekBar(duration);
        }*/
        int currentProgress = videoView.getCurrentPosition();
        if (videoView!=null && videoView.isPlaying()) {
            playbackSeekBar.setProgress(currentProgress);
                //保证在最后一条的时候不弹窗
                //切上下首时，duration会变成0，导致触发Toast，所以需要加入duration大于0
                /*if (simpleExoPlayer.getDuration() > 0 && simpleExoPlayer.getCurrentPosition() + 1000 > simpleExoPlayer.getDuration() && playQueue.getIndex() < playQueue.size() - 2) {
                    Toast.makeText(context, context.getResources().getString(R.string.play_auto_next), Toast.LENGTH_SHORT).show();
                }*/
            playbackCurrentTime.setText(Utils.getTimeString(currentProgress));
        }
    }


    private void quickSeek() {
        isSeeking = false;
        Log.i(TAG, "isSeeking:" + isSeeking);
        int durationTime = (int) videoView.getDuration();
        if (seekTo != 0 && durationTime != 0) {
            if (seekTo < durationTime && seekTo > 0) {
                seekTo = (seekTo >= durationTime) ? durationTime : seekTo;
                videoView.seekTo(seekTo);
                    /*if (!isPlaying()) {
                        simpleExoPlayer.setPlayWhenReady(true);
                        onPlay();
                        animateView(currentDisplaySeek, AnimationUtils.Type.SCALE_AND_ALPHA, false, 200);
                    }*/
                Log.e(TAG, "last 2 : " + seekTo);
                seekTo = -1;
                mSeekBarHandler.removeMessages(MSG_SEEK);
            }
        }

        startProgressLoop();
    }



    protected class SeekBarHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    playbackSeekBar.setProgress(seekTo);
                    playbackCurrentTime.setText(Utils.getTimeString(playbackSeekBar.getProgress()));
                    removeMessages(UPDATE_PROGRESS);
                    break;
                case MSG_SEEK:
                    quickSeek();
                    break;

            }
        }
    }

    protected void delaySeekTo(int seekTo) {
        this.seekTo = seekTo;
        mSeekBarHandler.sendEmptyMessage(UPDATE_PROGRESS);
    }

    public void onPlayReverse() {
        Utils.print(TAG,"onplayReverse");
        int last = (seekTo == -1) ? (int) videoView.getCurrentPosition() : seekTo;
        last = last - 10000 * (seekCount + 1);
        if (last <= 0) {
            last = 1000;
        }
        delaySeekTo(last);
    }

    public void onPlaySpeed() {
        Utils.print(TAG,"OnPlaySpeed");
        int last = (seekTo == -1) && videoView != null ? (int) videoView.getCurrentPosition() : seekTo;
        last = last + 10000 * (seekCount + 1);
        if (videoView != null && last > videoView.getDuration()) {
            return;
        }
        delaySeekTo(last);
    }

    private void setBottomControlsTimeout(){
        Observable<Long> observable = Observable.timer(BUTTOM_CONTROL_SHOW_TIMEOUT, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread());
        if(bottomControlDisposable!=null && bottomControlDisposable.isDisposed()) bottomControlDisposable.dispose();
        bottomControlDisposable = observable.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                viewAlphaInvisible(bottomControls);
            }
        });
    }

    /**
     * 重置定时器
     */
    private void resetBottomControlsTimer(){
        if(bottomControlDisposable!=null){
            bottomControlDisposable.dispose();
        }
        setBottomControlsTimeout();
    }


    /**
     * view 动画渐变隐藏
     * @param view
     */
    private void viewAlphaInvisible(View view){
        Utils.print(TAG,"viewAlphaInvisible");
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);//初始化操作，参数传入0和1，即由透明度0变化到透明度为1
        view.startAnimation(alphaAnimation);//开始动画
        alphaAnimation.setFillAfter(true);//动画结束后保持状态
        alphaAnimation.setDuration(1000);//动画持续时间，单位为毫秒

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        alphaAnimation.start();
    }


    /**
     * view 动画渐变显示
     * @param view
     */
    private void viewAlphaVisible(View view) {
        Utils.print(TAG, "viewAlphaVisible");
        view.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);//初始化操作，参数传入0和1，即由透明度0变化到透明度为1
        view.startAnimation(alphaAnimation);//开始动画
        alphaAnimation.setFillAfter(true);//动画结束后保持状态
        alphaAnimation.setDuration(1000);//动画持续时间，单位为毫秒

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onItemSelectedListener(ChannelTwoItem item) {

    }



}
