package com.mirage.live.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mirage.live.R;
import com.mirage.live.base.ItemClickListener;
import com.mirage.live.model.FirstShowPreviewItem;
import com.mirage.live.utils.Utils;

import java.util.List;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class FirstShowPreviewAdapter extends BaseQuickAdapter<FirstShowPreviewItem, BaseViewHolder> {
    private static final String TAG = "ChannelOneListAdapter";

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FirstShowPreviewAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final FirstShowPreviewItem item) {
        helper.getView(R.id.layout_first_show_item).setOnClickListener(v -> {
            Utils.print(TAG,"item click....."+item.getName());
            itemClickListener.onItemClickListener(item);
        });

        helper.getView(R.id.layout_first_show_item).setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) itemClickListener.onItemSelectedListener(item);
        });

        helper.getView(R.id.preview_icon).setBackgroundResource(item.getResourceID());
    }


}
