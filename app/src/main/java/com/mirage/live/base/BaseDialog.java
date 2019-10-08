package com.mirage.live.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mirage.live.utils.Utils;

import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


/**
 * Created by carter on 4/11/17.
 */

public abstract class BaseDialog extends Dialog implements DialogInterface.OnDismissListener{

    private CompositeDisposable mCompositeDispoable;

    private String tag = "BaseDialog";
    private Context context;

    public abstract int getLayoutId();

    public BaseDialog(Context context){
        super(context);
        this.context = context;
    }

    public BaseDialog(Context context, int type){
        super(context,type);
        this.context = context;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (this.mCompositeDispoable != null) {
            this.mCompositeDispoable.dispose();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
    }

    public void addDispose(Disposable d) {
        if (d == null) {
            return;
        }

        if (this.mCompositeDispoable == null) {
            this.mCompositeDispoable = new CompositeDisposable();
        }

        this.mCompositeDispoable.add(d);
    }


}
