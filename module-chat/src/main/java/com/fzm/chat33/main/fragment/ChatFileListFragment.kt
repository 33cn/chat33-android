package com.fzm.chat33.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.*
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.base.LoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.InfoCacheBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.provider.ChatInfoStrategy
import com.fzm.chat33.core.provider.InfoProvider
import com.fzm.chat33.core.provider.OnFindInfoListener
import com.fzm.chat33.hepler.FileDownloadManager
import com.fzm.chat33.main.mvvm.ChatFileViewModel
import com.fzm.chat33.utils.FileUtils
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.google.gson.Gson
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_chat_file_list.*
import org.kodein.di.generic.instance
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

/**
 * @author zhengjy
 * @since 2019/02/15
 * Description:
 */
class ChatFileListFragment : DILoadableFragment() {

    companion object {
        private const val SAVE_FILE = 1
    }

    lateinit var chatFileAdapter: CommonAdapter<ChatMessage>
    var data = arrayListOf<ChatMessage>()
    var channelType: Int? = null
    var targetId: String? = null
    var nextLog = ""
    var checkStatus: SparseBooleanArray = SparseBooleanArray()
    var nameCache: SparseArray<String> = SparseArray()

    private val gson: Gson by instance()
    lateinit var viewModel: ChatFileViewModel
    var refresh = true

    override fun getLayoutId(): Int {
        return R.layout.fragment_chat_file_list
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel()
        channelType = viewModel.channelType
        targetId = viewModel.targetId
    }

    override fun initData() {
        rv_chat_file.layoutManager = LinearLayoutManager(activity)
        rv_chat_file.addItemDecoration(RecyclerViewDivider(activity, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_forward_divider_receive)))
        chatFileAdapter = object : CommonAdapter<ChatMessage>(activity, R.layout.item_chat_file, data) {
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
                holder?.setVisible(R.id.cb_select, viewModel.selectable)
                val tvFileName = holder?.getView<TextView>(R.id.tv_file_name)
                if (!TextUtils.isEmpty(message?.msg?.encryptedMsg)) {
                    tvFileName?.setText(R.string.chat_tips_encrypted_file)
                    tvFileName?.setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_light))
                    val drawable = ContextCompat.getDrawable(activity, R.drawable.ic_encrypted_lock)
                    drawable?.setBounds(0, 0, ScreenUtils.dp2px(20f), ScreenUtils.dp2px(20f))
                    tvFileName?.setCompoundDrawables(null, null, drawable, null)
                    tvFileName?.compoundDrawablePadding = 5
                } else {
                    tvFileName?.text = message?.msg?.fileName
                    tvFileName?.setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_dark))
                    tvFileName?.setCompoundDrawables(null, null, null, null)
                }
                holder?.setText(R.id.tv_file_date, ToolUtils.formatDay(message?.sendTime ?: 0L))
                holder?.setText(R.id.tv_file_size, ToolUtils.byte2Mb(message?.msg?.fileSize ?: 0L))
                holder?.setTag(R.id.cb_select, position)
                val cbSelect: CheckBox = holder?.getView(R.id.cb_select)!!
                cbSelect.isChecked = checkStatus.get(position)
                cbSelect.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.tag == position) {
                        checkStatus.put(position, isChecked)
                        if (message != null) {
                            if (isChecked) {
                                viewModel.logIds.add(message.logId)
                                viewModel.fileLogIds.add(message.logId)
                                viewModel.messageItems.add(message)
                            } else {
                                viewModel.logIds.remove(message.logId)
                                viewModel.fileLogIds.remove(message.logId)
                                viewModel.messageItems.remove(message)
                            }
                        }
                    }
                }
                if (channelType == Chat33Const.CHANNEL_ROOM) {
                    holder.setTextColor(R.id.tv_uploader, ContextCompat.getColor(activity, R.color.chat_color_accent))
                    if (!viewModel.selectable) {
                        holder.setOnClickListener(R.id.tv_uploader) {
                            ARouter.getInstance().build(AppRoute.GROUP_USER_FILE)
                                    .withString("roomId", message?.receiveId)
                                    .withString("userId", message?.senderId)
                                    .withString("name", nameCache.get(position))
                                    .navigation()
                        }
                    } else {
                        holder.setOnClickListener(R.id.tv_uploader) {
                            cbSelect.performClick()
                        }
                    }
                } else {
                    holder.setTextColor(R.id.tv_uploader, ContextCompat.getColor(activity, R.color.chat_text_grey_light))
                }
                InfoProvider.getInstance().strategy(ChatInfoStrategy(message)).load(object : OnFindInfoListener<InfoCacheBean> {
                    override fun onFindInfo(data: InfoCacheBean?, place: Int) {
                        nameCache.put(position, data?.displayName)
                        holder.setText(R.id.tv_uploader, data?.displayName)
                    }

                    override fun onNotExist() {
                        nameCache.put(position, getString(R.string.chat_tips_no_name))
                        holder.setText(R.id.tv_uploader, getString(R.string.chat_tips_no_name))
                    }
                })
                ChatDatabase.getInstance().chatMessageDao()
                        .getMessageById(message!!.logId, message.channelType).run(Consumer {
                            if (message.channelType == Chat33Const.CHANNEL_FRIEND) {
                                decryptFriendSingle(it)
                            } else if (message.channelType == Chat33Const.CHANNEL_ROOM) {
                                decryptGroupSingle(it)
                            }
                            message.msg = it?.msg
                        }, Consumer {
                            message.ignoreInHistory = 1
                        })
            }
        }
        chatFileAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                if (viewModel.selectable) {
                    val cb_select: View = view?.findViewById(R.id.cb_select)!!
                    cb_select.performClick()
                    return
                }
                if (!data[position].msg.encryptedMsg.isNullOrEmpty()) {
                    EasyDialog.Builder()
                            .setHeaderTitle(getString(R.string.chat_tips_tips))
                            .setContent(getString(R.string.chat_dialog_encrypt_chat_file))
                            .setBottomLeftText(getString(R.string.chat_action_confirm))
                            .setBottomLeftClickListener { it.dismiss() }.create(activity).show()
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
        statusLayout.showLoading()
        getChatFileList("", true)
    }

    @AfterPermissionGranted(SAVE_FILE)
    private fun doDownloadWork(position: Int) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            val folder = File("${activity.filesDir.path}/download/file")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            FileDownloadManager.INS.download(folder, data[position], object : FileDownloadManager.DownloadCallback {
                override fun onStart() {
                    ShowUtils.showSysToast(activity, getString(R.string.chat_tips_start_download))
                }

                override fun onAlreadyRunning() {
                    ShowUtils.showSysToast(activity, getString(R.string.chat_tips_downloading))
                }

                override fun onProgress(progress: Float) {

                }

                override fun onFinish(file: File?, throwable: Throwable?) {
                    if (file != null) {
                        data[position].msg.localPath = file.absolutePath
                        ShowUtils.showSysToast(activity, getString(R.string.chat_tips_file_download_success))
                    } else {
                        data[position].msg.localPath = null
                        ShowUtils.showSysToast(activity, getString(R.string.chat_tips_file_download_fail))
                    }
                    RoomUtils.run { ChatDatabase.getInstance().chatMessageDao().insert(data[position]) }
                }
            })
        } else {
            EasyPermissions.requestPermissions(activity, getString(R.string.chat_permission_storage), SAVE_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun setEvent() {
        swipeLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                getChatFileList("", true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getChatFileList(nextLog, false)
            }
        })
        viewModel.chatFiles.observe(this, Observer {
            val data = it.chatFileResponse
            if(data != null) {
                val list = data.logs
                if (list != null && list.size > 0) {
                    onGetChatFileSuccess(list, data.nextLog, refresh)
                } else {
                    onGetChatFileSuccess(ArrayList(), "-1", refresh)
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
        chatFileAdapter.notifyDataSetChanged()
    }

    fun refresh() {
        getChatFileList("", true)
    }

    private fun getChatFileList(startId: String, refresh: Boolean) {
        this.refresh = refresh
        viewModel.getChatFileList(startId, channelType!!)
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
        if (channelType == Chat33Const.CHANNEL_FRIEND) {
            decryptFriendMessage(list)
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            decryptGroupMessage(list)
        }
        data.addAll(list)
        if (data.size == 0) {
            statusLayout.showEmpty()
        } else {
            statusLayout.showContent()
        }
        chatFileAdapter.notifyDataSetChanged()
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
                    val fromKey = item.msg.fromKey
                    val toKey = item.msg.toKey
                    val chatFile = CipherManager.decryptString(item.msg.encryptedMsg!!, item.decryptPublicKey, CipherManager.getPrivateKey())
                    item.msg = gson.fromJson(chatFile, ChatFile::class.java)
                    item.msg.fromKey = fromKey
                    item.msg.toKey = toKey
                } catch (e: Exception) {
                    e.printStackTrace()
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