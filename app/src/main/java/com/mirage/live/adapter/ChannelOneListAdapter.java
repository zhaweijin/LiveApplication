package com.mirage.live.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mirage.live.R;
import com.mirage.live.base.ItemClickListener;
import com.mirage.live.base.OnChannelItemClick;
import com.mirage.live.model.ChannelOneItem;
import com.mirage.live.model.LiveListItem;
import com.mirage.live.utils.Utils;

import java.util.List;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class ChannelOneListAdapter extends BaseQuickAdapter<ChannelOneItem, BaseViewHolder> {
    private static final String TAG = "ChannelOneListAdapter";

    private OnChannelItemClick itemClickListener;

    public void setItemClickListener(OnChannelItemClick itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ChannelOneListAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final ChannelOneItem item) {
        helper.setText(R.id.name, item.getName());
        helper.getView(R.id.layout_one_item).setOnClickListener(v -> {
            Utils.print(TAG,"item click....."+item.getName());
            itemClickListener.channelOneItemClick(item);
        });

        helper.getView(R.id.layout_one_item).setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) itemClickListener.channelOneItemSelected(item);
        });
    }


}
