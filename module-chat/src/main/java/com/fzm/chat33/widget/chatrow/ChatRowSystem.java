package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fzm.chat33.R;
import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.db.bean.ChatMessage;

public class ChatRowSystem extends ChatRowBase {

    TextView tvMessage;
    View ll_system_container;

    public ChatRowSystem(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }


    @Override
    int getLayoutId() {
        return R.layout.chat_row_system ;
    }

    @Override
    void onFindViewById() {
        tvMessage = rootView.findViewById(R.id.tv_message);
        ll_system_container = rootView.findViewById(R.id.ll_system_container);
    }

    @Override
    protected void setUpBaseView() {
        if (position == 0) {
            tvMessageTime.setText(ToolUtils.timeFormat(message.sendTime));
            tvMessageTime.setVisibility(View.VISIBLE);
        } else {
            //显示时间,如果与上一条间隔时间小于10分钟
            ChatMessage preChat = mAdapter.getItem(position - 1);
            if (preChat != null && message.sendTime - preChat.sendTime > 60 * 10 * 1000) {
                tvMessageTime.setText(ToolUtils.timeFormat(message.sendTime));
                tvMessageTime.setVisibility(View.VISIBLE);
            } else {
                tvMessageTime.setVisibility(View.GONE);
            }
        }
        rootView.setSelectable(selectable);
        if (selectable) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cbSelect != null) {
                        cbSelect.performClick();
                    }
                }
            });
        } else {
            rootView.setOnClickListener(null);
            ll_system_container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleLongClick(ll_system_container, message);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    void onSetUpView() {
        if(message.msg !=null){
            tvMessage.setText(message.msg.content);
        }
    }
}
