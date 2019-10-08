package com.mirage.live.base;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.mirage.live.utils.Utils;


/**
 * Created by kaede on 2015/10/20.
 */
public class LiveApplication extends Application {

	private static LiveApplication instance;

	private String tag = "LiveApplication";

	private static Context mContext;
	@Override
	public void onCreate() {
		super.onCreate();

		mContext = getApplicationContext();


		Utils.print(tag,"start");


		Fresco.initialize(this, ImagePipelineConfigUtils.getDefaultImagePipelineConfig(this));

	}


	@Override
	public void attachBaseContext(Context base) {
		MultiDex.install(base);
		super.attachBaseContext(base);
	}

	public static LiveApplication getInstance() {
		return instance;
	}

	public static Context getContext(){
		return mContext;
	}

	public static void setmContext(Context mContext) {
		LiveApplication.mContext = mContext;
	}


	@Override
	public void onTerminate() {
		super.onTerminate();
	}

}
