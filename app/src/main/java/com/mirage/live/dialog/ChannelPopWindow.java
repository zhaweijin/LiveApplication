package com.mirage.live.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.blankj.utilcode.utils.ScreenUtils;
import com.blankj.utilcode.utils.TimeUtils;
import com.mirage.live.R;
import com.mirage.live.adapter.ChannelOneListAdapter;
import com.mirage.live.adapter.ChannelTwoListAdapter;
import com.mirage.live.base.ItemClickListener;
import com.mirage.live.base.OnChannelItemClick;
import com.mirage.live.model.ChannelOneItem;
import com.mirage.live.model.ChannelTwoItem;
import com.mirage.live.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class ChannelPopWindow extends PopupWindow implements OnChannelItemClick{

    private static final String TAG = "channelPopWindow";

    private Context mContext;

    private String testIP = "http://192.168.31.107:80";

    /*弹框布局*/
    private View container;

    /*回调接口*/

    private RecyclerView channel_one_recyclerview;
    private RecyclerView channel_two_recyclerview;
    private Resources resources;


    private ChannelOneListAdapter channelOneAdapter;
    private ChannelTwoListAdapter channelTwoAdapter;

    private ItemClickListener itemClickListener;

    private int selectItem=0;
    private List<ChannelTwoItem> channelTwoItems = new ArrayList<>();

    private Disposable focusDisposable;


    public ChannelPopWindow(Context context, ItemClickListener itemClickListener){
        resources = context.getResources();
        this.mContext = context;
        this.itemClickListener = itemClickListener;
        init();
    }

    public void init(){
        LayoutInflater mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        container = mLayoutInflater.inflate(R.layout.channel_popwindow, null);

        channel_one_recyclerview =  (RecyclerView) container.findViewById(R.id.channel_one_recyclerview);
        channel_two_recyclerview =  (RecyclerView) container.findViewById(R.id.channel_two_recyclerview);

        initRecyclerView();
        initChannelOneData();
        updatechannelTwoData(1);


        this.setContentView(container);

        int width = 500;
        int height = ScreenUtils.getScreenHeight(mContext);
        this.setWidth(width);
        this.setHeight(height);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.channel_popu_in_out_style);
        this.setOutsideTouchable(false);
        this.setBackgroundDrawable(new BitmapDrawable());
        this.update();

    }


    public void resetFocus(){
        Utils.print(TAG,"selectItem="+selectItem);
        if(focusDisposable!=null && focusDisposable.isDisposed()) focusDisposable.dispose();
        focusDisposable = Observable.timer(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if(channel_two_recyclerview.getChildAt(selectItem)!=null){
                        Utils.print(TAG,"request focus");
                        channel_two_recyclerview.getChildAt(selectItem).requestFocus();
                    }else {
                        Utils.print(TAG,"null");
                    }
                });

    }

    private void initRecyclerView(){
        channel_one_recyclerview.setHasFixedSize(true);
        channel_two_recyclerview.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(mContext);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(mContext);
        channel_one_recyclerview.setLayoutManager(layoutManager1);
        channel_two_recyclerview.setLayoutManager(layoutManager2);
    }


    private void initChannelOneData(){
        List<ChannelOneItem> datas = new ArrayList<>();
        ChannelOneItem item1 = new ChannelOneItem();
        item1.setName("常用频道");
        item1.setPosition(1);
        datas.add(item1);

        ChannelOneItem item2 = new ChannelOneItem();
        item2.setName("央视节目");
        item2.setPosition(2);
        datas.add(item2);

        ChannelOneItem item3 = new ChannelOneItem();
        item3.setName("卫视节目");
        item3.setPosition(3);
        datas.add(item3);

        Utils.print(TAG,"size=="+datas.size());
        //初始化适配器
        channelOneAdapter = new ChannelOneListAdapter(R.layout.channel_one_item,datas);
        channel_one_recyclerview.setAdapter(channelOneAdapter);

        channelOneAdapter.setItemClickListener(this);
    }


    public ChannelTwoItem getDefaultPlayItem(){
        ChannelTwoItem item1 = new ChannelTwoItem();
        item1.setName("1#节目1");
        item1.setUrl(testIP+"/1080.mp4");
        return item1;
    }

    private void initchannelTwoNull(){
        channelTwoItems.clear();

        Utils.print(TAG,"size=="+channelTwoItems.size());
        //初始化适配器
        channelTwoAdapter = new ChannelTwoListAdapter(R.layout.live_two_item,channelTwoItems);
        channel_two_recyclerview.setAdapter(channelTwoAdapter);
        channelTwoAdapter.setItemClickListener(this);
    }


    private void updatechannelTwoData(int parent){
        channelTwoItems.clear();

        ChannelTwoItem item1 = new ChannelTwoItem();
        item1.setName(parent+"#节目1");
        item1.setUrl(testIP+"/1080.mp4");
        channelTwoItems.add(item1);

        ChannelTwoItem item2 = new ChannelTwoItem();
        item2.setName(parent+"#节目2");
        item2.setUrl(testIP+"/one.mp4");
        channelTwoItems.add(item2);

        ChannelTwoItem item3 = new ChannelTwoItem();
        item3.setName(parent+"#节目3");
        item3.setUrl(testIP+"/test.mp4");
        channelTwoItems.add(item3);

        ChannelTwoItem item4 = new ChannelTwoItem();
        item4.setName(parent+"#节目4");
        item4.setUrl("");
        channelTwoItems.add(item4);

        Utils.print(TAG,"size=="+channelTwoItems.size());
        //初始化适配器
        channelTwoAdapter = new ChannelTwoListAdapter(R.layout.channel_two_item,channelTwoItems);
        channel_two_recyclerview.setAdapter(channelTwoAdapter);
        channelTwoAdapter.setItemClickListener(this);
    }




    private int getSelectItem(ChannelTwoItem item){
        int pos = 0;
        for (int i = 0; i < channelTwoItems.size(); i++) {
            if(item==channelTwoItems.get(i)){
                pos = i;
                break;
            }
        }
        return pos;
    }



    /***
     *
     * @Title: setAddressWindow
     * @author:yuanhui
     * @Description: TODO 弹框显示在界面中间
     */
    public void showWindow() {
        this.showAtLocation(container, Gravity.LEFT, 0, 0);
    }




    @Override
    public void channelOneItemClick(ChannelOneItem item) {

    }

    @Override
    public void channelTwoItemClick(ChannelTwoItem item) {
        Utils.print(TAG,"switch chanel");
        selectItem = getSelectItem(item);
        itemClickListener.onItemClickListener(item);
    }

    @Override
    public void channelOneItemSelected(ChannelOneItem item) {
        Utils.print(TAG,"channelOneItemSelected");
        selectItem=0;
        updatechannelTwoData(item.getPosition());

    }
}
