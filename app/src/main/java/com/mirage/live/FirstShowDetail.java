package com.mirage.live;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.mirage.live.base.BaseActivity;
import com.mirage.live.utils.Utils;




public class FirstShowDetail extends BaseActivity {

    private static final String TAG = "FirstShowDetail";


    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, FirstShowDetail.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstshow_detail_main);


        Utils.print(TAG,"onCreate");

    }



}
