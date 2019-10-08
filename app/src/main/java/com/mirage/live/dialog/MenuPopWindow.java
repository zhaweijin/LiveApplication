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
import com.mirage.live.R;
import com.mirage.live.adapter.MenuOneAdapter;
import com.mirage.live.adapter.MenuTwoAdapter;
import com.mirage.live.base.OnMenuItemClick;
import com.mirage.live.constan.Constans;
import com.mirage.live.model.MenuOneItem;
import com.mirage.live.model.MenuTwoItem;
import com.mirage.live.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MenuPopWindow extends PopupWindow implements OnMenuItemClick {

    private static final String TAG = "MenuPopWindow";

    private Context mContext;

    /*弹框布局*/
    private View container;

    /*回调接口*/

    private RecyclerView menu_one_recyclerview;
    private RecyclerView menu_two_recyclerview;
    private Resources resources;


    private MenuOneAdapter menuOneAdapter;
    private MenuTwoAdapter menuTwoAdapter;

    private int oneSelectType=Constans.USER_CENTER;

    public MenuPopWindow(Context context){
        resources = context.getResources();
        this.mContext = context;
        init();
    }

    public void init(){
        LayoutInflater mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        container = mLayoutInflater.inflate(R.layout.menu_popwindow, null);

        menu_one_recyclerview =  (RecyclerView) container.findViewById(R.id.menu_one_recyclerview);
        menu_two_recyclerview =  (RecyclerView) container.findViewById(R.id.menu_two_recyclerview);

        initRecyclerView();
        initMenuOneData();

        this.setContentView(container);

        int width = 400;
        int height = ScreenUtils.getScreenHeight(mContext);
        this.setWidth(width);
        this.setHeight(height);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.menu_popu_in_out_style);
        this.setOutsideTouchable(false);
        this.setBackgroundDrawable(new BitmapDrawable());
        this.update();
    }


    private void initRecyclerView(){
        menu_one_recyclerview.setHasFixedSize(true);
        menu_two_recyclerview.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(mContext);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(mContext);
        menu_one_recyclerview.setLayoutManager(layoutManager1);
        menu_two_recyclerview.setLayoutManager(layoutManager2);
    }


    private void initMenuOneData(){
        List<MenuOneItem> datas = new ArrayList<>();
        MenuOneItem item1 = new MenuOneItem();
        item1.setName("个人中心");
        item1.setType(Constans.USER_CENTER);
        datas.add(item1);

        MenuOneItem item2 = new MenuOneItem();
        item2.setName("画面比例");
        item2.setType(Constans.SHOW_MODE);
        datas.add(item2);

        MenuOneItem item3 = new MenuOneItem();
        item3.setName("源");
        item3.setType(Constans.OUTPUT_SOURCE);
        datas.add(item3);

        Utils.print(TAG,"size=="+datas.size());
        //初始化适配器
        menuOneAdapter = new MenuOneAdapter(R.layout.live_two_item,datas);
        menu_one_recyclerview.setAdapter(menuOneAdapter);

        menuOneAdapter.setItemClickListener(this);
    }


    private void initMenuTwoNull(){
        List<MenuTwoItem> datas = new ArrayList<>();

        Utils.print(TAG,"size=="+datas.size());
        //初始化适配器
        menuTwoAdapter = new MenuTwoAdapter(R.layout.live_two_item,datas);
        menu_two_recyclerview.setAdapter(menuTwoAdapter);
        menuTwoAdapter.setItemClickListener(this);
    }


    private void updateMenuTwoData(int type){
        switch (type){
            case Constans.USER_CENTER:
                initMenuTwoNull();
                break;
            case Constans.SHOW_MODE:
                initTwoShowMode();
                break;
            case Constans.OUTPUT_SOURCE:
                initTwoOutputSource();
                break;

        }
    }



    private void initTwoShowMode(){
        List<MenuTwoItem> datas = new ArrayList<>();

        MenuTwoItem item1 = new MenuTwoItem();
        item1.setName("全屏");
        item1.setType(Constans.MODE_FULLSCREEN);
        datas.add(item1);

        MenuTwoItem item2 = new MenuTwoItem();
        item2.setName("4：3");
        item2.setType(Constans.MODE_4TO3);
        datas.add(item2);

        MenuTwoItem item3 = new MenuTwoItem();
        item3.setName("16：9");
        item3.setType(Constans.MODE_16TO9);
        datas.add(item3);

        MenuTwoItem item4 = new MenuTwoItem();
        item4.setName("原始大小");
        item4.setType(Constans.MODE_DEFUALT);
        datas.add(item4);

        Utils.print(TAG,"size=="+datas.size());
        //初始化适配器
        menuTwoAdapter = new MenuTwoAdapter(R.layout.live_two_item,datas);
        menu_two_recyclerview.setAdapter(menuTwoAdapter);
        menuTwoAdapter.setItemClickListener(this);
    }


    private void initTwoOutputSource(){
        List<MenuTwoItem> datas = new ArrayList<>();

        MenuTwoItem item1 = new MenuTwoItem();
        item1.setName("源1");
        item1.setType(Constans.MODE_FULLSCREEN);
        datas.add(item1);

        MenuTwoItem item2 = new MenuTwoItem();
        item2.setName("源2");
        item2.setType(Constans.MODE_4TO3);
        datas.add(item2);

        Utils.print(TAG,"size=="+datas.size());
        //初始化适配器
        menuTwoAdapter = new MenuTwoAdapter(R.layout.live_two_item,datas);
        menu_two_recyclerview.setAdapter(menuTwoAdapter);
        menuTwoAdapter.setItemClickListener(this);
    }


    /***
     *
     * @Title: setAddressWindow
     * @author:yuanhui
     * @Description: TODO 弹框显示在界面中间
     */
    public void showWindow() {
        this.showAtLocation(container, Gravity.RIGHT, 0, 0);
    }


    @Override
    public void menuOneItemClick(MenuOneItem item) {

    }

    @Override
    public void menuTwoItemClick(MenuTwoItem item) {
        switch (oneSelectType){
            case Constans.SHOW_MODE:
                clickShowModeItem(item);
                break;
            case Constans.OUTPUT_SOURCE:
                clickOutputSource(item);
                break;
        }
    }


    private void clickShowModeItem(MenuTwoItem item){
        Utils.print(TAG,"click mode ==="+item.getName());
         switch (item.getType()) {
             case Constans.MODE_4TO3:
                 break;
             case Constans.MODE_16TO9:
                 break;
             case Constans.MODE_DEFUALT:
                 break;
             case Constans.MODE_FULLSCREEN:
                 break;
             default:
                 break;
         }
    }

    private void clickOutputSource(MenuTwoItem item){
         Utils.print(TAG,"set source---"+item.getName());
    }

    @Override
    public void menuOneItemSelected(MenuOneItem item) {
        switch (item.getType()){
            case Constans.USER_CENTER:
                oneSelectType = Constans.USER_CENTER;
                break;
            case Constans.SHOW_MODE:
                oneSelectType = Constans.SHOW_MODE;
                break;
            case Constans.OUTPUT_SOURCE:
                oneSelectType = Constans.OUTPUT_SOURCE;
                break;
        }
        updateMenuTwoData(oneSelectType);
    }



}
