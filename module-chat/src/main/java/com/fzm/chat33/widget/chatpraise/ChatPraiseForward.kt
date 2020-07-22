package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.comparator.DateComparator
import com.fzm.chat33.core.db.bean.BriefChatLog
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import kotlinx.android.synthetic.main.chat_message_praise_forward.view.*
import java.util.*

/**
 * @author zhengjy
 * @since 2019/12/20
 * Description:
 */
class ChatPraiseForward(activity: Activity) : ChatPraiseBase(activity) {

    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_forward
    }

    override fun initView() {
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        if (message.msg.sourceChannel == Chat33Const.CHANNEL_FRIEND) {
            contentView.tv_message_title.text = activity.getString(R.string.chat_title_forward_list1, message.msg.forwardUserName, message.msg.sourceName)
        } else if (message.msg.sourceChannel == Chat33Const.CHANNEL_ROOM) {
            contentView.tv_message_title.text = activity.getString(R.string.chat_title_forward_list2, message.msg.sourceName)
        }
        val content = StringBuilder()
        Collections.sort(message.msg.sourceLog, DateComparator())
        if (message.msg.sourceLog.size >= 4) {
            for (i in 0..3) {
                setDisplayContent(content, message.msg.sourceLog[i])
                if (i < 3) {
                    content.append("\n")
                }
            }
        } else {
            for (i in message.msg.sourceLog.indices) {
                setDisplayContent(content, message.msg.sourceLog[i])
                if (i < message.msg.sourceLog.size - 1) {
                    content.append("\n")
                }
            }
        }
        contentView.tv_message.text = content.toString()
        contentView.tv_forward_count.text = activity.getString(R.string.forward_count, message.msg.sourceLog.size)
        contentView.chat_message_layout.setBackgroundResource(if(message.isSentType) R.drawable.chat_send_selector else R.drawable.chat_receive_selector)
        val color = activity.resources.getColor(if(message.isSentType) R.color.chat_forward_divider_send else R.color.chat_forward_divider_receive)
        contentView.forward_divider.setBackgroundColor(color)
    }

    private fun setDisplayContent(builder: StringBuilder, chatLog: BriefChatLog) {
        var temp = ""
        if (chatLog.msgType == ChatMessage.Type.SYSTEM || chatLog.msgType == ChatMessage.Type.TEXT) {
            temp = if (chatLog.msg.content == null) activity.getString(R.string.core_msg_type11) else chatLog.msg.content
        } else if (chatLog.msgType == ChatMessage.Type.AUDIO) {
            temp = activity.getString(R.string.core_msg_type2)
        } else if (chatLog.msgType == ChatMessage.Type.IMAGE) {
            temp = activity.getString(R.string.core_msg_type3)
        } else if (chatLog.msgType == ChatMessage.Type.RED_PACKET) {
            var remark: String? = chatLog.msg.redBagRemark
            if (remark == null) {
                remark = ""
            }
            temp = activity.getString(R.string.core_msg_type4) + remark
        } else if (chatLog.msgType == ChatMessage.Type.VIDEO) {
            temp = activity.getString(R.string.core_msg_type5)
        } else if (chatLog.msgType == ChatMessage.Type.FORWARD) {
            temp = activity.getString(R.string.core_msg_type7)
        } else if (chatLog.msgType == ChatMessage.Type.FILE) {
            temp = if (chatLog.msg.fileName == null)
                activity.getString(R.string.core_msg_type12)
            else
                activity.getString(R.string.core_msg_type12) + chatLog.msg.fileName
        }
        builder.append(chatLog.senderInfo.displayName).append(":").append(temp)
    }
}