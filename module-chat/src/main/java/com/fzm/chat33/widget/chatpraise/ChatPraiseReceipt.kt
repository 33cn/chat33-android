package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import android.text.TextUtils
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatMessage
import kotlinx.android.synthetic.main.chat_row_receive_receipt.view.*

/**
 * @author zhengjy
 * @since 2019/12/20
 * Description:
 */
class ChatPraiseReceipt(activity: Activity) : ChatPraiseBase(activity) {

    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_receipt
    }

    override fun initView() {
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        if (message.msg != null) {
            contentView.tv_amount.text = message.msg.amount + message.msg.coinName
            if (TextUtils.isEmpty(message.msg.recordId)) {
                if (message.isSentType) {
                    contentView.tv_request_tips.setText(R.string.chat_receipt_to_other)
                } else {
                    contentView.tv_request_tips.setText(R.string.chat_receipt_to_me)
                }
                contentView.chat_message_layout.setBackgroundResource(R.drawable.bg_chat_receipt)
                contentView.iv_status.setImageResource(R.mipmap.icon_chat_receipt)
            } else {
                contentView.tv_request_tips.setText(R.string.chat_receipt_payment)
                contentView.chat_message_layout.setBackgroundResource(R.drawable.bg_chat_receipt_finished)
                contentView.iv_status.setImageResource(R.mipmap.icon_chat_receipt_finished)
            }
        }
    }
}