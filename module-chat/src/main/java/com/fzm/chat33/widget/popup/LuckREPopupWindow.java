package com.fzm.chat33.widget.popup;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.common.utils.ScreenUtils;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.utils.StringUtils;


public class LuckREPopupWindow extends BasePopupWindow implements OnClickListener {

    private ImageView open;
    private ChatMessage message;
    private TextView tv_uid;
    private TextView remark;
    private TextView coin_type;
    private FrameLayout root;
    private ImageView ivHead;

    public LuckREPopupWindow(Context context, View popupView, ChatMessage message) {
        super(context, popupView);
        this.message = message;
        findView();
        initData();
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
        if (remark != null) {
            initData();
        }
    }

    private void findView() {
        ivHead = mRootView.findViewById(R.id.iv_head);
        open = mRootView.findViewById(R.id.open);
        tv_uid = mRootView.findViewById(R.id.tv_uid);
        remark = mRootView.findViewById(R.id.remark);
        coin_type = mRootView.findViewById(R.id.coin_type);
        root = mRootView.findViewById(R.id.root);
        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setOnOpenListener(OnClickListener listener) {
        open.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message == null || message.msg == null) {
                    return;
                }
                listener.onClick(v);
            }
        });
    }

    private void initData() {
        if (message == null) {
            return;
        }
        tv_uid.setText(message.senderInfo.nickname);
        if (message.msg != null) {
            if (message.msg.redBagRemark.length() > 20) {
                remark.setText(message.msg.redBagRemark.substring(0, 20) + "…");
            } else {
                remark.setText(message.msg.redBagRemark);
            }
            coin_type.setText(context.getString(R.string.chat_send_coin_message, message.msg.coinName));
        }
        if (!TextUtils.isEmpty(message.senderInfo.avatar)) {
            Glide.with(context).load(StringUtils.aliyunFormat(message.senderInfo.avatar, ScreenUtils.dp2px(context, 70)
                    , ScreenUtils.dp2px(context, 70)))
                    .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_round))
                    .into(ivHead);
        } else {
            ivHead.setImageResource(R.mipmap.default_avatar_round);
        }
    }
}
