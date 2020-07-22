package com.fuzamei.common.recycleviewbase.helper;

import android.view.View;

/**
 * @author zhengjy
 * @since 2018/11/29
 * Description:
 */
public interface ItemTouchListener {

    void onItemClick(View view, int position);

    void onRightMenuClick(View view, int position);
}
