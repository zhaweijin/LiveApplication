package com.mirage.live.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.utils.ToastUtils;
import com.mirage.live.base.LiveApplication;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @Title CloudScreenVipPlayer3.1.1Overseas
 * @Auther Spr_ypt
 * @Date 2016/6/6
 * @Description 用于处理需求：
 * 连续四次Loading（Loading时间10秒以内不含10秒），每次Loading平均间隔时间长10秒时弹窗提醒网速慢
 */
public class LoadingSpaceStack {
    private static final String TAG = LoadingSpaceStack.class.getSimpleName();
    private static final int CAPACITY = 3;
    private Deque<Long> stack = new LinkedList<>();
    private Context ctx = LiveApplication.getInstance().getApplicationContext();
    private FlowRateTester tester = new FlowRateTester(ctx);
    /**
     * 记录上次提醒测速的时间，用于比较2次时间间隔
     */
    private long lastRateTipTime = 0;

    /**
     * 单例话工具类
     */
    private LoadingSpaceStack() {
    }

    private static class Holder {
        public static LoadingSpaceStack INSTANCE = new LoadingSpaceStack();
    }

    public static LoadingSpaceStack getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * @Author Spr_ypt
     * @Date 2016/6/6
     * @Description 添加加载间隔时间，根据条件触发弹窗
     */
    public void addSpace(long space) {
        Log.d(TAG, "space=" + space);
        Log.d("4test", "space=" + space);
        stack.offer(space);
        //将超过数量的间隙从栈尾剔除
        for (int i = stack.size(); i > CAPACITY; i--) {
            stack.poll();
        }
        long avg = averageStack();
        Log.d(TAG, "avg=" + avg);
        Log.d(TAG, "stack.size()=" + stack.size());
        if (avg < 10000 && stack.size() == CAPACITY) {
            clear();
            tester.startTest();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    definitionAdapting(tester.getFlowRateLong(5000));
                }
            }, 5000);
        }
    }

    public void clear() {
        stack.clear();
    }

    private long averageStack() {
        Long sum = 0L;
        Iterator<Long> it = stack.iterator();
        while (it.hasNext()) {
            sum += it.next();
        }
        return sum / stack.size();
    }
    private long STAND_RATE = 250;// kb

    public void definitionAdapting(long rate) {
        Log.d("4test", "rate=" + rate);
        if (rate < STAND_RATE) {
            String rateStr = tester.formatRateStr(rate);
            definitionAdapting(rateStr);
        }
    }

    public void definitionAdapting(String rateStr) {
        if (System.currentTimeMillis() - lastRateTipTime > 20000) {
            lastRateTipTime = System.currentTimeMillis();
            ToastUtils.showLongToast(ctx, String.format("检测到卡顿，您的平均加载速度为%s", rateStr));
        }
    }
}
