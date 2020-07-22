package com.fzm.chat33.widget.forward

import android.Manifest
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
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
import com.fzm.chat33.hepler.glide.GlideApp
import com.fzm.chat33.main.adapter.ForwardListAdapter
import com.fzm.chat33.utils.FileUtils
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.qmuiteam.qmui.widget.QMUIProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:
 */
class ForwardRowVideo(context: Context, root: View, adapter: ForwardListAdapter) : ForwardRowBase(context, root, adapter) {

    companion object {
        private const val SAVE_VIDEO = 2
    }

    private var rl_image: View? = null
    private var ivImage: ImageView? = null
    private var iv_status: ImageView? = null
    private var tv_duration: TextView? = null
    internal var pb_video: QMUIProgressBar? = null
    private var task: DownloadTask? = null

    override fun onFindViewById() {
        rl_image = rootView.findViewById(R.id.rl_image)
        ivImage = rootView.findViewById(R.id.iv_image)
        pb_video = rootView.findViewById(R.id.pb_video)
        iv_status = rootView.findViewById(R.id.iv_status)
        tv_duration = rootView.findViewById(R.id.tv_duration)
    }

    override fun onSetUpView() {
        if (task != null && StatusUtil.getStatus(task!!) == StatusUtil.Status.RUNNING) {
            iv_status!!.visibility = View.GONE
        } else if (chatLog.msg.localPath == null || !File(chatLog.msg.localPath).exists()) {
            iv_status!!.visibility = View.VISIBLE
            iv_status!!.setImageResource(R.drawable.icon_video_download)
        } else {
            iv_status!!.visibility = View.VISIBLE
            iv_status!!.setImageResource(R.drawable.icon_video_play)
        }
        ivImage!!.visibility = View.VISIBLE
        tv_duration!!.text = ToolUtils.formatVideoDuration(chatLog.msg.duration)

        if (chatLog.msg.height > 0 && chatLog.msg.width > 0) {
            val result = ToolUtils.getChatImageHeightWidth(mContext, chatLog.msg.height, chatLog.msg.width)
            val height = result[0]
            val width = result[1]
            setViewParams(height, width, rl_image!!)
            setViewParams(height, width, ivImage!!)
        } else {
            //获取不到视频尺寸的情况，拿到bitmap后获取宽高，然后载入imageView
            val height = ScreenUtils.dp2px(mContext, 150f)
            val width = ScreenUtils.dp2px(mContext, 150f)
            setViewParams(height, width, rl_image!!)
            setViewParams(height, width, ivImage!!)
        }
        ivImage?.setImageResource(R.drawable.bg_image_placeholder)
        if (chatLog.msg.localPath == null || !File(chatLog.msg.localPath).exists()) {
            // 自动下载视频
            doDownloadWork(true)
        } else {
            setupChatVideo()
        }
    }

    private fun setupChatVideo() {
        if (chatLog.msg == null) {
            return
        }
        rl_image!!.setOnClickListener { downloadOrPlayVideo() }
        val temp: ChatMessage
        try {
            temp = message.clone()
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
            return
        }
        temp.briefPos = temp.msg.sourceLog.indexOf(chatLog) + 1
        val path = message.msg.sourceLog[temp.briefPos - 1].msg.localPath
        GlobalScope.launch(Dispatchers.Main) {
            val file = FileEncryption.decrypt(temp.toDecParams(), File(path).toByteArray())?.toCacheFile(path)
            val activity = mContext as Activity
            if (activity.isFinishing || activity.isDestroyed) {
                return@launch
            }
            GlideApp.with(activity).load(file).apply(RequestOptions().placeholder(R.drawable.bg_image_placeholder)).into(ivImage!!)
        }
    }

    private fun setViewParams(height: Int, width: Int, view: View) {
        val params = view.layoutParams
        params.height = height
        params.width = width
        view.layoutParams = params
    }

    private fun downloadOrPlayVideo() {
        if (chatLog.msg.localPath == null || !File(chatLog.msg.localPath).exists()) {
            if (chatLog.msg.localPath != null) {
                chatLog.msg.localPath = null
                RoomUtils.run(Runnable {
                    for (i in message.msg.sourceLog.indices) {
                        if (chatLog.logId != null && chatLog.logId == message.msg.sourceLog[i].logId) {
                            message.msg.sourceLog[i] = chatLog
                        }
                    }
                    ChatDatabase.getInstance().chatMessageDao().updateSourceLog(message.logId, message.channelType, message.msg.sourceLog)
                })
            }
            doDownloadWork(false)
        } else {
            val temp: ChatMessage
            try {
                temp = message.clone()
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
                return
            }

            temp.briefPos = temp.msg.sourceLog.indexOf(chatLog) + 1
            ARouter.getInstance().build(AppRoute.VIDEO_PLAYER)
                    .withSerializable("message", temp)
                    .withString("videoUrl", chatLog.msg.localPath)
                    .navigation()
        }
    }

    @AfterPermissionGranted(SAVE_VIDEO)
    private fun doDownloadWork(auto: Boolean) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            val folder = File(mContext.filesDir.path + "/download/video")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            task = DownloadTask.Builder(chatLog.msg.mediaUrl, folder)
                    .setFilename("${AppConfig.ENC_PREFIX}video_" + message.sendTime + "_" + message.senderId + "_" + chatLog.logId + "." + FileUtils.getExtension(chatLog.msg.mediaUrl))
                    .build()
            val status = StatusUtil.getStatus(task!!)
            if (status == StatusUtil.Status.RUNNING) {
                if (iv_status!!.visibility == View.GONE) {
                    if (!auto) {
                        ShowUtils.showSysToast(mContext, R.string.chat_tips_downloading)
                    }
                    return
                } else {
                    task!!.cancel()
                }
            }
            task!!.enqueue(object : DownloadListener1() {
                override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {
                    pb_video!!.maxValue = 100
                    if (pb_video!!.visibility == View.GONE) {
                        pb_video!!.visibility = View.VISIBLE
                    }
                    if (iv_status!!.visibility == View.VISIBLE) {
                        iv_status!!.visibility = View.GONE
                    }
                }

                override fun retry(task: DownloadTask, cause: ResumeFailedCause) {

                }

                override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {

                }

                override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                    pb_video!!.progress = (currentOffset * 1.0f / totalLength * 100).toInt()
                }

                override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                    pb_video!!.visibility = View.GONE
                    iv_status!!.visibility = View.VISIBLE
                    when (cause) {
                        EndCause.COMPLETED -> if (task.file != null) {
                            iv_status!!.setImageResource(R.drawable.icon_video_play)
                            chatLog.msg.localPath = task.file!!.absolutePath
                            setupChatVideo()
                        } else {
                            iv_status!!.setImageResource(R.drawable.icon_video_download)
                            chatLog.msg.localPath = null
                            if (!auto) {
                                ShowUtils.showSysToast(mContext, R.string.chat_tips_video_download_fail)
                            }
                        }
                        else -> {
                            iv_status!!.setImageResource(R.drawable.icon_video_download)
                            chatLog.msg.localPath = null
                            if (!auto) {
                                ShowUtils.showSysToast(mContext, R.string.chat_tips_video_download_fail)
                            }
                        }
                    }
                    RoomUtils.run(Runnable {
                        for (i in message.msg.sourceLog.indices) {
                            if (chatLog.logId != null && chatLog.logId == message.msg.sourceLog[i].logId) {
                                message.msg.sourceLog[i] = chatLog
                            }
                        }
                        ChatDatabase.getInstance().chatMessageDao().updateSourceLog(message.logId, message.channelType, message.msg.sourceLog)
                    })
                }
            })
        } else {
            EasyPermissions.requestPermissions(mContext as Activity, mContext.getString(R.string.chat_error_permission_storage),
                    SAVE_VIDEO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}
