package com.mirage.live.upgrade;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mirage.live.R;
import com.mirage.live.base.BaseDialog;
import com.mirage.live.utils.Utils;


import java.text.NumberFormat;

import butterknife.BindView;


public class UpgradeProgressDialog extends BaseDialog {

    private static final String TAG = UpgradeProgressDialog.class.getSimpleName();
    private Context mContext;



    @BindView(R.id.progress)
    ProgressBar progressBar;

    @BindView(R.id.progress_percent)
    TextView mProgressPercent;

    @BindView(R.id.progress_number)
    TextView mProgressNumber;

    /**
     * 对话框提示信息
     */
    @BindView(R.id.progress_message)
    TextView title;

    private boolean mHasStarted;

    private int mProgressVal;

    private Handler mViewUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int progress = progressBar.getProgress();
            int max = progressBar.getMax();
            double dProgress = (double) progress / (double) (1024 * 1024);
            double dMax = (double) max / (double) (1024 * 1024);
            if (mProgressNumberFormat != null) {
                String format = mProgressNumberFormat;
                mProgressNumber.setText(String.format(format, dProgress, dMax));
            } else {
                mProgressNumber.setText("");
            }
            if (mProgressPercentFormat != null) {
                double percent = (double) progress / (double) max;
                SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                        0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mProgressPercent.setText(tmp);
            } else {
                mProgressPercent.setText("");
            }
        }
    };
    private String mProgressNumberFormat;
    private NumberFormat mProgressPercentFormat;

    private int mMax;

    @Override
    public int getLayoutId() {
        return R.layout.upgrade_progress_dialog;
    }


    /**
     * 设定宽高等属性，以及初始化ui箭头等
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.print(TAG, "222");

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = 600;
        lp.height = 400;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);

        setOnDismissListener(this);

        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
    }


    /**
     * 构造函数传递参数
     *
     * @param context
     * @param confirmOnClickListener
     */
    public UpgradeProgressDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;

        Utils.print(TAG, "111");
        initFormats();
    }


    public interface ConfirmOnClickListener {
        void onOk();

        void onCancel();

        void onDismiss();
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        //confirmOnClickListener.onDismiss();
    }


    public void setTitle(String message) {
        title.setText(message);
    }


    private void initFormats() {
        mProgressNumberFormat = "%1.2fM/%2.2fM";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    private void onProgressChanged() {
        mViewUpdateHandler.sendEmptyMessage(0);
    }

    public void setProgressStyle(int style) {
        //mProgressStyle = style;
    }

    public int getMax() {
        if (progressBar != null) {
            return progressBar.getMax();
        }
        return mMax;
    }

    public void setMax(int max) {
        if (progressBar != null) {
            progressBar.setMax(max);
            onProgressChanged();
        } else {
            mMax = max;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (progressBar != null) {
            progressBar.setIndeterminate(indeterminate);
        }
    }

    public void setProgress(int value) {
        if (mHasStarted) {
            progressBar.setProgress(value);
            onProgressChanged();
        } else {
            mProgressVal = value;
        }
    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mHasStarted = true;
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        mHasStarted = false;
    }

}
