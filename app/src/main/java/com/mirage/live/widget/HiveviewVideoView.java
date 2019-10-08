/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mirage.live.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;


import com.mirage.live.constan.Constans;
import com.mirage.live.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;


//import android.media.Metadata;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class HiveviewVideoView extends SurfaceView implements MediaPlayerControl {
    private static final String TAG = "HiveviewVideoView";
    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders;
    private int mDuration;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private Context mContext = null;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private MediaController mMediaController;
    private OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;

    private int mSeekWhenPrepared; // recording the seek position while
    // preparing
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;


    private int videoAspectMode = Constans.MODE_FULLSCREEN;

    public void setVideoAspectMode(int videoAspectMode) {
        this.videoAspectMode = videoAspectMode;
    }

    public HiveviewVideoView(Context context) {
        super(context);
        mContext = context;
        initHiveviewVideoView();
    }

    public HiveviewVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initHiveviewVideoView();
    }

    public HiveviewVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initHiveviewVideoView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.i("@@@@", "onMeasure");
        Log.i(TAG, "onMeasure-->start");
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        Log.i(TAG, "onMeasure-->width:" + width);
        Log.i(TAG, "onMeasure-->height:" + height);
        Log.i(TAG, "onMeasure-->mVideoWidth:" + mVideoWidth);
        Log.i(TAG, "onMeasure-->mVideoHeight:" + mVideoHeight);

        if (mVideoWidth > 0 && mVideoHeight > 0) {
            /*if (mVideoWidth * height > width * mVideoHeight) {
                // Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height < width * mVideoHeight) {
                // Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else {
                // Log.i("@@@", "aspect ratio is correct: " +
                // width+"/"+height+"="+
                // mVideoWidth+"/"+mVideoHeight);
            }*/

            switch (videoAspectMode){
                case Constans.MODE_DEFUALT:
                    width=mVideoWidth;
                    height=mVideoHeight;
                    break;
                case Constans.MODE_4TO3:
                    width = (mVideoHeight * 4) / 3;
                    height = mVideoHeight;
                    break;
                case Constans.MODE_16TO9:
                    height = (mVideoHeight * 16) / 9;
                    width = mVideoWidth;
                    break;
                case Constans.MODE_FULLSCREEN:
                    break;
            }
        }




        /*if(mVideoHeight > width || mVideoHeight > height){
            float wRatio = (float)mVideoWidth/(float)width;
            float hRatio = (float)mVideoHeight/(float)height;
            float ratio = Math.max(wRatio, hRatio);
            mVideoWidth = (int)Math.ceil((float)mVideoWidth/ratio);
            mVideoHeight = (int)Math.ceil((float)mVideoHeight/ratio);
        }*/


        // if (mVideoWidth > 0 && mVideoHeight > 0) {
        // width = height * mVideoWidth / mVideoHeight;
        // }
        Log.i(TAG, "setting size: " + width + 'x' + height);
        setMeasuredDimension(width, height);
        // setMeasuredDimension(mVideoWidth, mVideoHeight);
        Log.i(TAG, "onMeasure-->end");
    }


    public void setScreenMode(int mode){
        setVideoAspectMode(mode);
        requestLayout();
    }


    @SuppressLint("NewApi")
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(HiveviewVideoView.class.getName());
    }

    @SuppressLint("NewApi")
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(HiveviewVideoView.class.getName());
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            /*
             * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
                result = desiredSize;
                break;

            case MeasureSpec.AT_MOST:
            /*
             * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
                result = Math.min(desiredSize, specSize);
                break;

            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }



    private boolean hasSetParameter = false;

    private void initHiveviewVideoView() {
        Class<MediaPlayer> mpc = MediaPlayer.class;
        Method[] methods = mpc.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if ("setParameter".equals(methods[i].getName())) {
                hasSetParameter = true;
            }
        }

        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * 检查片源是否重复，如果和当前url相同，不再重新设置
     *
     * @param uri
     * @Title setVideoURI
     * @author haozening
     * @Description
     */
    public void setVideoURI(Uri uri) {
        Log.d(TAG, "uri=" + uri);
        if (!uri.equals(mUri)) {// 相同URL片源无需重复加载
            setVideoURI(uri, null);
        } else {
            Log.e(TAG, "相同URL片源无需重复加载 uri=" + uri.toString());
        }
    }

    /**
     * 没有检查片源是否重复
     *
     * @param uri 视频地址
     * @Title setVideoURINoCheck
     * @author haozening
     */
    public void setVideoURINoCheck(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        if (!isValidURI(uri.toString())) {
            Log.e(TAG, "play uri invalidate : url=" + uri.toString());
            return;
        }
        mUri = uri;
        Log.d(TAG, "mUri=" + mUri);
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            // 关闭静帧功能
            // mMediaPlayer.setParameter(MediaPlayer.KEY_PARAMETER_AML_PLAYER_SET_DISP_LASTFRAME,
            // 0);
            mMediaPlayer.stop();
            mMediaPlayer.release();
            Log.d(TAG, "---->[videolive][stopPlayback] mMediaPlayer.release();");
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            if (null != listener) {
                listener.onMovieStop();
            }
        }
    }

    @SuppressLint("NewApi")
    public void openVideo() {
        Log.i(TAG, "openVideo-->start");
        Log.d(TAG, "mUri == null" + (mUri == null));
        Log.d(TAG, "mSurfaceHolder == null" + (mSurfaceHolder == null));
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the
        // framework.
        // Intent i = new Intent("com.android.music.musicservicecommand");
        // i.putExtra("command", "pause");
        // mContext.sendBroadcast(i);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        if (mMediaPlayer != null) {
            Log.e(TAG, "openVideo-->setParameter = 1");
            mCurrentState = STATE_PREPARED;
        }
        release(false);
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mContext, mUri);
            // mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            mMediaPlayer.setDisplay(null);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
        Log.i(TAG, "openVideo-->end");
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            Log.i(TAG, "onVideoSizeChanged-->before mVideoWidth:" + mVideoWidth);
            Log.i(TAG, "onVideoSizeChanged-->before mVideoHeight:" + mVideoHeight);

            /*switch (videoAspectMode){
                case MODE_DEFUALT:
                    break;
                case MODE_4TO3:
                    mVideoWidth = (mVideoHeight * 4) / 3;
                    break;
                case MODE_16TO9:
                    mVideoWidth = (mVideoHeight * 16) / 9;
                    break;
                case MODE_FULLSCREEN:
                    break;
            }*/
            Log.i(TAG, "onVideoSizeChanged-->mVideoWidth:" + mVideoWidth);
            Log.i(TAG, "onVideoSizeChanged-->mVideoHeight:" + mVideoHeight);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                requestLayout();
            }
            if (null != listener) {
                listener.onVideoSizeChange(width, height);
            }
        }
    };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            Log.e(TAG, "onPrepared-->setParameter = 1");
            mCurrentState = STATE_PREPARED;
            if (null != listener) {
                listener.onPrepared(mp.getVideoWidth(),mp.getVideoHeight());
            }
            // Get the capabilities of the player for this stream
            // Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
            // MediaPlayer.BYPASS_METADATA_FILTER);
            //
            // if (data != null) {
            // mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
            // || data.getBoolean(Metadata.PAUSE_AVAILABLE);
            // mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
            // || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
            // mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
            // || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
            // } else {
            mCanPause = mCanSeekBack = mCanSeekForward = true;
            // }

//            if (mOnPreparedListener != null) {
//                mOnPreparedListener.onPrepared(mMediaPlayer);
//            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            // int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may
            // be changed after seekTo() call
            // if (seekToPosition != 0) {
            // seekTo(seekToPosition);
            // }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                // Log.i("@@@@", "video size: " + mVideoWidth +"/"+
                // mVideoHeight);
                Log.i(TAG, "onPrepared-->before mVideoWidth:" + mVideoWidth);
                Log.i(TAG, "onPrepared-->before mVideoHeight:" + mVideoHeight);
                mVideoWidth = (mVideoHeight * 16) / 9;
                Log.i(TAG, "onPrepared-->mVideoWidth:" + mVideoWidth);
                Log.i(TAG, "onPrepared-->mVideoHeight:" + mVideoHeight);
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the
                    // size
                    // we need), so we won't get a "surface changed" callback,
                    // so
                    // start the video here instead of in the callback.

                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    }
                    // else if (!isPlaying() &&
                    // (seekToPosition != 0 || getCurrentPosition() > 0)) {
                    // if (mMediaController != null) {
                    // // Show the media controls when we're paused into a video
                    // and make 'em stick.
                    // mMediaController.show(0);
                    // }
                    // }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }

        }
    };

    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "onCompletion");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mMediaController != null) {
                mMediaController.hide();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
            if (null != listener) {
                listener.onMovieComplete();
            }
        }
    };

    private OnErrorListener mErrorListener = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.i(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mMediaController != null) {
                mMediaController.hide();
            }

			/* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }
            if (null != listener) {
                listener.onError();
            }

            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.i(TAG, "onBufferingUpdate");
            mCurrentBufferPercentage = percent;
            if (null != listener) {
                listener.onBufferingUpdate(mp, percent);
            }
        }
    };

    private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.i(TAG, "onSeekComplete");
            if (null != listener) {
                listener.onSeekComplete();
            }
        }
    };


    /**
     * Register a callback to be invoked when the media file is loaded and ready
     * to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been
     * reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or
     * setup. If no listener is specified, or if the listener returned false,
     * VideoView will inform the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event occurs
     * during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.i(TAG, "surfaceChanged-->start");
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            Log.i(TAG, "surfaceChanged-->mSurfaceWidth:" + mSurfaceWidth);
            Log.i(TAG, "surfaceChanged-->mSurfaceHeight:" + mSurfaceHeight);
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
            Log.i(TAG, "surfaceChanged-->end");
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "surfaceCreated-->start");
            mSurfaceHolder = holder;
            openVideo();
            Log.i(TAG, "surfaceCreated-->end");
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "surfaceDestroyed-->start");
            // after we return from this we can't use the surface any more

            mSurfaceHolder = null;
            if (mMediaController != null)
                mMediaController.hide();
            release(true);

            Log.i(TAG, "surfaceDestroyed-->end");
        }
    };

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        Log.i(TAG, "release-->start");
        if (mMediaPlayer != null) {
            //调试播放器埋点。
            if (null != listener) {
                listener.onMovieStop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            Log.d(TAG, "---->videolive mMediaPlayer.release();");
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                Log.i(TAG, "release-->cleartargetstate");
                mTargetState = STATE_IDLE;
            }

        }
        Log.i(TAG, "release-->end");
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    public void start() {
        Log.i(TAG, "start isInPlaybackState=" + isInPlaybackState());
        mTargetState = STATE_PLAYING;
        if (isInPlaybackState()) {
            if (null != listener && !mMediaPlayer.isPlaying()) {
                listener.onMovieStart();
            }
            if (mTargetState == STATE_PLAYING) {
                mMediaPlayer.start();
                mCurrentState = STATE_PLAYING;
            }
        }
    }

    public void pause() {
        mTargetState = STATE_PAUSED;
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        if (null != listener) {
            listener.onMoviePause();
        }
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        Log.i(TAG, "resume");
        if (null != listener) {
            listener.onMovieStart();
        }
        openVideo();
    }

    // cache duration as mDuration for faster access
    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public static boolean isValidURI(String uri) {
        if (uri == null || uri.indexOf(' ') >= 0 || uri.indexOf('\n') >= 0) {
            return false;
        }
        String scheme = Uri.parse(uri).getScheme();
        if (scheme == null) {
            return false;
        }

        // Look for period in a domain but followed by at least a two-char TLD
        // Forget strings that don't have a valid-looking protocol
        int period = uri.indexOf('.');
        if (period >= uri.length() - 2) {
            return false;
        }
        int colon = uri.indexOf(':');
        if (period < 0 && colon < 0) {
            return false;
        }
        if (colon >= 0) {
            if (period < 0 || period > colon) {
                // colon ends the protocol
                for (int i = 0; i < colon; i++) {
                    char c = uri.charAt(i);
                    if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
                        return false;
                    }
                }
            } else {
                // colon starts the port; crudely look for at least two numbers
                if (colon >= uri.length() - 2) {
                    return false;
                }
                for (int i = colon + 1; i < colon + 3; i++) {
                    char c = uri.charAt(i);
                    if (c < '0' || c > '9') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        Log.i(TAG, "isInPlaybackState::" + (mMediaPlayer != null) + "  " + (mCurrentState != STATE_ERROR) + "  " + (mCurrentState != STATE_IDLE) + "  " + (mCurrentState != STATE_PREPARING));
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public boolean canPause() {
        return mCanPause;
    }

    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    public int getAudioSessionId() {
        return 0;
    }

    private HiveViewVideoStateListener listener;

    public void setHiveViewVideoStateListener(HiveViewVideoStateListener listener) {
        this.listener = listener;
    }


    public interface HiveViewVideoStateListener {
        void onPrepared(int width,int height);

        void onMovieStart();

        void onMoviePause();

        void onMovieComplete();

        void onMovieStop();

        boolean onError();

        void onBufferingUpdate(MediaPlayer mp, int percent);

        void onVideoSizeChange(int width, int height);

        void onSeekComplete();

        void onBufferStart();

        void onBufferEnd();

    }
}
