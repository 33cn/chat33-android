package com.fzm.chat33.widget.forward;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.fuzamei.common.utils.ShowUtils;
import com.fzm.chat33.main.adapter.ForwardListAdapter;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.ChatMessage;

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:
 */
public class ForwardRowText extends ForwardRowBase {

    private TextView tvMessage;

    public ForwardRowText(Context context, View root, ForwardListAdapter adapter) {
        super(context, root, adapter);
    }

    @Override
    protected void onFindViewById() {
        tvMessage = rootView.findViewById(R.id.tv_message);
    }

    @Override
    protected void onSetUpView() {
        tvMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", chatLog.msg.content);
                // 将ClipData内容放到系统剪贴板里。
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                    ShowUtils.showToastNormal(mContext, R.string.chat_tips_chat_operate4);
                }
                return true;
            }
        });
        if (chatLog.msgType == ChatMessage.Type.SYSTEM
                || chatLog.msgType == ChatMessage.Type.TEXT) {
            tvMessage.setText(chatLog.msg.content);
        } else if (chatLog.msgType == ChatMessage.Type.AUDIO) {
            tvMessage.setText(R.string.core_msg_type2);
        } else if (chatLog.msgType == ChatMessage.Type.RED_PACKET) {
            tvMessage.setText(R.string.core_msg_type4);
        } else if (chatLog.msgType == ChatMessage.Type.FORWARD) {
            tvMessage.setText(R.string.core_msg_type7);
        } else if (chatLog.msgType == ChatMessage.Type.TRANSFER) {
            tvMessage.setText(R.string.core_msg_type8);
        } else if (chatLog.msgType == ChatMessage.Type.RECEIPT) {
            tvMessage.setText(R.string.core_msg_type9);
        } else if (chatLog.msgType == ChatMessage.Type.INVITATION) {
            tvMessage.setText(R.string.core_msg_type15);
        }
    }
}
