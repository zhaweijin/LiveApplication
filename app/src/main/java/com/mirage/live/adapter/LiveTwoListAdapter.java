package com.mirage.live.adapter;




import android.view.View;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mirage.live.R;
import com.mirage.live.base.ItemClickListener;
import com.mirage.live.model.LiveListItem;
import com.mirage.live.utils.Utils;
import java.util.List;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class LiveTwoListAdapter extends BaseQuickAdapter<LiveListItem, BaseViewHolder> {
    private static final String TAG = "liveadapter";

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public LiveTwoListAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final LiveListItem item) {
        helper.setText(R.id.name, item.getName());
        helper.getView(R.id.layout_item).setOnClickListener(v -> {
            Utils.print(TAG,"item click....."+item.getName());
            itemClickListener.onItemClickListener(item);
        });

        helper.getView(R.id.layout_item).setOnFocusChangeListener((v, hasFocus) -> {

        });
    }


}
