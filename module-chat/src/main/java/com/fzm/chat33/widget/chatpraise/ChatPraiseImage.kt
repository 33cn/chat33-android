package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.Transition
import com.fuzamei.common.callback.GlideTarget
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.common.widget.RoundRectImageView
import com.fuzamei.componentservice.ext.dp2px
import com.fzm.chat33.R
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.hepler.FileDownloadManager
import com.fzm.chat33.main.activity.ChatActivity
import com.fzm.chat33.main.activity.LargePhotoActivity
import kotlinx.android.synthetic.main.chat_message_praise_image.view.*
import java.io.File

/**
 * 创建日期：2019/11/28
 * 描述:
 * 作者:yll
 */
class ChatPraiseImage(activity: Activity) : ChatPraiseBase(activity) {

    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_image
    }

    override fun initView() {
        contentView.setOnClickListener {
            val intent = Intent(activity, LargePhotoActivity::class.java)
            intent.putExtra(LargePhotoActivity.CHAT_MESSAGE, message)
            activity.startActivityForResult(intent, ChatActivity.REQUEST_DEAD_TIME,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity, contentView, "shareImage").toBundle())
        }
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        if (message.msg != null && !(!message.isSentType && message.isSnap == 1)) {
            val imageUrl = message.msg.localPath
            if (message.msg.height > 0 && message.msg.width > 0) {
                val result = ToolUtils.getChatImageHeightWidth(activity, message.msg.height, message.msg.width)
                val height = result[0]
                val width = result[1]
                setViewParams(height, width, contentView.iv_image)
            } else {
                setViewParams(activity.dp2px(150f), activity.dp2px(150f), contentView.iv_image)
            }
            contentView.iv_image.setImageResource(R.drawable.bg_image_placeholder)
            if (TextUtils.isEmpty(imageUrl) || !File(imageUrl).exists()) {
                doDownloadWork()
            } else {
                setupChatImage(imageUrl)
            }
        }
        contentView.iv_lock.visibility = if (message.isSnap == 1 && (message.isSentType || message.snapVisible == 0)) View.VISIBLE else View.GONE
    }

    private fun setupChatImage(imageUrl: String) {
        if (imageUrl.endsWith("gif")) {
            displayGif()
        } else {
            displayImage()
        }
    }

    private fun doDownloadWork() {
        val folder = File(activity.filesDir.path + "/picture")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        message?.msg?.downloading = true
        FileDownloadManager.INS.download(folder, message, object : FileDownloadManager.DownloadCallback {
            override fun onStart() {

            }

            override fun onAlreadyRunning() {

            }

            override fun onProgress(progress: Float) {

            }

            override fun onFinish(file: File?, throwable: Throwable?) {
                message?.msg?.downloading = false
                if (file != null) {
                    message?.msg?.localPath = file.absolutePath
                    setupChatImage(file.absolutePath)
                } else {
                    message?.msg?.localPath = null
                }
            }
        })
    }

    private fun displayGif() {
        Glide.with(activity).asGif().load(message).apply(mImageOptions).into(contentView.iv_image)
    }

    private fun displayImage() {
        Glide.with(activity).load(message).apply(mImageOptions)
                .into(GlideTarget(contentView.iv_image, R.id.iv_image, message?.logId!!, object : GlideTarget.Callback {
                    override fun onResourceLoading(view: ImageView, placeholder: Drawable?) {
                        view.setImageResource(R.drawable.bg_image_placeholder)
                    }

                    override fun onResourceReady(view: ImageView, resource: Drawable, transition: Transition<in Drawable>?) {
                        view.setImageDrawable(resource)
                    }
                }))
    }

    private fun setViewParams(height: Int, width: Int, view: View) {
        val params = view.layoutParams
        params.height = height
        params.width = width
        view.layoutParams = params
    }

    private val mImageOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.bg_image_placeholder)
            .centerCrop()
}