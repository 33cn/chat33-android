package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatMessage
import kotlinx.android.synthetic.main.chat_message_praise_transfer.view.*

/**
 * @author zhengjy
 * @since 2019/12/20
 * Description:
 */
class ChatPraiseTransfer(activity: Activity) : ChatPraiseBase(activity) {

    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_transfer
    }

    override fun initView() {
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        if (message.msg != null) {
            contentView.tv_amount.text = message.msg.amount + message.msg.coinName
            if (message.isSentType) {
                contentView.tv_transfer_tips.setText(R.string.chat_transfer_to_other)
            } else {
                contentView.tv_transfer_tips.setText(R.string.chat_transfer_to_me)
            }
        }
    }
}