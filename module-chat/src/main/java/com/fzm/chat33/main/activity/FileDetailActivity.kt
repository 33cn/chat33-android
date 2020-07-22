package com.fzm.chat33.main.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.common.widget.BottomPopupWindow
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.LoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.helper.WeChatHelper
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.param.toDecParams
import com.fzm.chat33.core.db.bean.BriefChatLog
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.manager.FileEncryption
import com.fzm.chat33.core.manager.toByteArray
import com.fzm.chat33.core.manager.toFile
import com.fzm.chat33.utils.FileUtils
import kotlinx.android.synthetic.main.activity_file_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * @author zhengjy
 * @since 2019/02/13
 * Description:文件详情界面
 */
@Route(path = AppRoute.FILE_DETAIL)
class FileDetailActivity : LoadableActivity() {

    var popupWindow: BottomPopupWindow? = null

    var chatFile: ChatFile? = null
    @Autowired
    @JvmField
    var message: ChatMessage? = null
    @Autowired
    @JvmField
    var chatLog: BriefChatLog? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_file_detail
    }

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        if (message != null && chatLog == null) {
            chatFile = message?.msg
        } else if (chatLog != null) {
            chatFile = chatLog?.msg
        }
        iv_back.setOnClickListener { finish() }
        iv_more.setOnClickListener { showPopup() }
        tv_open.setOnClickListener { openFile(instance, chatFile?.localPath ?: "") }
        tv_title_middle.text = chatFile?.fileName
        tv_file_name.text = chatFile?.fileName
        tv_file_size.text = ToolUtils.byte2Mb(chatFile?.fileSize ?: 0L)
        val ext = FileUtils.getExtension(chatFile?.fileName)
        when (ext) {
            "doc", "docx" -> iv_file_type.setImageResource(R.mipmap.icon_file_doc)
            "pdf" -> iv_file_type.setImageResource(R.mipmap.icon_file_pdf)
            "xls", "xlsx" -> iv_file_type.setImageResource(R.mipmap.icon_file_xls)
            "mp3", "wma", "wav", "ogg" -> iv_file_type.setImageResource(R.mipmap.icon_file_music)
            "mp4", "avi", "rmvb", "flv", "f4v", "mpg", "mkv", "mov" -> iv_file_type.setImageResource(R.mipmap.icon_file_video)
            else -> iv_file_type.setImageResource(R.mipmap.icon_file_other)
        }
        open_tips.text = getString(R.string.chat_tips_can_not_open, getString(R.string.application_name))
    }

    override fun initData() {

    }

    override fun setEvent() {

    }

    private fun showPopup() {
        if (popupWindow == null) {
            val options = Arrays.asList(*resources.getStringArray(R.array.chat_choose_file_detail))
            popupWindow = BottomPopupWindow(this, options,
                    BottomPopupWindow.OnItemClickListener { _, popupWindow, position ->
                        popupWindow.dismiss()
                        if (position == 0) {
                            chatFile?.chatFileType = ChatMessage.Type.FILE
                            ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                                    .withSerializable("params", message?.toDecParams())
                                    .withSerializable("chatFile", chatFile)
                                    .navigation()
                        } else if (position == 1) {
                            openFile(instance, chatFile?.localPath ?: "")
                        } else if (position == 2) {
                            if (message != null) {
                                WeChatHelper.INS.shareFile(message?.msg?.fileName, message?.msg?.localPath, WeChatHelper.SESSION)
                            } else if (chatLog != null) {
                                WeChatHelper.INS.shareFile(chatLog?.msg?.fileName, chatLog?.msg?.localPath, WeChatHelper.SESSION)
                            }
                        }
                    })
        }
        popupWindow?.showAtLocation(iv_more, Gravity.BOTTOM, 0, 0)
    }

    private fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    }

    private fun openFile(context: Context, path: String) = GlobalScope.launch(Dispatchers.Main) {
        val file = if (AppConfig.FILE_ENCRYPT && path.contains(AppConfig.ENC_PREFIX)) {
            FileEncryption.decrypt(message?.toDecParams(), File(path).toByteArray())
                    ?.toFile(cacheDir.path + "/file", chatFile!!.fileName)
        } else {
            File(path)
        }
        try {
            if (file == null) {
                ShowUtils.showToastNormal(context, getString(R.string.chat_tips_file_not_found))
                return@launch
            }
            val intent = Intent().run {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                action = Intent.ACTION_VIEW
                setDataAndType(getUriForFile(context, file), FileUtils.getMimeType(file.path))
            }
            context.startActivity(intent)
            Intent.createChooser(intent, getString(R.string.chat_tips_select_open_type))
        } catch (e: ActivityNotFoundException) {
            ShowUtils.showToastNormal(context, getString(R.string.chat_tips_unsupported_file_type))
        }
    }
}
