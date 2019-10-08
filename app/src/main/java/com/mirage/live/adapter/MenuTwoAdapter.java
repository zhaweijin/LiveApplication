package com.mirage.live.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mirage.live.R;
import com.mirage.live.base.OnMenuItemClick;
import com.mirage.live.model.MenuTwoItem;

import java.util.List;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class MenuTwoAdapter extends BaseQuickAdapter<MenuTwoItem, BaseViewHolder> {
    private static final String TAG = "MenuTwoAdapter";

    private OnMenuItemClick onMenuItemClick;

    public void setItemClickListener(OnMenuItemClick itemClickListener) {
        this.onMenuItemClick = itemClickListener;
    }

    public MenuTwoAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final MenuTwoItem item) {
        helper.setText(R.id.name, item.getName());
        helper.getView(R.id.layout_item).setOnClickListener(v -> onMenuItemClick.menuTwoItemClick(item));
    }


}
