package com.fzm.chat33.widget.chatpraise

import android.Manifest
import android.app.Activity
import android.os.Environment
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.PermissionUtil
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.hepler.FileDownloadManager
import com.fzm.chat33.utils.FileUtils
import kotlinx.android.synthetic.main.chat_message_praise_file.view.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

/**
 * 创建日期：2019/11/28
 * 描述:
 * 作者:yll
 */
class ChatPraiseFile(activity: Activity) : ChatPraiseBase(activity) {

    companion object {

        const val SAVE_FILE = 1
    }

    private var downloading = false
    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_file
    }

    override fun initView() {
        contentView.setOnClickListener {
            fileClick()
        }
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        contentView.tv_file_name.text = message.msg.fileName
        contentView.tv_file_size.text = activity.getString(R.string.chat_file_size, ToolUtils.byte2Mb(message.msg.fileSize))
        contentView.iv_file_type.setImageResource(when (FileUtils.getExtension(message.msg.fileName)) {
            "doc", "docx" -> R.mipmap.icon_file_doc
            "pdf" -> R.mipmap.icon_file_pdf
            "ppt", "pptx" -> R.mipmap.icon_file_other
            "xls", "xlsx" -> R.mipmap.icon_file_xls
            "mp3", "wma", "wav", "ogg" -> R.mipmap.icon_file_music
            "mp4", "avi", "rmvb", "flv", "f4v", "mpg", "mkv" -> R.mipmap.icon_file_video
            "ext" -> R.mipmap.icon_file_other
            else -> R.mipmap.icon_file_other
        })
        if (message.msg.localPath == null || !File(message.msg.localPath).exists()) {
            // 自动下载文件
            doDownloadWork(true)
        }
    }

    private fun fileClick() {
        val localPath = message?.msg?.localPath
        if (localPath.isNullOrEmpty() || !File(message?.msg?.localPath).exists()) {
            doDownloadWork(false)
        } else {
            ARouter.getInstance().build(AppRoute.FILE_DETAIL).withSerializable("message", message).navigation()
        }
    }

    @AfterPermissionGranted(SAVE_FILE)
    private fun doDownloadWork(auto: Boolean) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            if(downloading) return
            downloading = true
            val folder = File(activity.filesDir.path + "/download/file")
            if (!folder.exists()) {
                folder.mkdirs()
            }

            FileDownloadManager.INS.download(folder, message, object : FileDownloadManager.DownloadCallback {
                override fun onStart() {
                    contentView.pb_file.maxValue = 100
                    if (contentView.pb_file.visibility == View.GONE) {
                        contentView.pb_file.visibility = View.VISIBLE
                    }
                }

                override fun onAlreadyRunning() {
                    if (!auto) {
                        ShowUtils.showSysToast(activity, R.string.chat_tips_downloading)
                    }
                }

                override fun onProgress(progress: Float) {
                    contentView.pb_file.progress = (progress * 100).toInt()
                }

                override fun onFinish(file: File?, throwable: Throwable?) {
                    downloading = false
                    contentView.pb_file.visibility = View.GONE
                    if (file != null) {
                        message?.msg?.localPath = file.absolutePath
                        if (!auto) {
                            ShowUtils.showSysToast(activity, activity.getString(R.string.chat_tips_file_download_to, file.absolutePath))
                        }
                        RoomUtils.run(Runnable { ChatDatabase.getInstance().chatMessageDao().insert(message) })
                    } else {
                        message?.msg?.localPath = null
                        if (!auto) {
                            ShowUtils.showSysToast(activity, R.string.chat_tips_file_download_fail)
                        }
                    }
                }
            })
        } else {
            EasyPermissions.requestPermissions(activity, activity.getString(R.string.chat_error_permission_storage), SAVE_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}