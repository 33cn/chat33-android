package com.fzm.chat33.widget.forward;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.common.utils.ScreenUtils;
import com.fzm.chat33.R;
import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.core.db.bean.BriefChatLog;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.utils.StringUtils;
import com.fzm.chat33.main.adapter.ForwardListAdapter;

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:转发消息列表ViewHolder基类
 */
public abstract class ForwardRowBase extends RecyclerView.ViewHolder {

    protected View rootView;
    protected ChatMessage message;
    protected BriefChatLog chatLog;
    protected ForwardListAdapter mAdapter;
    protected int position;
    protected Context mContext;

    protected TextView tvMessageTime;
    protected ImageView ivUserHead;
    protected TextView tvUserName;

    public ForwardRowBase(Context context, View root, ForwardListAdapter adapter) {
        super(root);
        this.mContext = context;
        this.rootView = root;
        this.mAdapter = adapter;

        initView();
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    private void initView() {
        tvMessageTime = rootView.findViewById(R.id.tv_message_time);
        ivUserHead = rootView.findViewById(R.id.iv_user_head);
        tvUserName = rootView.findViewById(R.id.tv_user_name);

        onFindViewById();
    }

    public View getRootView() {
        return rootView;
    }

    /**
     * 不同的ViewHolder有自己独立的控件
     */
    protected abstract void onFindViewById();

    /**
     * 不同ViewHolder有自己的逻辑
     */
    protected abstract void onSetUpView();

    public void setView(BriefChatLog chatLog, int position) {
        this.chatLog = chatLog;
        this.position = position;
        setUpBaseView();
        onSetUpView();
    }

    protected void setUpBaseView() {
        if (chatLog.showTime) {
            tvMessageTime.setText(ToolUtils.timeFormat(chatLog.datetime));
            tvMessageTime.setVisibility(View.VISIBLE);
        } else {
            tvMessageTime.setVisibility(View.GONE);
        }
        tvUserName.setVisibility(View.VISIBLE);
        tvUserName.setTextColor(ContextCompat.getColor(mContext, R.color.chat_text_grey_light));

        final RequestOptions options = new RequestOptions().placeholder(R.mipmap.default_avatar_round);
        Glide.with(mContext).load(StringUtils.aliyunFormat(chatLog.senderInfo.avatar, ScreenUtils.dp2px(mContext, 35)
                , ScreenUtils.dp2px(mContext, 35)))
                .apply(options)
                .into(ivUserHead);
        tvUserName.setText(chatLog.senderInfo.nickname);
    }
}
