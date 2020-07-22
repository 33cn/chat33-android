package com.fzm.chat33.main.activity;

import android.Manifest;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter;
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.common.utils.KeyboardUtils;
import com.fuzamei.common.utils.PermissionUtil;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.common.widget.MultiStatusLayout;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.InfoCacheBean;
import com.fzm.chat33.core.db.bean.RoomKey;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.manager.CipherManager;
import com.fzm.chat33.core.provider.ChatInfoStrategy;
import com.fzm.chat33.core.provider.InfoProvider;
import com.fzm.chat33.core.provider.OnFindInfoListener;
import com.fzm.chat33.core.response.ChatListResponse;
import com.fzm.chat33.hepler.FileDownloadManager;
import com.fzm.chat33.main.mvvm.ChatFileViewModel;
import com.fzm.chat33.utils.FileUtils;
import com.fuzamei.componentservice.widget.dialog.DialogInterface;
import com.fuzamei.componentservice.widget.dialog.EasyDialog;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author zhengjy
 * @since 2019/02/20
 * Description:聊天文件搜索
 */
@Route(path = AppRoute.SEARCH_CHAT_FILE)
public class SearchChatFileActivity extends DILoadableActivity {

    private final int SAVE_FILE = 2;

    private EditText et_search;
    private View tv_cancel;
    private SmartRefreshLayout swipeLayout;
    private MultiStatusLayout statusLayout;
    private RecyclerView recyclerView;
    private CommonAdapter<ChatMessage> adapter;
    private List<ChatMessage> data = new ArrayList<>();

    private String keywords = "";
    private String nextLog = "";

    @Inject
    public Gson gson;
    @Inject
    public ViewModelProvider.Factory provider;
    private ChatFileViewModel viewModel;
    private boolean refresh = true;

    @Autowired
    public String targetId;
    @Autowired
    public int channelType;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search_chat_file;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        et_search = findViewById(R.id.et_search);
        tv_cancel = findViewById(R.id.tv_cancel);
        statusLayout = findViewById(R.id.statusLayout);
        swipeLayout = findViewById(R.id.swipeLayout);
        recyclerView = findViewById(R.id.recyclerView);
    }

    @Override
    protected void initData() {
        viewModel = ViewModelProviders.of(this, provider).get(ChatFileViewModel.class);
        viewModel.setChatTarget(channelType, targetId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(this, R.color.chat_forward_divider_receive)));
        adapter = new CommonAdapter<ChatMessage>(this, R.layout.item_chat_file, data) {
            @Override
            protected void convert(ViewHolder holder, ChatMessage message, int position) {
                String ext = FileUtils.getExtension(message.msg.fileName);
                switch (ext) {
                    case "doc":
                    case "docx":
                        holder.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_doc);
                        break;
                    case "pdf":
                        holder.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_pdf);
                        break;
                    case "xls":
                    case "xlsx":
                        holder.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_xls);
                        break;
                    case "mp3":
                    case "wma":
                    case "wav":
                    case "ogg":
                        holder.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_music);
                        break;
                    case "mp4":
                    case "avi":
                    case "rmvb":
                    case "flv":
                    case "f4v":
                    case "mpg":
                    case "mkv":
                    case "mov":
                        holder.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_video);
                        break;
                    default:
                        holder.setImageResource(R.id.iv_file_type, R.mipmap.icon_file_other);
                        break;
                }
                TextView tvFileName = holder.getView(R.id.tv_file_name);
                if (!TextUtils.isEmpty(message.msg.encryptedMsg)) {
                    tvFileName.setText(R.string.chat_tips_encrypted_file);
                    tvFileName.setTextColor(ContextCompat.getColor(instance, R.color.chat_text_grey_light));
                    Drawable drawable = ContextCompat.getDrawable(instance, R.mipmap.icon_my_edit);
                    drawable.setBounds(0, 0, ScreenUtils.dp2px(20f), ScreenUtils.dp2px(20f));
                    tvFileName.setCompoundDrawables(null, null, drawable, null);
                    tvFileName.setCompoundDrawablePadding(5);
                } else {
                    tvFileName.setText(message.msg.fileName);
                    tvFileName.setTextColor(ContextCompat.getColor(instance, R.color.chat_text_grey_dark));
                    tvFileName.setCompoundDrawables(null, null, null, null);
                }
                holder.setText(R.id.tv_file_date, ToolUtils.formatDay(message.sendTime));
                holder.setText(R.id.tv_file_size, ToolUtils.byte2Mb(message.msg.fileSize));
                InfoProvider.getInstance().strategy(new ChatInfoStrategy(message)).load(new OnFindInfoListener<InfoCacheBean>() {

                    @Override
                    public void onFindInfo(InfoCacheBean data, int place) {
                        holder.setText(R.id.tv_uploader, data.getDisplayName());
                    }

                    @Override
                    public void onNotExist() {
                        holder.setText(R.id.tv_uploader, getString(R.string.chat_tips_no_name));
                    }
                });
                RoomUtils.subscribe(ChatDatabase.getInstance().chatMessageDao()
                        .mayGetMessageById(message.logId, message.channelType), new Consumer<ChatMessage>() {
                    @Override
                    public void accept(ChatMessage bean) throws Exception {
                        message.msg = bean.msg;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        message.ignoreInHistory = 1;
                    }
                });
            }
        };
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (!TextUtils.isEmpty(data.get(position).msg.encryptedMsg)) {
                    new EasyDialog.Builder()
                            .setHeaderTitle(getString(R.string.chat_tips_tips))
                            .setContent(getString(R.string.chat_dialog_encrypt_chat_file))
                            .setBottomLeftText(getString(R.string.chat_action_confirm))
                            .setBottomLeftClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog) {
                                    dialog.dismiss();
                                }
                            }).create(instance).show();
                    return;
                }
                if (data.get(position).msg.getLocalPath() == null || !new File(data.get(position).msg.getLocalPath()).exists()) {
                    doDownloadWork(position);
                } else {
                    ARouter.getInstance().build(AppRoute.FILE_DETAIL).withSerializable("message", data.get(position)).navigation();
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @AfterPermissionGranted(SAVE_FILE)
    private void doDownloadWork(int position) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            File folder = new File(getFilesDir() + "/download/file");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            FileDownloadManager.INS.download(folder, data.get(position), new FileDownloadManager.DownloadCallback() {
                @Override
                public void onStart() {
                    ShowUtils.showSysToast(instance, getString(R.string.chat_tips_start_download));
                }

                @Override
                public void onAlreadyRunning() {
                    ShowUtils.showSysToast(instance, getString(R.string.chat_tips_downloading));
                }

                @Override
                public void onProgress(float progress) {

                }

                @Override
                public void onFinish(File file, Throwable throwable) {
                    if (file != null) {
                        data.get(position).msg.setLocalPath(file.getAbsolutePath());
                        ShowUtils.showSysToast(instance, getString(R.string.chat_tips_file_download_to, file.getAbsolutePath()));
                    } else {
                        data.get(position).msg.setLocalPath(null);
                        ShowUtils.showSysToast(instance, getString(R.string.chat_tips_file_download_fail));
                    }
                    RoomUtils.run(new Runnable() {
                        @Override
                        public void run() {
                            ChatDatabase.getInstance().chatMessageDao().insert(data.get(position));
                        }
                    });
                }
            });
        } else {
            EasyPermissions.requestPermissions(instance, instance.getString(R.string.chat_permission_storage), SAVE_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void setEvent() {
        swipeLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                searchChatFile(true);
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                searchChatFile(false);
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_search.getText().toString().trim().length() != 0) {
                    et_search.setText("");
                    data.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    finish();
                }
            }
        });
        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                statusLayout.showLoading();
                searchChatFile(true);
                KeyboardUtils.hideKeyboard(v);
            }
            return false;
        });
        viewModel.getChatFiles().observe(this, chatFileResult -> {
            ChatListResponse data = chatFileResult.getChatFileResponse();
            if(data != null) {
                List<ChatMessage> list = data.logs;
                if (list != null && list.size() > 0) {
                    onGetChatFileSuccess(list, data.nextLog, refresh);
                } else {
                    onGetChatFileSuccess(new ArrayList<ChatMessage>(), "-1", refresh);
                }
            } else {
                ShowUtils.showToast(instance, chatFileResult.getApiException() == null ? "" : chatFileResult.getApiException().getMessage());
                onGetChatFileSuccess(new ArrayList<ChatMessage>(), "-1", refresh);
            }
        });
    }

    private void searchChatFile(boolean refresh) {
        keywords = et_search.getText().toString().trim();
        this.refresh = refresh;
        if (TextUtils.isEmpty(keywords)) {
            return;
        }
        if (refresh) {
            nextLog = "";
        }
        viewModel.searchChatFiles(keywords, nextLog);
    }

    private void onGetChatFileSuccess(List<ChatMessage> list, String nextLog, boolean refresh) {
        this.nextLog = nextLog;
        if (refresh) {
            data.clear();
        }
        swipeLayout.finishRefresh();
        if ("-1".equals(nextLog)) {
            swipeLayout.finishLoadMoreWithNoMoreData();
        } else {
            swipeLayout.finishLoadMore();
        }
        if (channelType == Chat33Const.CHANNEL_FRIEND) {
            decryptFriendMessage(list);
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            decryptGroupMessage(list);
        }
        data.addAll(list);
        if (data.size() == 0) {
            statusLayout.showEmpty();
        } else {
            statusLayout.showContent();
        }
        adapter.notifyDataSetChanged();
    }

    private void decryptFriendMessage(List<ChatMessage> list) {
        for (ChatMessage item : list) {
            decryptFriendSingle(item);
        }
    }

    private void decryptFriendSingle(ChatMessage item) {
        if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
            item.encrypted = 1;
            if (!TextUtils.isEmpty(item.getDecryptPublicKey()) && !TextUtils.isEmpty(CipherManager.getPrivateKey())) {
                try {
                    String fromKey = item.msg.fromKey;
                    String toKey = item.msg.toKey;
                    String chatFile = CipherManager.decryptString(item.msg.encryptedMsg, item.getDecryptPublicKey(), CipherManager.getPrivateKey());
                    item.msg = gson.fromJson(chatFile, ChatFile.class);
                    item.msg.fromKey = fromKey;
                    item.msg.toKey = toKey;
                } catch (Exception e) {

                }
            }
        }
    }

    private void decryptGroupMessage(List<ChatMessage> list) {
        for (ChatMessage item : list) {
            decryptGroupSingle(item);
        }
    }

    private void decryptGroupSingle(ChatMessage item) {
        if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
            item.encrypted = 1;
            if (item.msg.kid != null) {
                try {
                    String kid = item.msg.kid;
                    RoomKey roomKey = ChatDatabase.getInstance().roomKeyDao().getRoomKeyById(item.receiveId, item.msg.kid);
                    String chatFile = CipherManager.decryptSymmetric(item.msg.encryptedMsg, roomKey.getKeySafe());
                    item.msg = gson.fromJson(chatFile, ChatFile.class);
                    item.msg.kid = kid;
                } catch (Exception e) {

                }
            }
        }
    }
}
