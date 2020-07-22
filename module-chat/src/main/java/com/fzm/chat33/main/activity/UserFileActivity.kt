package com.fzm.chat33.main.activity

import android.Manifest
import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import android.os.Environment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.*
import com.fuzamei.common.widget.BottomPopupWindow
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.request.chat.PreForwardRequest
import com.fzm.chat33.hepler.FileDownloadManager
import com.fzm.chat33.main.mvvm.ChatFileViewModel
import com.fzm.chat33.utils.FileUtils
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.google.gson.Gson
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.qmuiteam.qmui.util.QMUIDirection
import com.qmuiteam.qmui.util.QMUIViewHelper
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_user_file.*
import org.kodein.di.generic.instance
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * @author zhengjy
 * @since 2019/02/19
 * Description:群里某用户发送的所有文件
 */
@Route(path = AppRoute.GROUP_USER_FILE)
class UserFileActivity : DILoadableActivity() {

    companion object {
        private const val SAVE_FILE = 1
    }

    lateinit var chatFileAdapter: CommonAdapter<ChatMessage>
    var data = arrayListOf<ChatMessage>()
    var checkStatus: SparseBooleanArray = SparseBooleanArray()
    var popupWindow: BottomPopupWindow? = null
    private var selectable = false
    var nextLog = ""

    private val gson: Gson by instance()
    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: ChatFileViewModel
    var refresh = true

    @Autowired
    @JvmField
    var roomId: String? = null

    @Autowired
    @JvmField
    var userId: String? = null

    @Autowired
    @JvmField
    var name: String = ""

    override fun getLayoutId(): Int {
        return R.layout.activity_user_file
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        tv_title_middle.text = getString(R.string.chat_title_user_chat_file, name)
        viewModel = findViewModel(provider)
        viewModel.setChatTarget(Chat33Const.CHANNEL_ROOM, roomId)
    }

    override fun initData() {
        rv_chat_file.layoutManager = LinearLayoutManager(this)
        rv_chat_file.addItemDecoration(RecyclerViewDivider(this, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(this, R.color.chat_forward_divider_receive)))
        chatFileAdapter = object : CommonAdapter<ChatMessage>(this, R.layout.item_chat_file, data) {
            @SuppressLint("CheckResult")
            override fun convert(holder: ViewHolder?, message: ChatMessage?, position: Int) {
                val ext = FileUtils.getExtension(message?.msg?.fileName)
                when (ext) {
                    "doc", "docx" -> holder?.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_doc)
                    "pdf" -> holder?.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_pdf)
                    "xls", "xlsx" -> holder?.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_xls)
                    "mp3", "wma", "wav", "ogg" -> holder?.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_music)
                    "mp4", "avi", "rmvb", "flv",
                    "f4v", "mpg", "mkv", "mov" -> holder?.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_video)
                    else -> holder?.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_other)
                }
                holder?.setVisible(R.id.cb_select, selectable)
                val tvFileName = holder?.getView<TextView>(R.id.tv_file_name)
                if (!TextUtils.isEmpty(message?.msg?.encryptedMsg)) {
                    tvFileName?.setText(R.string.chat_tips_encrypted_file)
                    tvFileName?.setTextColor(ContextCompat.getColor(instance, R.color.chat_text_grey_light))
                    val drawable = ContextCompat.getDrawable(instance, R.drawable.ic_encrypted_lock)
                    drawable?.setBounds(0, 0, ScreenUtils.dp2px(20f), ScreenUtils.dp2px(20f))
                    tvFileName?.setCompoundDrawables(null, null, drawable, null)
                    tvFileName?.compoundDrawablePadding = 5
                } else {
                    tvFileName?.text = message?.msg?.fileName
                    tvFileName?.setTextColor(ContextCompat.getColor(instance, R.color.chat_text_grey_dark))
                    tvFileName?.setCompoundDrawables(null, null, null, null)
                }
                holder?.setText(R.id.tv_file_date, ToolUtils.formatDay(message?.sendTime ?: 0L))
                holder?.setText(R.id.tv_file_size, ToolUtils.byte2Mb(message?.msg?.fileSize ?: 0L))
                holder?.setTag(R.id.cb_select, position)
                val cbSelect: CheckBox = holder?.getView(R.id.cb_select)!!
                cbSelect.isChecked = checkStatus.get(position)
                cbSelect.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.tag == position) {
                        if (message != null) {
                            if (isChecked) {
                                viewModel.messageItems.add(message)
                                viewModel.fileLogIds.add(message.logId)
                            } else {
                                viewModel.messageItems.remove(message)
                                viewModel.fileLogIds.remove(message.logId)
                            }
                        }
                        checkStatus.put(position, isChecked)
                    }
                }
                holder.setText(R.id.tv_uploader, name)
                ChatDatabase.getInstance().chatMessageDao().mayGetMessageById(message!!.logId, message.channelType)
                        .run(Consumer {
                            decryptGroupSingle(it)
                            message.msg = it.msg
                        }, Consumer {
                            message.ignoreInHistory = 1
                        })
            }
        }
        chatFileAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                if (selectable) {
                    val cb_select: View = view?.findViewById(R.id.cb_select)!!
                    cb_select.performClick()
                    return
                }
                if (!data[position].msg.encryptedMsg.isNullOrEmpty()) {
                    EasyDialog.Builder()
                            .setHeaderTitle(getString(R.string.chat_tips_tips))
                            .setContent(getString(R.string.chat_dialog_encrypt_chat_file))
                            .setBottomLeftText(getString(R.string.chat_action_confirm))
                            .setBottomLeftClickListener { it.dismiss() }.create(instance).show()
                    return
                }
                if (data[position].msg.localPath == null || !File(data[position].msg.localPath).exists()) {
                    doDownloadWork(position)
                } else {
                    ARouter.getInstance().build(AppRoute.FILE_DETAIL).withSerializable("message", data[position]).navigation()
                }
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }
        })
        rv_chat_file.adapter = chatFileAdapter
        getChatFileList("", true)
    }

    @AfterPermissionGranted(SAVE_FILE)
    private fun doDownloadWork(position: Int) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            val folder = File("${filesDir.path}/download/file")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            if (data[position].msg.fileUrl.isNullOrEmpty()) {
                return
            }
            val task = DownloadTask.Builder(data[position].msg.fileUrl, folder)
                    .setFilename(data[position].msg.fileName)
                    .build()
            val status = StatusUtil.getStatus(task)
            if (status == StatusUtil.Status.RUNNING) {
                ShowUtils.showSysToast(instance, getString(R.string.chat_tips_downloading))
                return
            }
            task.enqueue(object : DownloadListener1() {
                override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {
                    ShowUtils.showSysToast(instance, getString(R.string.chat_tips_start_download))
                }

                override fun retry(task: DownloadTask, cause: ResumeFailedCause) {

                }

                override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {

                }

                override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {

                }

                override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                    when (cause) {
                        EndCause.COMPLETED -> if (task.file != null) {
                            data[position].msg.localPath = task.file!!.absolutePath
                            ShowUtils.showSysToast(instance, getString(R.string.chat_tips_file_download_to, task.file!!.absolutePath))
                        } else {
                            data[position].msg.localPath = null
                            ShowUtils.showSysToast(instance, getString(R.string.chat_tips_file_download_fail))
                        }
                        else -> {
                            data[position].msg.localPath = null
                            ShowUtils.showSysToast(instance, getString(R.string.chat_tips_file_download_fail))
                        }
                    }
                    RoomUtils.run { ChatDatabase.getInstance().chatMessageDao().insert(data[position]) }
                }
            })
        } else {
            EasyPermissions.requestPermissions(instance, getString(R.string.chat_permission_storage), SAVE_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun setEvent() {
        iv_back.setOnClickListener { onBackPressed() }
        tv_switch_choose.setOnClickListener {
            if (!animating) {
                switchSelectMode()
            }
        }
        ll_forward.setOnClickListener { forward(1) }
        ll_batch_forward.setOnClickListener { forward(2) }
        ll_download.setOnClickListener {
            if (viewModel.messageItems.size == 0) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_file1))
                return@setOnClickListener
            }
            for (message in viewModel.messageItems) {
                if (!message.msg.encryptedMsg.isNullOrEmpty()) {
                    continue
                }
                downloadFile(message)
            }
        }
        ll_delete.setOnClickListener {
            if (viewModel.fileLogIds.size == 0) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_file3))
                return@setOnClickListener
            }
            showPopup()
        }
        swipeLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                getChatFileList("", true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getChatFileList(nextLog, false)
            }
        })

        viewModel.loading.observe(this, Observer {
            if (it?.loading == true) {
                loading(it.cancelable)
            } else {
                dismiss()
            }
        })
        viewModel.revokeResponse.observe(this, Observer {
            val data = it.stateResponse
            if(data != null) {
                if (data.state == 0) {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_delete_success))
                } else {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_delete_not_permitted, data.state))
                }
                getChatFileList("", true)
            } else {
                ShowUtils.showToastNormal(instance, it.apiException?.message)
            }
        })
        viewModel.chatFiles.observe(this, Observer {
            if(it.chatFileResponse != null) {
                val data = it.chatFileResponse
                val list = data.logs
                if (list != null && list.size > 0) {
                    onGetChatFileSuccess(list, data.nextLog, refresh)
                } else {
                    onGetChatFileSuccess(ArrayList(), "-1", refresh)
                }
            } else {
                swipeLayout.finishRefresh(0, false)
                swipeLayout.finishLoadMore(0, false, false)
                ShowUtils.showToast(instance, it.apiException?.message)
            }
        })
    }

    private fun showPopup() {
        if (popupWindow == null) {
            val options = Arrays.asList(*resources.getStringArray(R.array.chat_choose_delete_file))
            popupWindow = BottomPopupWindow(this, options,
                    BottomPopupWindow.OnItemClickListener { _, popupWindow, position ->
                        popupWindow.dismiss()
                        if (position == 0) {
                            viewModel.revokeFiles()
                        } else if (position == 1) {
                            for (logId in viewModel.fileLogIds) {
                                ChatDatabase.getInstance().chatMessageDao().mayGetMessageById(logId, Chat33Const.CHANNEL_ROOM).run(Consumer {
                                    val path = it?.msg?.localPath
                                    if (path != null) {
                                        val file = File(path)
                                        if (file.exists()) {
                                            file.delete()
                                        }
                                    }
                                    it?.msg?.localPath = null
                                    RoomUtils.run {
                                        ChatDatabase.getInstance().chatMessageDao().insert(it)
                                    }
                                })
                            }
                            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_delete_success))
                        }
                        switchSelectMode()
                    })
        }
        popupWindow?.showAtLocation(ll_select_options, Gravity.BOTTOM, 0, 0)
    }

    private fun forward(forwardType: Int) {
        if (viewModel.messageItems.size == 0) {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tip_choose_msg2))
            return
        }
        val type = 1
        val request = PreForwardRequest(roomId, type, forwardType, viewModel.messageItems)
        ARouter.getInstance().build(AppRoute.CONTACT_SELECT).withSerializable("preForward", request).navigation()
    }

    private fun getChatFileList(startId: String, refresh: Boolean) {
        this.refresh = refresh
        viewModel.getUserChatFileList(userId, startId)
    }

    private fun onGetChatFileSuccess(list: List<ChatMessage>, nextLog: String, refresh: Boolean) {
        this.nextLog = nextLog
        if (refresh) {
            data.clear()
        }
        swipeLayout.finishRefresh()
        if (nextLog == "-1") {
            swipeLayout.finishLoadMoreWithNoMoreData()
        } else {
            swipeLayout.finishLoadMore()
        }
        decryptGroupMessage(list)
        data.addAll(list)
        chatFileAdapter.notifyDataSetChanged()
    }

    private fun decryptGroupMessage(list: List<ChatMessage>) {
        for (item in list) {
            decryptGroupSingle(item)
        }
    }

    private fun decryptGroupSingle(item: ChatMessage) {
        if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
            item.encrypted = 1
            if (item.msg.kid != null) {
                try {
                    val kid = item.msg.kid
                    val roomKey = ChatDatabase.getInstance().roomKeyDao().getRoomKeyById(item.receiveId, item.msg.kid)
                    val chatFile = CipherManager.decryptSymmetric(item.msg.encryptedMsg!!, roomKey.keySafe)
                    item.msg = gson.fromJson(chatFile, ChatFile::class.java)
                    item.msg.kid = kid
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun onBackPressed() {
        if (selectable) {
            switchSelectMode()
        } else {
            finish()
        }
    }

    private fun switchSelectMode() {
        checkStatus.clear()
        chatFileAdapter.notifyDataSetChanged()
        if (selectable) {
            ll_select_options.visibility = View.GONE
            tv_switch_choose.text = getString(R.string.chat_action_choose)
            selectable = false
            QMUIViewHelper.slideOut(ll_select_options, 500, animateListener, true, QMUIDirection.TOP_TO_BOTTOM)
        } else {
            ll_select_options.visibility = View.VISIBLE
            tv_switch_choose.text = getString(R.string.chat_action_cancel)
            selectable = true
            QMUIViewHelper.slideIn(ll_select_options, 500, animateListener, true, QMUIDirection.BOTTOM_TO_TOP)
        }
    }

    private fun downloadFile(message: ChatMessage) {
        val folder = File("${filesDir.path}/save")
        if (message.msg.localPath == null || !File(message.msg.localPath).exists() || !message.msg.localPath.contains(folder.absolutePath)) {
            if (message.msg.downloading) {
                return
            }
            if (!folder.exists()) {
                folder.mkdirs()
            }
            message.msg.downloading = true
            var fileName by PreferenceDelegate("${AppConfig.ENC_PREFIX}tempPath_${message.logId}", "")
            if (TextUtils.isEmpty(fileName)) {
                fileName = FileUtils.getFileNameUnique(folder, message.msg.fileName)
            }
            val task = when {
                message.msgType == ChatMessage.Type.FILE -> DownloadTask.Builder(message.msg.fileUrl, folder)
                        .setFilename(fileName)
                        .build()
                message.msgType == ChatMessage.Type.VIDEO -> DownloadTask.Builder(message.msg.mediaUrl, folder)
                        .setFilename("${AppConfig.ENC_PREFIX}video_${message.sendTime}_${message.senderId}.${FileUtils.getExtension(message.msg.mediaUrl)}")
                        .build()
                else -> DownloadTask.Builder(message.msg.imageUrl, folder)
                        .setFilename("${AppConfig.ENC_PREFIX}image_${message.sendTime}_${message.senderId}.${FileUtils.getExtension(message.msg.imageUrl)}")
                        .build()
            }
            if (message.msgType == ChatMessage.Type.FILE) {
                downloadFile(folder, message)
            } else {
                downloadMedia(task, message, fileName)
            }
        }
    }

    private fun downloadFile(folder: File, message: ChatMessage) {
        FileDownloadManager.INS.download(folder, message, object : FileDownloadManager.DownloadCallback {
            override fun onStart() {

            }

            override fun onAlreadyRunning() {

            }

            override fun onProgress(progress: Float) {

            }

            override fun onFinish(file: File?, throwable: Throwable?) {
                message.msg.localPath = file?.absolutePath
                RoomUtils.run { ChatDatabase.getInstance().chatMessageDao().insert(message) }
            }
        })
    }

    private fun downloadMedia(task: DownloadTask, message: ChatMessage, fileName: String) {
        val status = StatusUtil.getStatus(task)
        if (status == StatusUtil.Status.RUNNING) {
            return
        }
        var temp by PreferenceDelegate("tempPath_${message.logId}", "")
        temp = fileName
        task.enqueue(object : DownloadListener1() {
            override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {

            }

            override fun retry(task: DownloadTask, cause: ResumeFailedCause) {

            }

            override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {

            }

            override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {

            }

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                message.msg.downloading = false
                var result by PreferenceDelegate("tempPath_${message.logId}", "")
                result = ""
                when (cause) {
                    EndCause.COMPLETED -> if (task.file != null) {
                        message.msg.localPath = task.file!!.absolutePath
                    } else {
                        message.msg.localPath = null
                    }
                    else -> {
                        message.msg.localPath = null
                    }
                }
                RoomUtils.run { ChatDatabase.getInstance().chatMessageDao().insert(message) }
            }
        })
    }

    private var animating = false

    private val animateListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {

        }

        override fun onAnimationEnd(animation: Animation) {
            animating = false
        }

        override fun onAnimationRepeat(animation: Animation) {

        }
    }
}
