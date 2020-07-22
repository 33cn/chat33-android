package com.fzm.chat33.main.activity

import android.Manifest
import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Environment
import androidx.viewpager.widget.ViewPager
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.ess.filepicker.FilePicker
import com.ess.filepicker.model.EssFile
import com.fuzamei.common.utils.PermissionUtil
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.run
import com.fuzamei.common.widget.BottomPopupWindow
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.Chat33Const.UPLOAD_IMAGE_PERMISSION
import com.fzm.chat33.main.fragment.ChatFileListFragment
import com.fzm.chat33.main.fragment.ChatMediaListFragment
import com.fzm.chat33.main.mvvm.ChatFileViewModel
import com.fzm.chat33.utils.FileUtils
import com.fuzamei.common.view.ScrollPagerAdapter
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.core.bean.param.toEncParams
import com.fzm.chat33.core.net.OssModel
import com.fzm.chat33.core.request.chat.PreForwardRequest
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.qmuiteam.qmui.util.QMUIDirection
import com.qmuiteam.qmui.util.QMUIViewHelper
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_chat_file.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/02/15
 * Description:聊天文件页面
 */
@Route(path = AppRoute.CHAT_FILE)
class ChatFileActivity : DILoadableActivity() {

    private lateinit var chatFileListFragment: ChatFileListFragment
    private lateinit var chatMediaListFragment: ChatMediaListFragment
    private lateinit var scrollPagerAdapter: ScrollPagerAdapter
    var popupWindow: BottomPopupWindow? = null
    private var fragments = arrayListOf<Fragment>()
    private var titles = arrayListOf("", "")
    private var currentPos = 0
    private var logIds = arrayListOf<String>()
    private var messageItems = arrayListOf<ChatMessage>()

    private var fileLogIds = arrayListOf<String>()
    private var mediaLogIds = arrayListOf<String>()

    private val REQUEST_UPLOAD_FILE = 10
    private val REQUEST_FORWARD = 11

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: ChatFileViewModel

    @Autowired
    @JvmField
    var channelType: Int = Chat33Const.CHANNEL_FRIEND
    @Autowired
    @JvmField
    var targetId: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_file
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        viewModel.setChatTarget(channelType, targetId)

        logIds = viewModel.logIds
        fileLogIds = viewModel.fileLogIds
        mediaLogIds = viewModel.mediaLogIds
        messageItems = viewModel.messageItems

        viewModel.disableDelete.observe(this, Observer {
            if (it == true) {
                ll_delete.isEnabled = false
                iv_delete.setImageResource(R.mipmap.icon_batch_delete_disable)
            } else {
                ll_delete.isEnabled = true
                iv_delete.setImageResource(R.mipmap.icon_batch_delete)
            }
        })

        chatFileListFragment = ChatFileListFragment()
        chatMediaListFragment = ChatMediaListFragment()
        fragments.add(chatFileListFragment)
        fragments.add(chatMediaListFragment)
        scrollPagerAdapter = ScrollPagerAdapter(supportFragmentManager, titles, fragments)
        vp_file.adapter = scrollPagerAdapter
        vp_file.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                switchChoose(position)
                currentPos = position
                if (position == 0) {
                    if (!viewModel.selectable) {
                        iv_search.visibility = View.VISIBLE
                    }
                } else{
                    iv_search.visibility = View.GONE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        switchChoose(0)
        vp_file.currentItem = 0
    }

    override fun initData() {

    }

    override fun setEvent() {
        iv_back.setOnClickListener { onBackPressed() }
        tv_switch_choose.setOnClickListener {
            if (!animating) {
                switchSelectMode()
            }
        }
        tv_file.setOnClickListener { vp_file.currentItem = 0 }
        tv_media.setOnClickListener { vp_file.currentItem = 1 }
        iv_upload.setOnClickListener {
            startFilePicker()
        }
        iv_search.setOnClickListener {
            ARouter.getInstance().build(AppRoute.SEARCH_CHAT_FILE)
                    .withString("targetId", targetId)
                    .withInt("channelType", channelType)
                    .navigation()
        }
        ll_forward.setOnClickListener { forward(1) }
        ll_batch_forward.setOnClickListener { forward(2) }
        ll_download.setOnClickListener {
            if (messageItems.size == 0) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_file1))
                return@setOnClickListener
            }
            for (message in messageItems) {
                if (!message.msg.encryptedMsg.isNullOrEmpty()) {
                    continue
                }
                downloadFile(message)
            }
            switchSelectMode()
        }
        ll_delete.setOnClickListener {
            if (fileLogIds.size == 0) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_file3))
                return@setOnClickListener
            }
            showPopup()
        }
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
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_file5, data.state))
                }
                chatFileListFragment.refresh()
            } else {
                ShowUtils.showToastNormal(instance, it.apiException?.message)
            }
            switchSelectMode()
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
                            for (logId in fileLogIds) {
                                ChatDatabase.getInstance().chatMessageDao().mayGetMessageById(logId, channelType).run(Consumer {
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
                            switchSelectMode()
                        }
                    })
        }
        popupWindow?.showAtLocation(ll_select_options, Gravity.BOTTOM, 0, 0)
    }

    @AfterPermissionGranted(UPLOAD_IMAGE_PERMISSION)
    private fun startFilePicker() {
        if (PermissionUtil.hasWriteExternalPermission()) {
            FilePicker.from(instance)
                    .chooseForBrowser()
                    .setCompressImage(false)
                    .setMaxCount(9)
                    .requestCode(REQUEST_UPLOAD_FILE)
                    .start()
        } else {
            EasyPermissions.requestPermissions(instance, getString(R.string.chat_permission_storage),
                    UPLOAD_IMAGE_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun forward(forwardType: Int) {
        if (messageItems.size == 0) {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_file4))
            return
        }
        val type = if (channelType == Chat33Const.CHANNEL_ROOM) 1 else 2
        val request = PreForwardRequest(targetId, type, forwardType, messageItems)
        ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                .withSerializable("preForward", request)
                .navigation(this, REQUEST_FORWARD)
    }

    private fun switchSelectMode() {
        logIds.clear()
        messageItems.clear()
        mediaLogIds.clear()
        fileLogIds.clear()
        chatFileListFragment.notifyDataSetChanged()
        chatMediaListFragment.notifyDataSetChanged()
        if (viewModel.selectable) {
            iv_upload.visibility = View.VISIBLE
            if (currentPos == 0) {
                iv_search.visibility = View.VISIBLE
            }
            ll_select_options.visibility = View.GONE
            tv_switch_choose.text = getString(R.string.chat_action_choose)
            viewModel.selectable = false
            QMUIViewHelper.slideOut(ll_select_options, 500, animateListener, true, QMUIDirection.TOP_TO_BOTTOM)
        } else {
            iv_upload.visibility = View.GONE
            iv_search.visibility = View.GONE
            ll_select_options.visibility = View.VISIBLE
            tv_switch_choose.text = getString(R.string.chat_action_cancel)
            viewModel.selectable = true
            QMUIViewHelper.slideIn(ll_select_options, 500, animateListener, true, QMUIDirection.BOTTOM_TO_TOP)
        }
    }

    override fun onBackPressed() {
        if (viewModel.selectable) {
            switchSelectMode()
        } else {
            finish()
        }
    }

    private fun switchChoose(index: Int) {
        when (index) {
            0 -> {
                tv_file.setTextColor(ContextCompat.getColor(this, R.color.chat_color_title))
                tv_file.setBackgroundResource(R.drawable.shape_common_table)
                tv_media.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light))
                tv_media.setBackgroundResource(0)
            }
            1 -> {
                tv_file.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light))
                tv_file.setBackgroundResource(0)
                tv_media.setTextColor(ContextCompat.getColor(this, R.color.chat_color_title))
                tv_media.setBackgroundResource(R.drawable.shape_common_table)
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_UPLOAD_FILE) {
                // 文件选择回调
                val files = data!!.getParcelableArrayListExtra<EssFile>("extra_result_selection")
                for (file in files) {
                    val f = File(file.absolutePath)
                    val chatFile = ChatFile.newFile(f.absolutePath, f.name,
                            FileUtils.getLength(f.absolutePath), FileUtils.getFileMD5(f))
                    val message = ChatMessage.create(targetId, channelType, chatFile.chatFileType, 2, chatFile)
                    Chat33.getLocalCache().localPathMap[message.msgId] = f.absolutePath
                    uploadFile(targetId!!, file.absolutePath, message)
                }
            } else if (requestCode == REQUEST_FORWARD) {
                switchSelectMode()
            }
        }
    }

    fun sendMessage(sentMsg: ChatMessage) {
        viewModel.sendMessage(sentMsg)
        ShowUtils.showToastNormal(this, getString(R.string.chat_tips_chat_file6))
        vp_file.postDelayed({ chatFileListFragment.refresh() }, 1500)
    }

    private fun uploadFile(toId: String, filePath: String, message: ChatMessage) {
        val file = File(filePath)
        if (!file.exists()) {
            ShowUtils.showToast(instance, getString(R.string.chat_tips_chat_file7))
            return
        }
        if (FileUtils.getLength(filePath) > Chat33Const.MAX_UPLOAD_FILE_SIZE) {
            ShowUtils.showToast(instance, getString(R.string.chat_tips_chat_file8))
            return
        }
        loading(true)
        OssModel.getInstance().uploadMedia(message.toEncParams(), file.absolutePath, OssModel.FILE, object : OssModel.UpLoadCallBack {
            override fun onSuccess(url: String) {
                dismiss()
                message.msg.fileUrl = url
                sendMessage(message)
            }

            override fun onProgress(currentSize: Long, totalSize: Long) {

            }

            override fun onFailure(path: String) {
                dismiss()
                ShowUtils.showToast(instance, getString(R.string.chat_tips_chat_file9))
            }

        })
    }

    private fun downloadFile(message: ChatMessage) {
        val folder = File("${filesDir.path}/save")
        if (message.msg.localPath == null || !File(message.msg.localPath).exists() || !message.msg.localPath.contains(folder.absolutePath)) {
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val task = when {
                message.msgType == ChatMessage.Type.FILE -> DownloadTask.Builder(message.msg.fileUrl, folder)
                        .setFilename(message.msg.fileName)
                        .build()
                message.msgType == ChatMessage.Type.VIDEO -> DownloadTask.Builder(message.msg.mediaUrl, folder)
                        .setFilename("video_" + message.sendTime + "_" + message.senderId
                                + "." + FileUtils.getExtension(message.msg.mediaUrl))
                        .build()
                else -> DownloadTask.Builder(message.msg.imageUrl, folder)
                        .setFilename("image_" + message.sendTime + "_" + message.senderId
                                + "." + FileUtils.getExtension(message.msg.imageUrl))
                        .build()
            }
            val status = StatusUtil.getStatus(task)
            if (status == StatusUtil.Status.RUNNING) {
                return
            }
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
                    messageItems.remove(message)
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
