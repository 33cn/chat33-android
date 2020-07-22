package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatMessage
import kotlinx.android.synthetic.main.chat_message_praise_invitation.view.*

/**
 * 创建日期：2019/11/28
 * 描述:
 * 作者:yll
 */
class ChatPraiseInvitation(activity: Activity) : ChatPraiseBase(activity) {

    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_invitation
    }

    override fun initView() {
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        if (TextUtils.isEmpty(message.msg.avatar)) {
            contentView.iv_group_avatar.setImageResource(R.mipmap.default_avatar_room)
        } else {
            Glide.with(activity).load(message.msg.avatar)
                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                    .into(contentView.iv_group_avatar)
        }
        contentView.iv_group_avatar.setIconRes(if (!TextUtils.isEmpty(message.msg.identificationInfo)) R.drawable.ic_group_identified else -1)
        contentView.tv_group_name.text = message.msg.roomName
        if (TextUtils.isEmpty(message.msg.identificationInfo)) {
            contentView.tv_group_info.visibility = View.GONE
        } else {
            contentView.tv_group_info.text = message.msg.identificationInfo
            contentView.tv_group_info.visibility = View.VISIBLE
        }
    }
}