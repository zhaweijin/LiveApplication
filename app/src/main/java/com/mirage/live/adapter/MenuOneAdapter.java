package com.mirage.live.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mirage.live.R;
import com.mirage.live.base.OnMenuItemClick;
import com.mirage.live.model.MenuOneItem;
import java.util.List;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class MenuOneAdapter extends BaseQuickAdapter<MenuOneItem, BaseViewHolder> {
    private static final String TAG = "MenuOneAdapter";

    private OnMenuItemClick onMenuItemClick;
    public void setItemClickListener(OnMenuItemClick itemClickListener) {
        this.onMenuItemClick = itemClickListener;
    }

    public MenuOneAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final MenuOneItem item) {
        helper.setText(R.id.name, item.getName());
        helper.getView(R.id.layout_item).setOnClickListener(v -> onMenuItemClick.menuOneItemClick(item));

        helper.getView(R.id.layout_item).setOnFocusChangeListener((v, hasFocus) -> {
            onMenuItemClick.menuOneItemSelected(item);
        });
    }


}
