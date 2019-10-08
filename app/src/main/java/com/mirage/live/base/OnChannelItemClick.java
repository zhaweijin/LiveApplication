package com.mirage.live.base;

import com.mirage.live.model.ChannelOneItem;
import com.mirage.live.model.ChannelTwoItem;

public interface OnChannelItemClick {
    void channelOneItemClick(ChannelOneItem item);
    void channelTwoItemClick(ChannelTwoItem item);
    void channelOneItemSelected(ChannelOneItem item);
}