package com.fzm.chat33.widget.forward;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ForwardListAdapter;
import com.fzm.chat33.core.db.bean.BriefChatLog;

public class ForwardRowUnsupported extends ForwardRowBase {

    TextView tvMessage;

    public ForwardRowUnsupported(Context context, View root, ForwardListAdapter adapter) {
        super(context, root, adapter);
    }

    @Override
    protected void onFindViewById() {
        tvMessage = rootView.findViewById(R.id.tv_message);
    }

    @Override
    protected void setUpBaseView() {
        if (position == 0) {
            tvMessageTime.setText(ToolUtils.timeFormat(chatLog.datetime));
            tvMessageTime.setVisibility(View.VISIBLE);
        } else {
            //显示时间,如果与上一条间隔时间小于10分钟
            BriefChatLog preChat = mAdapter.getItem(position - 1);
            if (preChat != null && chatLog.datetime - preChat.datetime > 10 * 60 * 1000) {
                tvMessageTime.setText(ToolUtils.timeFormat(chatLog.datetime));
                tvMessageTime.setVisibility(View.VISIBLE);
            } else {
                tvMessageTime.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onSetUpView() {
        tvMessage.setText(R.string.chat_error_unsupport_message_type);
    }
}
