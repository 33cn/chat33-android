package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatMessage
import com.qmuiteam.qmui.widget.textview.QMUILinkTextView
import kotlinx.android.synthetic.main.chat_message_praise_text.view.*

/**
 * 创建日期：2019/11/28
 * 描述:
 * 作者:yll
 */
class ChatPraiseText(activity: Activity) : ChatPraiseBase(activity) {

    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_text
    }

    override fun initView() {
        contentView.tv_message.isNeedExpandFun = true
        contentView.tv_message.setNeedForceEventToParent(true)
        contentView.tv_message.setOnLinkClickListener(object : QMUILinkTextView.OnLinkClickListener {
            override fun onTelLinkClick(phoneNumber: String) {
                val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val mClipData = ClipData.newPlainText("Label", phoneNumber)
                if (cm != null) {
                    cm.setPrimaryClip(mClipData)
                    ShowUtils.showToastNormal(activity, R.string.chat_copyed_message)
                }
            }

            override fun onMailLinkClick(mailAddress: String) {
                val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val mClipData = ClipData.newPlainText("Label", mailAddress)
                if (cm != null) {
                    cm.setPrimaryClip(mClipData)
                    ShowUtils.showToastNormal(activity, R.string.chat_copyed_message)
                }
            }

            override fun onWebUrlLinkClick(url: String) {
                ARouter.getInstance().build(AppRoute.WEB_BROWSER)
                        .withString("url", url)
                        .navigation()
                contentView.tv_message.scrollY = 0
            }
        })
        contentView.setOnClickListener {
            contentView.tv_message.expandText()
        }
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        contentView.tv_message.text = message.msg.content
        contentView.iv_lock.visibility = if(message.isSnap == 1 && (message.isSentType || message.snapVisible == 0)) View.VISIBLE else View.GONE
        contentView.rl_chat.setBackgroundResource(if(message.isSentType) R.drawable.chat_send_selector else R.drawable.chat_receive_selector)
    }
}