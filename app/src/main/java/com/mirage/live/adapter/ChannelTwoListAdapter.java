package com.mirage.live.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mirage.live.R;
import com.mirage.live.base.ItemClickListener;
import com.mirage.live.base.OnChannelItemClick;
import com.mirage.live.model.ChannelTwoItem;
import com.mirage.live.model.LiveListItem;
import com.mirage.live.utils.Utils;

import java.util.List;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class ChannelTwoListAdapter extends BaseQuickAdapter<ChannelTwoItem, BaseViewHolder> {
    private static final String TAG = "ChannelTwoListAdapter";

    private OnChannelItemClick itemClickListener;

    public void setItemClickListener(OnChannelItemClick itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ChannelTwoListAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final ChannelTwoItem item) {
        helper.setText(R.id.name, item.getName());
        helper.getView(R.id.layout_two_item).setOnClickListener(v -> {
            Utils.print(TAG,"item click....."+item.getName());
            itemClickListener.channelTwoItemClick(item);
        });


    }


}
