package com.fzm.chat33.widget.chatrow;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.componentservice.app.AppRoute;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.main.mvvm.ChatViewModel;
import com.fzm.chat33.widget.ChatAvatarView;

/**
 * @author zhengjy
 * @since 2019/07/16
 * Description:
 */
public class ChatRowInvitation extends ChatRowBase {

    View bubbleLayout;
    ChatAvatarView iv_group_avatar;
    TextView tv_group_name, tv_group_info, thumb_up;
    private ChatViewModel viewModel;

    public ChatRowInvitation(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
        viewModel = ViewModelProviders.of(activity).get(ChatViewModel.class);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_invitation : R.layout.chat_row_receive_invitation;
    }

    @Override
    void onFindViewById() {
        bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        iv_group_avatar = rootView.findViewById(R.id.iv_group_avatar);
        tv_group_name = rootView.findViewById(R.id.tv_group_name);
        tv_group_info = rootView.findViewById(R.id.tv_group_info);
        thumb_up = rootView.findViewById(R.id.thumb_up);
    }

    @Override
    void onSetUpView() {
        if (TextUtils.isEmpty(message.msg.avatar)) {
            iv_group_avatar.setImageResource(R.mipmap.default_avatar_room);
        } else {
            Glide.with(activity).load(message.msg.avatar)
                    .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_room))
                    .into(iv_group_avatar);
        }
        iv_group_avatar.setIconRes(!TextUtils.isEmpty(message.msg.identificationInfo) ? R.drawable.ic_group_identified : -1);
        tv_group_name.setText(message.msg.roomName);
        if (TextUtils.isEmpty(message.msg.identificationInfo)) {
            tv_group_info.setVisibility(View.GONE);
        } else {
            tv_group_info.setText(message.msg.identificationInfo);
            tv_group_info.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected View chatMainView() {
        return bubbleLayout;
    }

    @Override
    public TextView thumbUpView() {
        return thumb_up;
    }
}
