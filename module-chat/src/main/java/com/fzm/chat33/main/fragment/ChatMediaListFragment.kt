package com.fzm.chat33.main.fragment

import android.Manifest
import android.os.Bundle
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.*
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.ChatMessage.Type.IMAGE
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.main.mvvm.ChatFileViewModel
import com.fzm.chat33.utils.FileUtils
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.fzm.chat33.hepler.glide.GlideApp
import com.google.gson.Gson
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.qmuiteam.qmui.widget.QMUIProgressBar
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_chat_media_list.*
import org.kodein.di.generic.instance
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

/**
 * @author zhengjy
 * @since 2019/02/15
 * Description:
 */
class ChatMediaListFragment : DILoadableFragment() {

    companion object {
        private const val SAVE_MEDIA = 2
    }

    lateinit var chatMediaAdapter: CommonAdapter<ChatMessage>
    var data = arrayListOf<ChatMessage>()
    var channelType: Int = 0
    var targetId: String? = null
    var nextLog = ""
    var itemSize = 0
    var checkStatus: SparseBooleanArray = SparseBooleanArray()

    private val gson: Gson by instance()
    lateinit var viewModel: ChatFileViewModel
    var refresh = true

    override fun getLayoutId(): Int {
        return R.layout.fragment_chat_media_list
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel()
        channelType = viewModel.channelType
        targetId = viewModel.targetId
    }

    override fun initData() {
        itemSize = (activity.windowManager.defaultDisplay.width - ScreenUtils.dp2px(activity, 10.0f)) / 4
        val manager = GridLayoutManager(activity, 4)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return data[position].offset
            }
        }
        rv_chat_media.layoutManager = manager
        chatMediaAdapter = object : CommonAdapter<ChatMessage>(activity, R.layout.item_chat_media, data) {

            override fun getItemId(position: Int): Long {
                return data[position].hashCode().toLong()
            }

            override fun onViewRecycled(holder: ViewHolder) {
                super.onViewRecycled(holder)
                holder.setImageResource(R.id.iv_media, 0)
            }

            override fun convert(holder: ViewHolder?, message: ChatMessage?, position: Int) {
                val image: ImageView = holder?.getView(R.id.iv_media)!!
                val params = image.layoutParams
                params.height = itemSize
                params.width = itemSize
                image.layoutParams = params
                val container: View = holder.getView(R.id.rl_image)
                val params1 = container.layoutParams
                params1.width = itemSize
                container.layoutParams = params1
                if (position > 0) {
                    val lastDay = ToolUtils.formatDay(data[position - 1].sendTime)
                    val currentDay = ToolUtils.formatDay(data[position].sendTime)
                    if (lastDay == currentDay) {
                        if (position > 3) {
                            val time = ToolUtils.formatDay(data[position].sendTime)
                            if (ToolUtils.formatDay(data[position - 1].sendTime) == time
                                    && ToolUtils.formatDay(data[position - 2].sendTime) == time
                                    && ToolUtils.formatDay(data[position - 3].sendTime) == time
                                    && ToolUtils.formatDay(data[position - 4].sendTime) == time) {
                                // 前面至少四个都属于同组日期，则日期布局设置为gone，为了紧贴上一行
                                holder.setVisible(R.id.tv_date, false)
                            } else {
                                // 前面少于四个属于同组日期，则日期布局设置为invisible，为了对齐有日期的一项
                                holder.setInVisible(R.id.tv_date)
                            }
                        } else {
                            // 前面少于四个属于同组日期，则日期布局设置为invisible，为了对齐有日期的一项
                            holder.setInVisible(R.id.tv_date)
                        }
                    } else {
                        holder.setVisible(R.id.tv_date, true)
                        holder.setText(R.id.tv_date, ToolUtils.formatDay(message?.sendTime ?: 0L))
                    }
                } else {
                    holder.setVisible(R.id.tv_date, true)
                    holder.setText(R.id.tv_date, ToolUtils.formatDay(message?.sendTime ?: 0L))
                }
                holder.setTag(R.id.cb_select, position)
                holder.setVisible(R.id.cb_select, viewModel.selectable)
                val cbSelect: CheckBox = holder.getView(R.id.cb_select)!!
                cbSelect.isChecked = checkStatus.get(position)
                cbSelect.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.tag == position) {
                        checkStatus.put(position, isChecked)
                        if (message != null) {
                            if (isChecked) {
                                viewModel.logIds.add(message.logId)
                                viewModel.mediaLogIds.add(message.logId)
                                viewModel.messageItems.add(message)
                                viewModel.disableDelete.value = true
                            } else {
                                viewModel.logIds.remove(message.logId)
                                viewModel.mediaLogIds.remove(message.logId)
                                viewModel.messageItems.remove(message)
                                if (viewModel.mediaLogIds.size == 0) {
                                    viewModel.disableDelete.value = false
                                }
                            }
                        }
                    }
                }
                ChatDatabase.getInstance().chatMessageDao()
                        .getMessageById(message!!.logId, message.channelType).run(Consumer {
                            if (message.channelType == Chat33Const.CHANNEL_FRIEND) {
                                decryptFriendSingle(it)
                            } else if (message.channelType == Chat33Const.CHANNEL_ROOM) {
                                decryptGroupSingle(it)
                            }
                            message.msg = it?.msg
                            setupView(holder, message, image)
                        }, Consumer {
                            message.ignoreInHistory = 1
                            setupView(holder, message, image)
                        })
            }
        }
        chatMediaAdapter.setHasStableIds(true)
        chatMediaAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                if (!data[position].msg.encryptedMsg.isNullOrEmpty()) {
                    EasyDialog.Builder()
                            .setHeaderTitle(getString(R.string.chat_tips_tips))
                            .setContent(getString(
                                    if (data[position].msgType == IMAGE)
                                        R.string.chat_dialog_encrypt_chat_image
                                    else
                                        R.string.chat_dialog_encrypt_chat_video))
                            .setBottomLeftText(getString(R.string.chat_action_confirm))
                            .setBottomLeftClickListener { it.dismiss() }.create(activity).show()
                    return
                }
                if (data[position].msgType == ChatMessage.Type.VIDEO) {
                    if (data[position].msg.localPath == null || !File(data[position].msg.localPath).exists()) {
                        val pb_video: QMUIProgressBar = view!!.findViewById(R.id.pb_video)
                        val iv_video: ImageView = view.findViewById(R.id.iv_video)
                        doDownloadWork(pb_video, iv_video, position)
                    } else {
                        ARouter.getInstance().build(AppRoute.VIDEO_PLAYER)
                                .withSerializable("message", data[position])
                                .withString("videoUrl", data[position].msg.localPath)
                                .navigation()
                    }
                } else {
                    ARouter.getInstance().build(AppRoute.CHAT_MEDIA)
                            .withSerializable("mediaList", data)
                            .withInt("currentIndex", position).navigation()
                }
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }
        })
        rv_chat_media.adapter = chatMediaAdapter
        statusLayout.showLoading()
        getChatMediaList("", true)
    }

    @AfterPermissionGranted(SAVE_MEDIA)
    private fun doDownloadWork(pb_video: QMUIProgressBar, iv_video: ImageView, position: Int) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            val folder = File("${activity.filesDir.path}/download/video")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            if (data[position].msg.mediaUrl.isNullOrEmpty()) {
                return
            }
            val task = DownloadTask.Builder(data[position].msg.mediaUrl, folder)
                    .setFilename("${AppConfig.ENC_PREFIX}video_${data[position].sendTime}_${data[position].senderId}.${FileUtils.getExtension(data[position].msg.mediaUrl)}")
                    .build()
            val status = StatusUtil.getStatus(task)
            if (status == StatusUtil.Status.RUNNING) {
                ShowUtils.showSysToast(activity, getString(R.string.chat_tips_downloading))
                return
            }
            task.enqueue(object : DownloadListener1() {
                override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {
                    ShowUtils.showSysToast(activity, getString(R.string.chat_tips_start_download))
                    pb_video.maxValue = 100
                    if (pb_video.visibility == View.GONE) {
                        pb_video.visibility = View.VISIBLE
                    }
                    if (iv_video.visibility == View.VISIBLE) {
                        iv_video.visibility = View.GONE
                    }
                }

                override fun retry(task: DownloadTask, cause: ResumeFailedCause) {

                }

                override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {

                }

                override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                    pb_video.progress = (currentOffset * 1.0f / totalLength * 100).toInt()
                }

                override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                    pb_video.visibility = View.GONE
                    iv_video.visibility = View.VISIBLE
                    when (cause) {
                        EndCause.COMPLETED -> if (task.file != null) {
                            iv_video.setImageResource(R.drawable.icon_video_play)
                            data[position].msg.localPath = task.file!!.absolutePath
                            ShowUtils.showSysToast(activity, getString(R.string.chat_tips_video_download_success))
                        } else {
                            iv_video.setImageResource(R.drawable.icon_video_download)
                            data[position].msg.localPath = null
                            ShowUtils.showSysToast(activity, getString(R.string.chat_tips_video_download_fail))
                        }
                        else -> {
                            iv_video.setImageResource(R.drawable.icon_video_download)
                            data[position].msg.localPath = null
                            ShowUtils.showSysToast(activity, getString(R.string.chat_tips_video_download_fail))
                        }
                    }
                    RoomUtils.run { ChatDatabase.getInstance().chatMessageDao().insert(data[position]) }
                }
            })
        } else {
            EasyPermissions.requestPermissions(activity, getString(R.string.chat_permission_storage), SAVE_MEDIA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun setupView(holder: ViewHolder, message: ChatMessage, image: ImageView) {
        if (message.msg.encryptedMsg.isNullOrEmpty()) {
            if (message.msgType == ChatMessage.Type.VIDEO) {
                holder.setVisible(R.id.iv_video, true)
                holder.setVisible(R.id.tv_duration, true)
                if (message.msg.localPath == null || !File(message.msg.localPath).exists()) {
                    holder.setImageResource(R.id.iv_video, R.drawable.icon_video_download)
                } else {
                    holder.setImageResource(R.id.iv_video, R.drawable.icon_video_play)
                }
                holder.setText(R.id.tv_duration, ToolUtils.formatVideoDuration(message.msg.duration))
                GlideApp.with(activity).load(message)
                        .apply(RequestOptions().placeholder(R.drawable.bg_placeholder))
                        .into(image)
            } else {
                holder.setVisible(R.id.iv_video, false)
                holder.setVisible(R.id.tv_duration, false)
                GlideApp.with(activity).load(message)
                        .apply(RequestOptions().placeholder(R.drawable.bg_placeholder))
                        .into(image)
            }
        } else {
            holder.setVisible(R.id.iv_video, false)
            holder.setVisible(R.id.tv_duration, false)
            image.setImageResource(R.mipmap.ic_encrypt_media)
        }
    }

    override fun setEvent() {
        swipeLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                getChatMediaList("", true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getChatMediaList(nextLog, false)
            }
        })
        viewModel.chatMedias.observe(this, Observer {
            val data = it.chatFileResponse
            if(data != null) {
                val list = data.logs
                if (list != null && list.size > 0) {
                    onGetChatMediaSuccess(list, data.nextLog, refresh)
                } else {
                    onGetChatMediaSuccess(ArrayList(), "-1", refresh)
                }
            } else {
                swipeLayout.finishRefresh(0, false)
                swipeLayout.finishLoadMore(0, false, false)
                statusLayout.showError()
            }
        })
    }

    override fun fetchData() {

    }

    override fun destroyData() {

    }

    fun notifyDataSetChanged() {
        checkStatus.clear()
        chatMediaAdapter.notifyDataSetChanged()
    }

    fun refresh() {
        getChatMediaList("", true)
    }

    private fun getChatMediaList(startId: String, refresh: Boolean) {
        this.refresh = refresh
        viewModel.getChatMediaList(startId, channelType!!)
    }

    private fun onGetChatMediaSuccess(list: List<ChatMessage>, nextLog: String, refresh: Boolean) {
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
        if (channelType == Chat33Const.CHANNEL_FRIEND) {
            decryptFriendMessage(list)
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            decryptGroupMessage(list)
        }
        data.addAll(list)
        if (data.size == 0) {
            statusLayout.showEmpty()
        } else {
            var offset = 0
            for (position in data.indices) {
                if (position == data.size - 1) {
                    offset = (offset + 1) % 4
                    data[position].offset = 1
                } else {
                    val currentDay = ToolUtils.formatDay(data[position].sendTime)
                    val nextDay = ToolUtils.formatDay(data[position + 1].sendTime)
                    if (currentDay == nextDay) {
                        offset = (offset + 1) % 4
                        data[position].offset = 1
                    } else {
                        data[position].offset = 4 - offset
                        offset = 0
                    }
                }
            }
            statusLayout.showContent()
        }
        chatMediaAdapter.notifyDataSetChanged()
    }

    private fun decryptFriendMessage(list: List<ChatMessage>) {
        for (item in list) {
            decryptFriendSingle(item)
        }
    }

    private fun decryptFriendSingle(item: ChatMessage) {
        if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
            item.encrypted = 1
            if (!TextUtils.isEmpty(item.decryptPublicKey) && !TextUtils.isEmpty(CipherManager.getPrivateKey())) {
                try {
                    val chatFile = CipherManager.decryptString(item.msg.encryptedMsg!!, item.decryptPublicKey, CipherManager.getPrivateKey())
                    item.msg = gson.fromJson(chatFile, ChatFile::class.java)
                } catch (e: Exception) {

                }
            }
        }
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
}