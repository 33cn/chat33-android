package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.fzm.chat33.core.db.bean.ChatMessage

/**
 * 创建日期：2019/11/28
 * 描述:
 * 作者:yll
 */
abstract class ChatPraiseBase(val activity: Activity){

    protected var message: ChatMessage? = null
    val contentView: View

    init {
        contentView = LayoutInflater.from(activity).inflate(getLayoutId(), null)
        initView()
    }

    @androidx.annotation.LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun initView()

    open fun bindData(message: ChatMessage) {
        this.message = message
    }

    open fun onPause() {
    }
}