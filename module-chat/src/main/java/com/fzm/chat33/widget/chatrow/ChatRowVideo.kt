package com.fzm.chat33.widget.chatrow

import android.Manifest
import androidx.fragment.app.FragmentActivity

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.utils.PermissionUtil
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.common.utils.ScreenUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.param.toDecParams
import com.fzm.chat33.core.manager.FileEncryption
import com.fzm.chat33.main.adapter.ChatListAdapter
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.dao.ChatMessageDao
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.manager.toByteArray
import com.fzm.chat33.core.manager.toCacheFile
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

import java.io.File

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class ChatRowVideo(activity: FragmentActivity, message: ChatMessage, position: Int, adapter: ChatListAdapter) : ChatRowBase(activity, message, position, adapter), SnapChat {

    companion object {

        private const val SAVE_VIDEO = 2
    }

    private var iv_lock: View? = null
    private var chat_message_snap: View? = null
    private var rl_image: View? = null
    private var ivImage: ImageView? = null
    private var iv_status: ImageView? = null
    private var iv_cancel: ImageView? = null
    private var tv_count: TextView? = null
    private var tv_forward: TextView? = null
    private var tv_duration: TextView? = null
    private var thumb_up: TextView? = null
    internal var pb_video: QMUIProgressBar? = null
    private var timer: SnapChatCountDown? = null
    private val chatDao: ChatMessageDao
    private var task: DownloadTask? = null

    private val formatUrl: String

    init {
        formatUrl = "?x-oss-process=image/resize,h_" + ScreenUtils.dp2px(activity, 150f) + "/quality,q_70/format,jpg/interlace,1"
        chatDao = ChatDatabase.getInstance().chatMessageDao()
    }

    internal override fun getLayoutId(): Int {
        return if (message.isSentType) R.layout.chat_row_sent_video else R.layout.chat_row_receive_video
    }

    internal override fun onFindViewById() {
        rl_image = rootView.findViewById(R.id.rl_image)
        ivImage = rootView.findViewById(R.id.iv_image)
        pb_video = rootView.findViewById(R.id.pb_video)
        iv_status = rootView.findViewById(R.id.iv_status)
        tv_duration = rootView.findViewById(R.id.tv_duration)
        iv_lock = rootView.findViewById(R.id.iv_lock)
        thumb_up = rootView.findViewById(R.id.thumb_up)
        if (message.isSentType) {
            tv_forward = rootView.findViewById(R.id.tv_forward)
        } else {
            iv_cancel = rootView.findViewById(R.id.iv_cancel)
        }
    }

    internal override fun onSetUpView() {
        if (!message.isSentType) {
            tv_count = rootView.findViewById(R.id.tv_count)
            chat_message_snap = rootView.findViewById(R.id.chat_message_snap)
        } else {
            if (message.msg.sourceChannel == Chat33Const.CHANNEL_ROOM) {
                tv_forward!!.visibility = View.VISIBLE
                tv_forward!!.text = activity.getString(R.string.chat_forward_room_content, message.msg.sourceName)
            } else if (message.msg.sourceChannel == Chat33Const.CHANNEL_FRIEND) {
                tv_forward!!.visibility = View.VISIBLE
                tv_forward!!.text = activity.getString(R.string.chat_forward_friend_content, message.msg.sourceName)
            } else {
                tv_forward!!.visibility = View.GONE
            }
        }
        if (message.isSnap == 1) {
            if (message.isSentType) {
                iv_lock!!.visibility = View.VISIBLE
            } else if (message.snapVisible == 0) {
                hideContent()
            } else {
                showContent()
            }
        } else {
            if (task != null && StatusUtil.getStatus(task!!) == StatusUtil.Status.RUNNING) {
                iv_status!!.visibility = View.GONE
            } else if (message.msg.localPath == null || !File(message.msg.localPath).exists()) {
                iv_status!!.visibility = View.VISIBLE
                iv_status!!.setImageResource(R.drawable.icon_video_download)
            } else {
                iv_status!!.visibility = View.VISIBLE
                iv_status!!.setImageResource(R.drawable.icon_video_play)
            }
            ivImage!!.visibility = View.VISIBLE
            if (!message.isSentType) {
                tv_count!!.visibility = View.GONE
                chat_message_snap!!.visibility = View.GONE
            }
            iv_lock!!.visibility = View.GONE
        }
        if (chat_message_snap != null) {
            chat_message_snap!!.setOnClickListener {
                ChatMessageDao.visibleSnapMsg.add(message.channelType.toString() + "-" + message.logId)
                // TODO:2019年10月24日 14:44:41，如果视频支持阅后即焚，则需要请求阅读阅后即焚接口
            }
        }
        if (iv_cancel != null) {
            iv_cancel!!.setOnClickListener {
                if (task != null) {
                    val status = StatusUtil.getStatus(task!!)
                    if (status == StatusUtil.Status.RUNNING) {
                        task!!.cancel()
                    }
                }
            }
        }
        tv_duration!!.text = ToolUtils.formatVideoDuration(message.msg.duration)
        if (message.msg.height > 0 && message.msg.width > 0) {
            val result = ToolUtils.getChatImageHeightWidth(activity, message.msg.height, message.msg.width)
            val height = result[0]
            val width = result[1]
            setViewParams(height, width, rl_image!!)
            setViewParams(height, width, ivImage!!)
        } else {
            //获取不到视频尺寸的情况，拿到bitmap后获取宽高，然后载入imageView
            val height = ScreenUtils.dp2px(activity, 150f)
            val width = ScreenUtils.dp2px(activity, 150f)
            setViewParams(height, width, rl_image!!)
            setViewParams(height, width, ivImage!!)
        }
        ivImage?.setImageResource(R.drawable.bg_image_placeholder)
        if (message.msg.localPath == null || !File(message.msg.localPath).exists()) {
            // 自动下载视频
            doDownloadWork(true)
        } else {
            setupChatVideo()
        }
    }

    fun clickBubble() {
        downloadOrPlayVideo()
    }

    override fun chatMainView(): View? {
        return rl_image
    }

    override fun thumbUpView(): TextView? {
        return thumb_up
    }

    private fun setupChatVideo() {
        if (message.msg.localPath == null) {
            return
        }
        if (!message.isSentType && message.isSnap == 1) {
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            val file = if (AppConfig.FILE_ENCRYPT && message.msg.localPath.contains(AppConfig.ENC_PREFIX)) {
                FileEncryption.decrypt(message.toDecParams(), File(message.msg.localPath).toByteArray())?.toCacheFile(message.msg.localPath)
            } else {
                File(message.msg.localPath)
            }
            if (activity.isFinishing || activity.isDestroyed) {
                return@launch
            }
            Glide.with(activity).load(file).apply(RequestOptions().placeholder(R.drawable.bg_image_placeholder)).into(ivImage!!)
        }
    }

    private fun setViewParams(height: Int, width: Int, view: View) {
        val params = view.layoutParams
        params.height = height
        params.width = width
        view.layoutParams = params
    }

    private fun downloadOrPlayVideo() {
        if (message.msg.localPath == null || !File(message.msg.localPath).exists()) {
            if (message.msg.localPath != null) {
                message.msg.localPath = null
                RoomUtils.run(Runnable { ChatDatabase.getInstance().chatMessageDao().insert(message) })
            }
            doDownloadWork(false)
        } else {
            ARouter.getInstance().build(AppRoute.VIDEO_PLAYER)
                    .withSerializable("message", message)
                    .withString("videoUrl", message.msg.localPath)
                    .navigation()
        }
    }

    @AfterPermissionGranted(SAVE_VIDEO)
    private fun doDownloadWork(auto: Boolean) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            val folder = File(activity.filesDir.path + "/download/video")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            task = DownloadTask.Builder(message.msg.mediaUrl, folder)
                    .setFilename("${AppConfig.ENC_PREFIX}video_" + message.sendTime + "_" + message.senderId + "." + FileUtils.getExtension(message.msg.mediaUrl))
                    .build()
            val status = StatusUtil.getStatus(task!!)
            if (status == StatusUtil.Status.RUNNING) {
                if (iv_status!!.visibility == View.GONE) {
                    if (!auto) {
                        ShowUtils.showSysToast(activity, R.string.chat_tips_downloading)
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
                    if (iv_cancel != null && iv_cancel!!.visibility == View.GONE) {
                        iv_cancel!!.visibility = View.VISIBLE
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
                    if (iv_cancel != null) {
                        iv_cancel!!.visibility = View.GONE
                    }
                    when (cause) {
                        EndCause.COMPLETED -> if (task.file != null) {
                            iv_status!!.setImageResource(R.drawable.icon_video_play)
                            message.msg.localPath = task.file!!.absolutePath
                            setupChatVideo()
                        } else {
                            iv_status!!.setImageResource(R.drawable.icon_video_download)
                            message.msg.localPath = null
                            if (!auto) {
                                ShowUtils.showSysToast(activity, R.string.chat_tips_video_download_fail)
                            }
                        }
                        EndCause.CANCELED -> message.msg.localPath = null
                        else -> {
                            iv_status!!.setImageResource(R.drawable.icon_video_download)
                            message.msg.localPath = null
                            if (!auto) {
                                ShowUtils.showSysToast(activity, R.string.chat_tips_video_download_fail)
                            }
                        }
                    }
                    RoomUtils.run(Runnable {
                        if (message.msg.width <= 0 || message.msg.height <= 0) {
                            val thumb = ToolUtils.getVideoPhoto(message.msg.localPath)
                            message.msg.width = thumb.width
                            message.msg.height = thumb.height
                        }
                        ChatDatabase.getInstance().chatMessageDao().insert(message)
                    })
                }
            })
        } else {
            EasyPermissions.requestPermissions(activity, activity.getString(R.string.chat_error_permission_storage), SAVE_VIDEO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun hideContent() {
        ivImage!!.visibility = View.GONE
        tv_count!!.visibility = View.GONE
        chat_message_snap!!.visibility = View.VISIBLE
        iv_lock!!.visibility = View.VISIBLE
    }

    override fun showContent() {
        if (message.snapVisible == 0) {
            message.snapVisible = 1
            RoomUtils.run(Runnable { chatDao.updateVisible(message.snapVisible, message.channelType, message.logId) })
        }
        ivImage!!.visibility = View.VISIBLE
        tv_count!!.visibility = View.VISIBLE
        chat_message_snap!!.visibility = View.GONE
        iv_lock!!.visibility = View.GONE
        val thumb = ToolUtils.getVideoPhoto(message.msg.mediaUrl)
        ivImage!!.setImageBitmap(thumb)
        if (message.snapCounting == 0) {
            tv_count!!.text = com.fzm.chat33.utils.StringUtils.formateTime(30000)
            if (itemClickListener != null) {
                itemClickListener.onMessageShow(ivImage, message, position)
            }
        } else {
            startCount()
        }
    }

    override fun startCount() {
        if (itemClickListener != null) {
            itemClickListener.onMessageCountDown(message.logId, timer)
        }
        if (message.snapCounting == 0) {
            message.snapCounting = 1
            RoomUtils.run(Runnable { chatDao.updateCounting(message.snapCounting, message.channelType, message.logId) })
        }
        if (message.timer == null) {
            tv_count!!.tag = message
            timer = SnapChatCountDown(calculateRemainTime(), 1000L, tv_count, message, SnapChatCountDown.OnFinishListener { `object` -> destroyContent(`object`) })
            message.timer = timer
            timer!!.start()
        } else {
            timer = message.timer as SnapChatCountDown
            tv_count!!.tag = message
            tv_count!!.text = timer!!.currentText
            timer!!.setCountView(tv_count)
        }
    }

    override fun destroyContent(`object`: Any) {
        timer = null
        (`object` as ChatMessage).timer = null
        if (itemClickListener != null) {
            itemClickListener.onMessageDestroy(rootView, `object`)
        }
    }

    override fun calculateRemainTime(): Long {
        if (message.destroyTime == 0L) {
            message.destroyTime = System.currentTimeMillis() + 30000
            RoomUtils.run(Runnable { chatDao.updateDestroyTime(message.destroyTime, message.channelType, message.logId) })
            return 30000
        } else {
            return message.destroyTime - System.currentTimeMillis()
        }
    }
}
