package com.mirage.live.base;



import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by carter on 4/5/17.
 */

public class BaseActivity extends FragmentActivity{

    protected Context mContext;
    private String tag = "BaseActivity";


    private CompositeDisposable mCompositeDisposable;
    //public CustomProgressDialog mProgressDialog = null;




    public void addDisposable(Disposable d) {
        if (d == null) {
            return;
        }

        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = new CompositeDisposable();
        }

        this.mCompositeDisposable.add(d);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveApplication.setmContext(null);
        if (this.mCompositeDisposable != null) {
            this.mCompositeDisposable.dispose();
        }

    }
}
