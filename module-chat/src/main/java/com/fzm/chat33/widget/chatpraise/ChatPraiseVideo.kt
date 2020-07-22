package com.fzm.chat33.widget.chatpraise

import android.Manifest
import android.app.Activity
import android.os.Environment
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.utils.*
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.param.toDecParams
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.manager.FileEncryption
import com.fzm.chat33.core.manager.toByteArray
import com.fzm.chat33.core.manager.toCacheFile
import com.fzm.chat33.utils.FileUtils
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import kotlinx.android.synthetic.main.chat_message_praise_video.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

/**
 * 创建日期：2019/11/28
 * 描述:
 * 作者:yll
 */
class ChatPraiseVideo(activity: Activity) : ChatPraiseBase(activity) {

    companion object {
        const val SAVE_VIDEO = 2
    }
    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_video
    }

    private var downloading = false

    override fun initView() {
        contentView.setOnClickListener {
            downloadOrPlayVideo()
        }
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)

        if (message.msg.height > 0 && message.msg.width > 0) {
            val result = ToolUtils.getChatImageHeightWidth(activity, message.msg.height, message.msg.width)
            val height = result[0]
            val width = result[1]
            setViewParams(height, width, contentView.iv_image)
        } else {
            //获取不到视频尺寸的情况，拿到bitmap后获取宽高，然后载入imageView
            val height = ScreenUtils.dp2px(activity, 150f)
            val width = ScreenUtils.dp2px(activity, 150f)
            setViewParams(height, width, contentView.iv_image)
        }
        contentView.iv_image.setImageResource(R.drawable.bg_image_placeholder)
        if (message.msg.localPath.isNullOrEmpty() || !File(message.msg.localPath).exists()) {
            // 自动下载视频
            doDownloadWork(true)
        } else {
            setupChatVideo()
//            contentView.iv_status.visibility = View.VISIBLE
        }
    }

    private fun setupChatVideo() {
        contentView.iv_image.visibility = View.VISIBLE
        contentView.tv_duration.text = ToolUtils.formatVideoDuration(message!!.msg.duration)
        if (message!!.msg != null && !(!message!!.isSentType && message!!.isSnap == 1)) {
            GlobalScope.launch(Dispatchers.Main) {
                val file = if (AppConfig.FILE_ENCRYPT && message!!.msg.localPath.contains(AppConfig.ENC_PREFIX)) {
                    FileEncryption.decrypt(message!!.toDecParams(), File(message!!.msg.localPath).toByteArray())?.toCacheFile(message!!.msg.localPath)
                } else {
                    File(message!!.msg.localPath)
                }
                if (activity.isFinishing || activity.isDestroyed) {
                    return@launch
                }
                Glide.with(activity).load(file).apply(RequestOptions().placeholder(R.drawable.bg_image_placeholder)).into(contentView.iv_image)
            }
        }
    }

    private fun setViewParams(height: Int, width: Int, view: View) {
        val params = view.layoutParams
        params.height = height
        params.width = width
        view.layoutParams = params
    }

    private fun downloadOrPlayVideo() {
        val localPath = message?.msg?.localPath
        if (localPath.isNullOrEmpty() || !File(localPath).exists()) {
            doDownloadWork(false)
        } else {
            ARouter.getInstance().build(AppRoute.VIDEO_PLAYER)
                    .withSerializable("message", message)
                    .withString("videoUrl", localPath)
                    .navigation()
        }
    }

    @AfterPermissionGranted(SAVE_VIDEO)
    private fun doDownloadWork(auto: Boolean) {
        if (PermissionUtil.hasWriteExternalPermission() && message != null) {
            if(downloading) return
            downloading = true
            val folder = File(activity.filesDir.path + "/download/video")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val task = DownloadTask.Builder(message?.msg?.mediaUrl!!, folder)
                    .setFilename("${AppConfig.ENC_PREFIX}video_" + message?.sendTime + "_" + message?.senderId + "." + FileUtils.getExtension(message?.msg?.mediaUrl))
                    .build()
            val status = StatusUtil.getStatus(task)
            if (status == StatusUtil.Status.RUNNING) {
                if (contentView.iv_status.visibility == View.GONE) {
                    if (!auto) {
                        ShowUtils.showSysToast(activity, R.string.chat_tips_downloading)
                    }
                    downloading = false
                    return
                } else {
                    task.cancel()
                }
            }
            task.enqueue(object : DownloadListener1() {
                override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {
                    contentView.pb_video.maxValue = 100
                    if (contentView.pb_video.visibility == View.GONE) {
                        contentView.pb_video.visibility = View.VISIBLE
                    }
                    if (contentView.iv_status.visibility == View.VISIBLE) {
                        contentView.iv_status.visibility = View.GONE
                    }
                }

                override fun retry(task: DownloadTask, cause: ResumeFailedCause) {

                }

                override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {

                }

                override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                    contentView.pb_video.progress = (currentOffset * 1.0f / totalLength * 100).toInt()
                }

                override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                    downloading = false
                    contentView.pb_video.visibility = View.GONE
                    contentView.iv_status.visibility = View.VISIBLE
                    when (cause) {
                        EndCause.COMPLETED -> if (task.file != null) {
                            contentView.iv_status.setImageResource(R.drawable.icon_video_play)
                            message?.msg?.localPath = task.file!!.absolutePath
                            setupChatVideo()
                        } else {
                            contentView.iv_status.setImageResource(R.drawable.icon_video_download)
                            message?.msg?.localPath = null
                            if (!auto) {
                                ShowUtils.showSysToast(activity, R.string.chat_tips_video_download_fail)
                            }
                        }
                        EndCause.CANCELED -> message?.msg?.localPath = null
                        else -> {
                            contentView.iv_status.setImageResource(R.drawable.icon_video_download)
                            message?.msg?.localPath = null
                            if (!auto) {
                                ShowUtils.showSysToast(activity, R.string.chat_tips_video_download_fail)
                            }
                        }
                    }
                }
            })
        } else {
            EasyPermissions.requestPermissions(activity, activity.getString(R.string.chat_error_permission_storage), SAVE_VIDEO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}