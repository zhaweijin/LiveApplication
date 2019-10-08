package com.mirage.live.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;

/**
 * @ClassName: FlowRateTester
 * @Description: 网速测量器，用于测量当前app使用的网速，test how much network speed app used
 * @author: yupengtong
 * @date 2014年12月12日 下午1:14:41
 */
public class FlowRateTester {
    /**
     * log tag
     */
    private static final String TAG = "FlowRateTester";
    /**
     * context
     */
    private Context mContext;
    /**
     * 当前包名，初始化时从context的方法获取，current pockage name
     */
    private String pmName;
    /**
     * 开始测量的起始量（Kb） start byte
     */
    private long startTestFlow;
    /**
     * 前次测量的量（Kb）last test byte
     */
    private long preTestFlow;
    /**
     * 流媒体最低缓存值 android 4.0.3之后流媒体最低缓冲量4M(4*1024K) 用于计算加载进度，least buffer byte
     */
    private static final long kDefaultLowWaterThreshold = 4 * 1024;

    public FlowRateTester(Context mContext) {
        super();
        this.mContext = mContext;
        pmName = mContext.getPackageName().toString();
    }

    /**
     * @Title: FlowRateTester
     * @author:yupengtong
     * @Description: 开始测量，在调用其他公用方法时必须先调用该方法以获取加载数据量的起始值，否则返回数据错乱，must be used
     * before other functions,otherwise you will get wrong date
     */
    public void startTest() {
        preTestFlow = getUidRxBytes();
        startTestFlow = preTestFlow;
    }

    /**
     * @param testTime 距前次测量的时间(ms) time after last test
     * @return 即时网速，超过Mb单位自动变更,auto change to Mb
     * @Title: FlowRateTester
     * @author:yupengtong
     * @Description: 返回即时网速，需要在startTest()之后调用,current network speed,must be
     * used after startTest()
     */
    public String getFlowRate(int testTime) {
        long currentFlow = getUidRxBytes();
        long rate = (currentFlow - preTestFlow) * 1000 / testTime;

        String flowRate = formatRateStr(rate);

        preTestFlow = currentFlow;
        return flowRate;

    }

    /**
     * @Author Spr_ypt
     * @Date 2016/6/9
     * @Description 格式话速度，返回带有单位的string
     */
    public String formatRateStr(long rate) {
        String flowRate = "";
        if (rate < 1024) {
            flowRate = String.valueOf(rate) + "KB/s";
        } else {
            flowRate = String.valueOf(rate / 1024) + "MB/s";
        }
        return flowRate;
    }

    /**
     * @Author Spr_ypt
     * @Date 2016/6/9
     * @Description 获取long类型的加载速度
     */
    public long getFlowRateLong(int testTime) {
        long currentFlow = getUidRxBytes();
        long rate = (currentFlow - startTestFlow) * 1000 / testTime;
        return rate;
    }

    /**
     * @return 0-99整数 int 0-99
     * @Title: FlowRateTester
     * @author:yupengtong
     * @Description: 获取加载进度，需要在startTest()之后调用,get buffer percent,must be used
     * after startTest()
     */
    public int getBufferPercent() {
        long currentFlow = getUidRxBytes();
        int percent = (int) ((currentFlow - startTestFlow) * 100 / kDefaultLowWaterThreshold);
        return percent > 99 ? 99 : percent;
    }

    /**
     * @return 已经获取的数据量（Kb）bytes has got
     * @Title: FlowRateTester
     * @author:yupengtong
     * @Description: 核心方法，获取对应包名应用接收的总数据量,main code,get bytes based on pockage
     * name
     */
    private long getUidRxBytes() { // 获取总的接受字节数，包含Mobile和WiFi等
        PackageManager pm = mContext.getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo(pmName, PackageManager.GET_ACTIVITIES);
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        return null != ai ? TrafficStats.getUidRxBytes(ai.uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024) : 0L;
    }
}
