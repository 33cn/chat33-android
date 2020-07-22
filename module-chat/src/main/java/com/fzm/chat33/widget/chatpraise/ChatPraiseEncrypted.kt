package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatMessage
import kotlinx.android.synthetic.main.chat_message_praise_encrypted.view.*

/**
 * 创建日期：2019/11/28
 * 描述:
 * 作者:yll
 */
class ChatPraiseEncrypted(activity: Activity) : ChatPraiseBase(activity) {

    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_encrypted
    }

    override fun initView() {
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        contentView.tv_message.setText(R.string.chat_encrypted_message)
        contentView.setBackgroundResource(if(message.isSentType) R.drawable.chat_send_selector else R.drawable.chat_receive_selector)
    }
}