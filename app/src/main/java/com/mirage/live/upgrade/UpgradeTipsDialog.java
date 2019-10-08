package com.mirage.live.upgrade;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.mirage.live.R;
import com.mirage.live.base.BaseDialog;
import com.mirage.live.utils.Utils;

import butterknife.BindView;


public class UpgradeTipsDialog extends BaseDialog{

    private static final String TAG = UpgradeTipsDialog.class.getSimpleName();
    private Context mContext;

    private ConfirmOnClickListener confirmOnClickListener;

    @BindView(R.id.btn_cancle)
    Button btn_cancle;


    /**
     * 对话框提示信息
     */
    @BindView(R.id.title)
    TextView title;

    @Override
    public int getLayoutId() {
        return R.layout.upgrade_tips_dialog;
    }

    /**
     * 设定宽高等属性，以及初始化ui箭头等
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.print(TAG,"222");

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = 600;
        lp.height = 400;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);

        setOnDismissListener(this);
        btn_cancle.setOnClickListener(onClickListener);
    }

    /**
     * 构造函数传递参数
     * @param context
     * @param confirmOnClickListener
     */
    public UpgradeTipsDialog(Context context, ConfirmOnClickListener confirmOnClickListener) {
        super(context, R.style.CustomDialog);
        mContext = context;
        this.confirmOnClickListener = confirmOnClickListener;
    }



    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.upgrade_ok:
                    confirmOnClickListener.onOk();
                    break;
                /*case R.id.cancel:
                    confirmOnClickListener.onCancel();
                    break;*/
            }
        }
    };

    public interface ConfirmOnClickListener{
        void onOk();
        void onCancel();
        void onDismiss();
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        confirmOnClickListener.onDismiss();
    }


    public void setTitle(String message){
        title.setText(message);
    }

    public void setTitleSize(int id){
        title.setTextSize(id);
    }

}
