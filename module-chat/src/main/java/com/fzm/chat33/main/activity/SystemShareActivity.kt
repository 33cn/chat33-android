package com.fzm.chat33.main.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.net.subscribers.Loadable
import com.fuzamei.common.utils.PathUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.common.widget.LoadingDialog
import com.fuzamei.componentservice.app.AppRoute
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatFile
import com.fuzamei.componentservice.app.RouterHelper
import com.fzm.chat33.utils.FileUtils
import java.io.File

/**
 * @author zhengjy
 * @since 2019/05/15
 * Description:系统分享中转页面
 */
@Route(path = AppRoute.SYSTEM_SHARE)
class SystemShareActivity : Activity(), Loadable {

    private var chatFile: ChatFile? = null
    private var dialog: LoadingDialog? = null

    @JvmField
    @Autowired
    var data: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_share)
        ARouter.getInstance().inject(this)
        try {
            initData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("WrongConstant")
    private fun initData() {
        loading(true)
        window.decorView.postDelayed({
            if (data != null) {
                chatFile = data?.getSerializable("chatFile") as ChatFile?
                ARouter.getInstance().build(AppRoute.CONTACT_SELECT).withSerializable("chatFile", chatFile).navigation()
            } else if (Intent.ACTION_SEND == intent.action && intent.type != null) {
                val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                if (uri == null) {
                    val text: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (text.isNullOrEmpty()) {
                        ShowUtils.showToastNormal(getString(R.string.chat_tips_resource_fail))
                    } else {
                        chatFile = ChatFile.newText(text)
                    }
                } else {
                    when {
                        intent.type!!.startsWith("image/") -> {
                            val path = PathUtils.getPath(this, uri)
                            if (path == null || !File(path).exists()) {
                                ShowUtils.showToastNormal(getString(R.string.chat_tips_resource_fail))
                            } else {
                                val heightWidth = ToolUtils.getLocalImageHeightWidth(path)
                                chatFile = ChatFile.newImage(path, heightWidth[0], heightWidth[1])
                            }
                        }
                        intent.type!!.startsWith("video/") -> {
                            val path = PathUtils.getPath(this, uri)
                            if (path == null || !File(path).exists()) {
                                ShowUtils.showToastNormal(getString(R.string.chat_tips_resource_fail))
                            } else {
                                val duration = ToolUtils.getVideoDuration(path)
                                val thumb = ToolUtils.getVideoPhoto(path)
                                chatFile = ChatFile.newVideo(duration.toInt(), path, thumb.height, thumb.width)
                            }
                        }
                        else -> {
                            val path = PathUtils.getPath(this, uri)
                            if (path == null || !File(path).exists()) {
                                ShowUtils.showToastNormal(getString(R.string.chat_tips_resource_fail))
                            } else {
                                val file = File(path)
                                chatFile = ChatFile.newFile(path, file.name, FileUtils.getLength(path), FileUtils.getFileMD5(file))
                            }
                        }
                    }
                }
                if (chatFile != null) {
                    val data = Bundle().apply {
                        putParcelable("route", Uri.parse("${RouterHelper.APP_LINK}?type=systemShare"))
                        putSerializable("chatFile", chatFile)
                    }
                    ARouter.getInstance().build(AppRoute.CONTACT_SELECT).withBundle("data", data).navigation()
                }
            }
            dismiss()
            finish()
        }, 500)
    }

    override fun loading(cancelable: Boolean) {
        if (dialog == null) {
            dialog = LoadingDialog(this, cancelable)
        }
        if (dialog?.isShowing == true) {
            dialog?.show()
        }
    }

    override fun dismiss() {
        if (dialog != null) {
            if (!this.isFinishing && dialog?.isShowing == true) {
                dialog?.cancel()
            }
        }
    }


}