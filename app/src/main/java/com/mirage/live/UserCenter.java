package com.mirage.live;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mirage.live.base.BaseActivity;

public class UserCenter extends BaseActivity{

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, UserCenter.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter);
    }
}
