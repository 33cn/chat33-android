package com.fzm.chat33.main.adapter;

import android.content.Context;
import android.view.View;

import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.common.recycleviewbase.helper.ItemTouchListener;
import com.fuzamei.common.recycleviewbase.helper.SwipeItemLayout;
import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.GroupNotice;

import java.util.List;

/**
 * @author zhengjy
 * @since 2018/11/29
 * Description:
 */
public class GroupNoticeListAdapter extends CommonAdapter<GroupNotice> {

    private ItemTouchListener listener;
    private boolean canSwipe;

    public GroupNoticeListAdapter(Context context, int layoutId, List<GroupNotice> datas) {
        super(context, layoutId, datas);
    }

    public void setItemTouchListener(ItemTouchListener listener) {
        this.listener = listener;
    }

    public void setCanSwipe(boolean canSwipe) {
        this.canSwipe = canSwipe;
    }

    @Override
    protected void convert(ViewHolder holder, GroupNotice groupNotice, final int position) {
        holder.setText(R.id.tv_notice_author, groupNotice.getSenderName());
        holder.setText(R.id.tv_notice_time, ToolUtils.timeFormat(groupNotice.getDatetime()));
        holder.setText(R.id.tv_notice_content, groupNotice.getContent());
        final SwipeItemLayout swipeItemLayout = (SwipeItemLayout) holder.itemView;
        swipeItemLayout.setSwipeEnable(canSwipe);
        holder.setOnClickListener(R.id.tv_delete_notice, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRightMenuClick(v, mDatas.indexOf(groupNotice));
                    swipeItemLayout.close();
                }
            }
        });
        holder.setOnClickListener(R.id.rv_item, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, mDatas.indexOf(groupNotice));
                }
            }
        });
    }
}
