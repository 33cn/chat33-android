package com.fzm.chat33.widget.chatrow;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.db.bean.ChatMessage;

import static com.fzm.chat33.core.global.Chat33Const.*;

/**
 * 通知消息
 */
public class ChatRowNotification extends ChatRowBase {

    TextView tvMessage;

    public ChatRowNotification(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }

    @Override
    int getLayoutId() {
        return R.layout.chat_row_notification ;
    }

    @Override
    void onFindViewById() {
        tvMessage = rootView.findViewById(R.id.tv_message);
    }

    @Override
    protected void setUpBaseView() {
        if (position == 0) {
            tvMessageTime.setText(ToolUtils.timeFormat(message.sendTime));
            tvMessageTime.setVisibility(View.VISIBLE);
        } else {
            //显示时间,如果与上一条间隔时间小于10分钟
            ChatMessage preChat = (ChatMessage) mAdapter.getItem(position - 1);
            if (preChat != null && message.sendTime - preChat.sendTime > 10 * 60 * 1000) {
                tvMessageTime.setText(ToolUtils.timeFormat(message.sendTime));
                tvMessageTime.setVisibility(View.VISIBLE);
            } else {
                tvMessageTime.setVisibility(View.GONE);
            }
        }
    }

    @Override
    void onSetUpView() {
        if (message.msg == null) {
            return;
        }
        if (TextUtils.isEmpty(message.msg.operator) && TextUtils.isEmpty(message.msg.target) && TextUtils.isEmpty(message.msg.key)) {
            // 兼容旧版本消息的显示
            if (message.msg.content != null) {
                setText();
            } else {
                if (message.msg.type == 16) {
                    if (message.senderId.equals(AppConfig.MY_ID)) {
                        message.msg.content = activity.getString(R.string.core_screenshots_from_me);
                    } else {
                        message.msg.content = activity.getString(R.string.core_screenshots_from_other);
                    }
                    setText();
                }
            }
            return;
        }
        switch (message.msg.type) {
            case REVOKE_MSG:// 撤回消息
            case CREATE_GROUP:// 创建群聊
            case EXIT_GROUP:// 退出群聊
            case KICK_OUT:// 移出群聊
            case JOIN_GROUP:// 加入群聊
            case DISMISS_GROUP:// 解散群聊
            case FRIEND_IN_GROUP:// 群中添加好友
            case DELETE_FRIEND:// 删除好友
            case CHANGE_GROUP_OWNER:// 成为群主
            case CHANGE_GROUP_ADMIN:// 设为管理员
            case CHANGE_GROUP_NAME:// 更改群名
                setText();
                break;
            case RECEIVE_RED_PACKET:// 领取红包
                setText(getClickableText(2, "#DD5F5F", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ARouter.getInstance().build("/app/redPacketInfo")
                                .withString("packetId", message.msg.packetId)
                                .navigation();
                    }
                }));
                break;
            case BECOME_FRIEND:// 群外添加好友
            case MUTE_IN_GROUP:// 群禁言
                setText();
                break;
            case SNAP_DESTROY:// 消息焚毁
                break;
            case SCREEN_SHOT:// 聊天截图
            case INVITE_GROUP:// 邀请加入群聊
                setText();
                break;
            case RECEIPT_SUCCESS:// 收款成功，对方已经付款
                setText(getClickableText(2, AppConfig.APP_ACCENT_COLOR_STR, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ARouter.getInstance().build(AppRoute.DEPOSIT_TX_DETAIL)
                                .withString("recordId", message.msg.recordId)
                                .navigation();
                    }
                }));
                break;
            case GROUP_REJECT_MSG:// 好友拒绝加入群聊
            case FRIEND_REJECT_MSG:// 对方拒收你的消息
                setText();
                break;
        }
    }

    /**
     * 设置通知可点击部分
     *
     * @param clickLength   可点击部分长度（从尾部算起）
     * @param color         可点击部分颜色，eg: #FFFFFF
     * @param listener      点击事件
     * @return style        Spanned对象
     */
    private Spanned getClickableText(int clickLength, String color, View.OnClickListener listener) {
        int length = message.msg.content.length();
        SpannableStringBuilder style = new SpannableStringBuilder(message.msg.content);
        if (length < clickLength) {
            return style;
        }
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                listener.onClick(widget);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);
                ds.setUnderlineText(false);
            }
        };
        style.setSpan(clickableSpan, length - clickLength, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor(color));
        style.setSpan(foregroundColorSpan, length - clickLength, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return style;
    }

    private void setText() {
        setText(Html.fromHtml(message.msg.content));
    }

    private void setText(Spanned spanned) {
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        tvMessage.setText(spanned);
    }
}
