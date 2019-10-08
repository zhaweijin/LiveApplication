package com.mirage.live.base;

import com.mirage.live.model.MenuOneItem;
import com.mirage.live.model.MenuTwoItem;

public interface OnMenuItemClick{
    void menuOneItemClick(MenuOneItem item);
    void menuTwoItemClick(MenuTwoItem item);
    void menuOneItemSelected(MenuOneItem item);
}