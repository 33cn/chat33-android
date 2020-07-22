package com.fzm.chat33.main.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ess.filepicker.FilePicker;
import com.ess.filepicker.model.EssFile;
import com.fuzamei.common.bus.LiveBus;
import com.fuzamei.common.net.rxjava.ApiException;
import com.fuzamei.common.utils.ActivityUtils;
import com.fuzamei.common.utils.DateUtils;
import com.fuzamei.common.utils.KeyboardUtils;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.SharedPrefUtil;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.common.utils.task.Task;
import com.fuzamei.common.utils.task.TaskManager;
import com.fuzamei.common.widget.BottomPopupWindow;
import com.fuzamei.common.widget.GuideUserView;
import com.fuzamei.common.widget.IconView;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.app.BusEvent;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.config.AppPreference;
import com.fuzamei.componentservice.event.NicknameRefreshEvent;
import com.fuzamei.componentservice.helper.WeChatHelper;
import com.fzm.chat33.R;
import com.fzm.chat33.ait.AitManager;
import com.fzm.chat33.ait.activity.AitSelectorActivity;
import com.fzm.chat33.app.App;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.bean.GroupNotice;
import com.fzm.chat33.core.bean.MessageState;
import com.fzm.chat33.core.bean.RelationshipBean;
import com.fzm.chat33.core.bean.param.DecryptParams;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.RecentMessage;
import com.fzm.chat33.core.db.bean.RoomInfoBean;
import com.fzm.chat33.core.db.bean.RoomKey;
import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.db.dao.ChatMessageDao;
import com.fzm.chat33.core.event.BaseChatEvent;
import com.fzm.chat33.core.event.NewMessageEvent;
import com.fzm.chat33.core.event.NewMessageListEvent;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.EventReceiver;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.core.manager.CipherManager;
import com.fzm.chat33.core.manager.GroupKeyManager;
import com.fzm.chat33.core.manager.MessageManager;
import com.fzm.chat33.core.net.socket.ChatSocket;
import com.fzm.chat33.core.request.chat.PreForwardRequest;
import com.fzm.chat33.core.utils.UserInfoPreference;
import com.fzm.chat33.global.AppConst;
import com.fzm.chat33.hepler.ShareHelper;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.main.listener.AdapterItemClickListener;
import com.fzm.chat33.main.mvvm.ChatViewModel;
import com.fzm.chat33.utils.FileUtils;
import com.fzm.chat33.utils.PraiseUtil;
import com.fzm.chat33.widget.ChatInputView;
import com.fzm.chat33.widget.ImageGuidanceDialog;
import com.fzm.chat33.widget.SnapChatInputView;
import com.fuzamei.componentservice.widget.dialog.DialogInterface;
import com.fuzamei.componentservice.widget.dialog.EasyDialog;
import com.fzm.chat33.widget.popup.LuckREPopupWindow;
import com.fzm.chat33.widget.popup.MutePopupWindow;
import com.fzm.chat33.widget.popup.RedPacketOverduePopupWindow;
import com.google.gson.Gson;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static com.fuzamei.common.utils.SharedPrefUtil.READ_CURRENT_NOTICE;
import static com.fzm.chat33.core.utils.UserInfoPreference.SHOW_CHAT_GUIDANCE;
import static com.fzm.chat33.core.consts.PraiseAction.ACTION_LIKE;
import static com.fzm.chat33.core.consts.PraiseAction.ACTION_REWARD;
import static com.fzm.chat33.core.global.Chat33Const.*;
import static com.fzm.chat33.core.utils.UserInfoPreference.SHOW_TEXT_PACKET_GUIDANCE;
import static com.fzm.chat33.core.utils.UserInfoPreference.SHOW_UNENCRYPTED_TIPS;
import static com.qmuiteam.qmui.util.QMUIDirection.BOTTOM_TO_TOP;
import static com.qmuiteam.qmui.util.QMUIDirection.LEFT_TO_RIGHT;
import static com.qmuiteam.qmui.util.QMUIDirection.RIGHT_TO_LEFT;
import static com.qmuiteam.qmui.util.QMUIDirection.TOP_TO_BOTTOM;

/**
 * @author zhengjy
 * @since 2018/10/18
 * Description:聊天界面
 */
@Route(path = AppRoute.CHAT)
public class ChatActivity extends DILoadableActivity implements View.OnClickListener, AitManager.OnOpenAitListListener {

    private static final int PAGE_SIZE = 20;
    TextView titleTV, roomNum;
    SmartRefreshLayout swipeLayout;
    // "更多"操作
    private View ll_select_options, ll_forward, ll_batch_forward, ll_collect, ll_delete;

    View mPublicHide;
    View mPublicLayout;
    TextView mPublicContent;

    ChatInputView chatInputView;
    SnapChatInputView chat_input_snap;

    private GuideUserView guideView;
    ImageView chatBtnUser;
    LinearLayout rl_new_reward_up;
    LinearLayout rl_new_msg_up;
    LinearLayout rl_new_msg_down;
    TextView tv_new_reward_up;
    TextView tv_new_msg_up;
    TextView tv_new_msg_down;

    private int currentRoomNum = 0;

    private List<ChatMessage> chatList;
    private ListView mListView;
    private ChatListAdapter mAdapter;
    @Inject
    public Gson gson;
    @Inject
    public ViewModelProvider.Factory provider;
    private ChatViewModel viewModel;
    private boolean isLoadMore = false;
    private boolean seeNewMsg = false;

    private MutePopupWindow operationPopupWindow;

    private LuckREPopupWindow luckREPopupWindow;
    private RedPacketOverduePopupWindow overduePopupWindow;

    private String nextLog = "-1";
    private long timeStamp = Long.MAX_VALUE;

    @Inject
    public MessageManager manager;
    @Inject
    public ChatSocket chatSocket;

    @Autowired
    public int channelType;
    @Autowired
    public boolean isGroupChat = false;
    @Autowired
    public String targetId;
    @Autowired
    public String targetName;
    @Autowired
    public String fromLogId;
    // 删除好友或被移除群
    @Autowired
    public boolean isDeleted = false;
    @Autowired
    @Deprecated
    public String shareImage;
    // 是否为发送阅后即焚消息模式
    private int isSnap = 2;
    public boolean encryptMode = true;
    public String publicKey = "";
    public String groupKid = "";
    public String groupKey = "";

    private AitManager aitManager;

    private int csMessageType = 1;//如果是2则是系统消息
    private RoomInfoBean roomInfo;
    private int firstVisible = 0;
    private int lastVisible = 0;
    private int messageCountOut = 0;
    private int msgCount = 0;
    private int newMsgCount = 0;

    private List<String> imageOptions = new ArrayList<>();
    private ArrayList<RoomUserBean> roomUsers = new ArrayList<>();
    private int memberLevel = 1;

    IconView backButton;

    @Override
    protected void initView() {
        viewModel = ViewModelProviders.of(this, provider).get(ChatViewModel.class);
        titleTV = findViewById(R.id.title_tv);
        roomNum = findViewById(R.id.room_num);
        swipeLayout = findViewById(R.id.swipeLayout);
        mListView = findViewById(R.id.list);
        mPublicHide = findViewById(R.id.public_hide);
        mPublicLayout = findViewById(R.id.public_layout);
        mPublicContent = findViewById(R.id.public_content);
        chatInputView = findViewById(R.id.chat_input_bar);
        chat_input_snap = findViewById(R.id.chat_input_snap);
        chatBtnUser = findViewById(R.id.chat_btn_user);
        backButton = findViewById(R.id.toolbar_back_button);
        rl_new_reward_up = findViewById(R.id.rl_new_reward_up);
        rl_new_msg_up = findViewById(R.id.rl_new_msg_up);
        rl_new_msg_down = findViewById(R.id.rl_new_msg_down);
        tv_new_reward_up = findViewById(R.id.tv_new_reward_up);
        tv_new_msg_up = findViewById(R.id.tv_new_msg_up);
        tv_new_msg_down = findViewById(R.id.tv_new_msg_down);

        // "更多"操作，删除转发等
        ll_select_options = findViewById(R.id.ll_select_options);
        ll_forward = findViewById(R.id.ll_forward);
        ll_batch_forward = findViewById(R.id.ll_batch_forward);
        ll_collect = findViewById(R.id.ll_collect);
        ll_delete = findViewById(R.id.ll_delete);

        backButton.setOnClickListener(this);
        mPublicHide.setOnClickListener(this);
        chatBtnUser.setOnClickListener(this);
        rl_new_reward_up.setOnClickListener(this);
        rl_new_msg_up.setOnClickListener(this);
        rl_new_msg_down.setOnClickListener(this);
        ll_forward.setOnClickListener(this);
        ll_batch_forward.setOnClickListener(this);
        ll_collect.setOnClickListener(this);
        ll_delete.setOnClickListener(this);
        viewModel.getLoading().observe(this, this::setupLoading);
        viewModel.getChatLogResult().observe(this, it -> onGetChatLogByTimeSuccess(it.getChatList(), it.getNextLogId()));
        viewModel.getSendMessage().observe(this, it -> {
            ChatMessage message = it.getMessage();
            boolean resend = it.getResend();
            if (message.msgType == ChatMessage.Type.VIDEO || message.msgType == ChatMessage.Type.AUDIO) {
                if (!TextUtils.isEmpty(message.msg.getMediaUrl())) {
                    sendMessage(message, resend);
                }
            } else if (message.msgType == ChatMessage.Type.IMAGE) {
                if (!TextUtils.isEmpty(message.msg.getImageUrl())) {
                    sendMessage(message, resend);
                }
            } else if (message.msgType == ChatMessage.Type.FILE) {
                if (!TextUtils.isEmpty(message.msg.fileUrl)) {
                    sendMessage(message, resend);
                }
            } else {
                sendMessage(message, resend);
            }
        });
        viewModel.getRevokeMessage().observe(this, it -> {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_operate6));
            updateOutList();
        });
        viewModel.getSetMutedSingle().observe(this, it -> {
            if(it > 0) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_enable_mute));
            } else {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_operate1));
            }
        });
        viewModel.getReceiveRedPacket().observe(this, it -> {
            if(it.getSuccess()) {
                ChatMessage message = it.getMessage();
                message.msg.isOpened = true;
                updateMessage(message);
                openRedPacketDetail(message.msg.packetUrl, message.msg.packetId);
            } else {
                ChatMessage message = it.getMessage();
                ApiException exception = it.getApiException();
                int code = exception.getErrorCode();
                if (code == -4001) {
                    // 红包已领完
                    message.msg.redPacketStatus = 2;
                    openRedPacketDetail(message.msg.packetUrl, message.msg.packetId);
                } else if (code == -4009) {
                    // 红包已领取
                    message.msg.isOpened = true;
                    openRedPacketDetail(message.msg.packetUrl, message.msg.packetId);
                } else if (code == -4013) {
                    // 红包已过期
                    message.msg.redPacketStatus = 3;
                    handleOverdueRedPacket(message);
                }
                updateMessage(message);
            }
        });
        viewModel.getHasRelationship().observe(this, it -> {
            if(it.getSuccess()) {
                hasRelationshipSuccess(it.getRelationshipBean(), it.getOldDeleted());
            } else {
                hasRelationshipFail();
            }
        });
        viewModel.getGetRoomInfo().observe(this, it -> {
            if(it.getSuccess()) {
                getRoomInfoSuccess(it.getRoomInfoBean());
            } else {
                roomNum.setText("");
            }
        });
        viewModel.getGetRoomUsers().observe(this, it -> {
            final String roomId = it.getTargetId();
            roomUsers.clear();
            roomUsers.addAll(it.getWrapper().userList);
            Map<String, RoomUserBean> roomUsers = new HashMap<>();
            for (RoomUserBean roomUserBean : it.getWrapper().userList) {
                roomUserBean.roomId = roomId;
                roomUsers.put(roomUserBean.getId(), roomUserBean);
            }
            if (mAdapter != null) {
                mAdapter.setRoomUsers(roomUsers);
                mAdapter.notifyDataSetChanged();
            }
        });
        viewModel.getMsgLikeResult().observe(this, it -> {
            String logId = it.component1();
            String action = it.component2();
            if (ACTION_LIKE.equals(action)) {
                PraiseUtil.INSTANCE.showLike(instance);
                for (ChatMessage msg : chatList) {
                    if (msg.logId.equals(logId)) {
                        msg.praise.like();
                        mAdapter.notifyDataSetChanged();
                        RoomUtils.run(new Runnable() {
                            @Override
                            public void run() {
                                ChatDatabase.getInstance().chatMessageDao().insert(msg);
                            }
                        });
                        break;
                    }
                }
            }
        });
        viewModel.getNotifyChanged().observe(this, it -> {
            if (!isClick) {
                mAdapter.notifyDataSetChanged();
            } else {
                viewModel.notifyDataSetChanged(1000L);
            }
        });
    }

    @Override
    protected boolean enableSlideBack() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return R.layout.activity_chat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        ARouter.getInstance().inject(this);
        imageOptions.add(getString(R.string.chat_tips_shoot));
        imageOptions.add(getString(R.string.chat_tips_choose_from));

        init();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        channelType = intent.getIntExtra("channelType", CHANNEL_ROOM);
        isGroupChat = intent.getBooleanExtra("isGroupChat", false);
        targetId = intent.getStringExtra("targetId");
        targetName = intent.getStringExtra("targetName");
        fromLogId = intent.getStringExtra("fromLogId");
        isDeleted = intent.getBooleanExtra("isDeleted", false);
        shareImage = intent.getStringExtra("shareImage");
        msgCount = 0;
        newMsgCount = 0;
        init();
    }

    /**
     * 更新群的会话密钥
     */
    private void updateGroupKey() {
        disGroupKey = RoomUtils.subscribe(ChatDatabase.getInstance().roomKeyDao().loadLatestKey(targetId), new Consumer<RoomKey>() {
            @Override
            public void accept(RoomKey roomKey) throws Exception {
                groupKey = roomKey.getKeySafe();
                groupKid = roomKey.getKid();
            }
        });
    }

    private Disposable disGroupKey, disGroup, disFriend, disMsgCount, disReward;

    private void init() {
        RoomUtils.subscribe(ChatDatabase.getInstance().recentMessageDao()
                .getMsgCount(channelType, targetId), new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                msgCount = integer;
            }
        });
        manager.setChatTarget(channelType, targetId);
        isGroupChat = channelType == CHANNEL_ROOM;
        if (isGroupChat) {
            disGroup = RoomUtils.subscribe(ChatDatabase.getInstance().roomsDao().getRoomById(targetId), new Consumer<RoomListBean>() {
                @Override
                public void accept(RoomListBean roomListBean) throws Exception {
                    encryptMode = roomListBean.getEncrypt() == 1;
                    updateGroupKey();
                    targetName = roomListBean.getDisplayName();
                    titleTV.setText(targetName);
                }
            });
        } else {
            disFriend = RoomUtils.subscribe(ChatDatabase.getInstance().friendsDao().getFriendById(targetId), new Consumer<FriendBean>() {
                @Override
                public void accept(FriendBean friendBean) throws Exception {
                    targetName = friendBean.getDisplayName();
                    titleTV.setText(targetName);
                }
            });
        }
        disReward = RoomUtils.subscribe(ChatDatabase.getInstance().recentMessageDao().observeSingleMsg(channelType, targetId), new Consumer<RecentMessage>() {
            @Override
            public void accept(RecentMessage recent) throws Exception {
                int rewardNum = recent.getPraise().like + recent.getPraise().reward;
                if (rewardNum > 0) {
                    rl_new_reward_up.setVisibility(View.VISIBLE);
                    tv_new_reward_up.setText(getString(R.string.chat_unread_reward2, rewardNum));
                    if (recent.getPraise().reward > 0) {
                        rl_new_reward_up.setBackgroundResource(R.drawable.chat_left_round_bg_orange);
                        tv_new_reward_up.setTextColor(ContextCompat.getColor(instance, R.color.chat_reward_orange));
                    } else {
                        rl_new_reward_up.setBackgroundResource(R.drawable.chat_left_round_bg);
                        tv_new_reward_up.setTextColor(ContextCompat.getColor(instance, R.color.chat_color_accent));
                    }
                } else {
                    rl_new_reward_up.setVisibility(View.GONE);
                }
            }
        });

        chatList = new ArrayList<>();
        if (TextUtils.isEmpty(targetId)) {
            targetId = "";
        }

        // 输入框重置为初始状态
        isSnap = 2;
        chat_input_snap.setVisibility(View.GONE);
        chatInputView.setVisibility(View.VISIBLE);
        chatInputView.chatEditInput.requestFocus();
        chatInputView.setChannelType(channelType);

        initViews();
        initDatas();
        bindListener();
        attachKeyboardListeners();

        if (isGroupChat) {
            // 初始化@功能
            aitManager = new AitManager(targetId);
            chatInputView.addAitTextWatcher(aitManager);
            aitManager.setTextChangeListener(chatInputView);
            aitManager.setOnOpenAitListListener(this);
            mAdapter.setupAit(aitManager, chatInputView);
        }

        LiveBus.of(BusEvent.class).loginEvent().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean login) {
                if (login) {
                    chatInputView.getLogin().setVisibility(View.GONE);
                }
            }
        });
        LiveBus.of(BusEvent.class).nicknameRefresh().observe(this, new Observer<NicknameRefreshEvent>() {
            @Override
            public void onChanged(NicknameRefreshEvent event) {
                if (event.id != null) {
                    if (event.id.equals(targetId)) {
                        targetName = event.nickname;
                        titleTV.setText(targetName);
                    }
                    if (isGroupChat) {
                        for (int i = firstVisible; i <= lastVisible; i++) {
                            if (i >= chatList.size()) {
                                break;
                            }
                            if (event.id.equals(chatList.get(i).senderId)) {
                                mAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
            }
        });
        // 如果没显示过引导界面则需要显示
//        TaskManager.create()
//                .addTask(new Task() {
//                    @Override
//                    public void work() {
//                        if (UserInfoPreference.getInstance().getBooleanPref(SHOW_CHAT_GUIDANCE, true)) {
//                            Dialog dialog = new ImageGuidanceDialog(instance);
//                            dialog.setOnDismissListener(it -> {
//                                UserInfoPreference.getInstance().setBooleanPref(SHOW_CHAT_GUIDANCE, false);
//                                done();
//                            });
//                            dialog.show();
//                        } else {
//                            done();
//                        }
//                    }
//                })
//                .addTask(new Task() {
//                    @Override
//                    public void work() {
//                        if (UserInfoPreference.getInstance().getBooleanPref(SHOW_TEXT_PACKET_GUIDANCE, true)) {
//                            chatInputView.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    chatInputView.showOtherSendLayout();
//                                    View v = chatInputView.findViewById(R.id.iv_text_packet);
//                                    guideView = GuideUserView.show(R.id.confirm, new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            chatInputView.hideOtherSendLayout();
//                                            UserInfoPreference.getInstance().setBooleanPref(SHOW_TEXT_PACKET_GUIDANCE, false);
//                                            done();
//                                        }
//                                    }, instance, new GuideUserView.ViewEntity(v, R.layout.layout_text_packet_guidance, null));
//                                }
//                            }, 200);
//                        } else {
//                            done();
//                        }
//                    }
//                })
//                .start();
    }

    private void bindListener() {
        Chat33.registerEventReceiver(mEventReceiver);
    }

    private void unBindListener() {
        Chat33.unregisterEventReceiver(mEventReceiver);
    }

    private void initViews() {
        titleTV.setText(targetName);
        if (channelType == CHANNEL_GROUP) {
            // 聊天室
            chatBtnUser.setVisibility(View.INVISIBLE);
        } else if (channelType == CHANNEL_ROOM) {
            // 群聊
        } else if (channelType == CHANNEL_FRIEND) {

        }

        chatInputView.setData(this, iChatInputView);
        chat_input_snap.setSnapChatViewCallback(this, snapChatViewCallback);

    }

    private boolean upAnimating = false;
    private boolean downAnimating = false;

    private Animation.AnimationListener upListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            upAnimating = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener downListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            downAnimating = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    /**
     * 右上角显示未读消息按钮
     *
     * @param show
     */
    private void showNewMsgUp(boolean show) {
        if (show) {
            if (upAnimating) {
                return;
            }
            if (rl_new_msg_up.getVisibility() == View.GONE) {
                upAnimating = true;
                QMUIViewHelper.slideIn(rl_new_msg_up, 500, upListener, true, RIGHT_TO_LEFT);
            }
        } else {
            msgCount = 0;
            if (upAnimating) {
                return;
            }
            if (rl_new_msg_up.getVisibility() == View.VISIBLE) {
                upAnimating = true;
                QMUIViewHelper.slideOut(rl_new_msg_up, 500, upListener, true, LEFT_TO_RIGHT);
            }
        }
    }

    /**
     * 底部显示未读消息按钮
     *
     * @param show
     */
    private void showNewMsgDown(boolean show) {
        if (show) {
            if (downAnimating) {
                return;
            }
            if (rl_new_msg_down.getVisibility() == View.GONE) {
                downAnimating = true;
                QMUIViewHelper.slideIn(rl_new_msg_down, 500, downListener, true, BOTTOM_TO_TOP);
            }
        } else {
            newMsgCount = 0;
            if (downAnimating) {
                return;
            }
            if (rl_new_msg_down.getVisibility() == View.VISIBLE) {
                downAnimating = true;
                QMUIViewHelper.slideOut(rl_new_msg_down, 500, downListener, true, TOP_TO_BOTTOM);
            }
        }
    }

    @Override
    public void finish() {
        if (ActivityUtils.getActivityCount() == 1) {
            Chat33.getRouter().gotoMainPage();
        }
        super.finish();
    }

    private int oldScrollState = -1;
    // 用户是否点击消息
    private boolean isClick = false;

    private void initDatas() {
        swipeLayout.setEnableRefresh(true);
        swipeLayout.setEnableLoadMore(false);
        swipeLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isLoadMore = true;
                if ("-1".equals(nextLog)) {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_no_more_msg));
                    swipeLayout.finishRefresh();
                    return;
                }
                viewModel.getChatLogHistory(ChatActivity.this, targetId, channelType, nextLog, timeStamp, PAGE_SIZE);
            }
        });

        mListView.setSelector(R.color.chat_transparent);
//        mListView.setStackFromBottom(true);
        mListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (oldScrollState == SCROLL_STATE_TOUCH_SCROLL && scrollState == SCROLL_STATE_FLING) {
                    KeyboardUtils.hideKeyboard(getWindow().getDecorView());
                    chatInputView.hideOtherSendLayout();
                }
                oldScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstVisible = firstVisibleItem;
                lastVisible = firstVisibleItem + visibleItemCount - 1;
                // 当底部不可见消息不超过2条时，不显示新消息按钮
                if (lastVisible >= chatList.size() - 3) {
                    showNewMsgDown(false);
                }
                if (lastVisible != -1) {
                    if (firstVisible > chatList.size() - msgCount) {
                        if (chatList.size() > 2 * PAGE_SIZE) {
                            showNewMsgUp(false);
                        } else {
                            if (msgCount > 999) {
                                tv_new_msg_up.setText(getString(R.string.chat_tips_new_msg, "999+"));
                                showNewMsgUp(true);
                            } else if (msgCount > 0) {
                                tv_new_msg_up.setText(getString(R.string.chat_tips_new_msg, String.valueOf(msgCount)));
                                showNewMsgUp(true);
                            }
                        }
                    } else {
                        showNewMsgUp(false);
                    }
                }
                for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                    if (i >= chatList.size()) {
                        break;
                    }
                    if (chatList.get(i).snapVisible == 1) {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                        break;
                    }
                    if (i == firstVisibleItem + visibleItemCount - 1) {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }
                }
            }
        });
        mAdapter = new ChatListAdapter(chatList, ChatActivity.this, viewModel);
        mAdapter.setOnItemTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isClick = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        isClick = false;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return false;
            }
        });
        mAdapter.setGroupChat(isGroupChat);
        mAdapter.setListener(new AdapterItemClickListener() {
            @Override
            public void itemClick(Object item, int position, View view) {
                //此处是操作弹出框的点击事件
                final ChatMessage message = (ChatMessage) item;
                int i = view.getId();
                if (i == R.id.disable_speak) {
                    showOperation(message);
                } else if (i == R.id.enable_speak) {
                    viewModel.setMutedSingle(targetId, message.senderId, 0);
                } else if (i == R.id.communication_mode) {
                    AppPreference.INSTANCE.setSOUND_PLAY_MODE(AudioManager.MODE_IN_COMMUNICATION);
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_operate2));
                } else if (i == R.id.normal_mode) {
                    AppPreference.INSTANCE.setSOUND_PLAY_MODE(AudioManager.MODE_NORMAL);
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_chat_operate3));
                } else if (i == R.id.multi_choose) {
                    switchSelectMode();
                } else if (i == R.id.thumb) {
                    viewModel.messageLike(channelType, message.logId, ACTION_LIKE);
                } else if (i == R.id.reward) {
                    ARouter.getInstance().build(AppRoute.REWARD_PACKET)
                            .withInt("channelType", channelType)
                            .withString("logId", message.logId)
                            .navigation();
                } else if (i == R.id.copy) {
                    if (message.msgType == ChatMessage.Type.TEXT
                            || message.msgType == ChatMessage.Type.SYSTEM) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", message.msg.content);
                        if (cm != null) {
                            cm.setPrimaryClip(mClipData);
                            ShowUtils.showToastNormal(ChatActivity.this, getString(R.string.chat_tips_chat_operate4));
                        }

                    } else if (message.msgType == ChatMessage.Type.RED_PACKET) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", message.msg.redBagRemark);
                        if (cm != null) {
                            cm.setPrimaryClip(mClipData);
                            ShowUtils.showToastNormal(ChatActivity.this, getString(R.string.chat_tips_chat_operate4));
                        }
                    } else {
                        ShowUtils.showToastNormal(ChatActivity.this, getString(R.string.chat_tips_chat_operate5));
                    }
                } else if (i == R.id.revoke) {
                    // 撤回消息
                    viewModel.revokeMessage(message.logId, message.channelType == CHANNEL_FRIEND ? 2 : 1);
                } else if (i == R.id.forward) {
                    message.msg.setChatFileType(message.msgType);
                    ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                            .withSerializable("params", new DecryptParams(message))
                            .withSerializable("chatFile", message.msg)
                            .navigation();
                } else if (i == R.id.delete) {
                    final ChatMessage msg = (ChatMessage) item;
                    new EasyDialog.Builder()
                            .setHeaderTitle(getString(R.string.chat_tips_tips))
                            .setContent(getString(R.string.chat_dialog_delete))
                            .setBottomLeftText(getString(R.string.chat_action_cancel))
                            .setBottomRightText(getString(R.string.chat_action_delete))
                            .setBottomRightClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog) {
                                    dialog.dismiss();
                                    RoomUtils.run(new Runnable() {
                                        @Override
                                        public void run() {
                                            ChatDatabase.getInstance().chatMessageDao()
                                                    .fakeDeleteMessage(msg.channelType, msg.logId);
                                        }
                                    });
                                    chatList.remove(msg);
                                    mAdapter.notifyDataSetChanged();
                                    updateOutList();
                                }
                            }).create(instance).show();
                } else if (i == R.id.share) {
                    if (message.msgType == ChatMessage.Type.RED_PACKET) {
                        WeChatHelper.INS.shareWeb(message.msg.packetUrl, getString(R.string.chat_tip_packet_share_title, message.msg.coinName),
                                getString(R.string.chat_tip_packet_share_content), WeChatHelper.SESSION);
                    } else if (message.msgType == ChatMessage.Type.IMAGE) {
                        loading(true);
                        Glide.with(view).asBitmap().load(message.msg.getImageUrl()).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                dismiss();
                                WeChatHelper.INS.shareImage(resource, WeChatHelper.SESSION);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                dismiss();
                            }
                        });
                    } else if (message.msgType == ChatMessage.Type.VIDEO) {
                        ShareHelper.shareFile(instance, message);
                    } else if (message.msgType == ChatMessage.Type.FILE) {
                        WeChatHelper.INS.shareFile(message.msg.fileName, message.msg.getLocalPath(), WeChatHelper.SESSION);
                    } else {
                        WeChatHelper.INS.shareText(message.msg.content, WeChatHelper.SESSION);
                    }
                }
            }
        });
        mAdapter.setMessageCallBack(new ChatListAdapter.MessageCallBack() {
            @Override
            public void onRedBagClick(ChatMessage message) {
                //这里跳转一个弹框
                if (message.msg == null) {
                    return;
                }
                if (message.msg.isOpened) {
                    openRedPacketDetail(message.msg.packetUrl, message.msg.packetId);
                } else {
                    if (message.msg.redPacketStatus == 2
                            || message.msg.redPacketStatus == 4) {
                        openRedPacketDetail(message.msg.packetUrl, message.msg.packetId);
                    } else if (message.msg.redPacketStatus == 3) {
                        handleOverdueRedPacket(message);
                    } else {
                        openRedPacket(message);
                    }
                }
            }

            @Override
            public void onSnapMsgClick(ChatMessage message, int position) {
                if (mListView.getLastVisiblePosition() <= position) {
                    mListView.setSelection(position);
                }
            }

            @Override
            public void onResendClick(ChatMessage message) {
                viewModel.sendResendMessage(message);
            }

            @Override
            public void onVoiceStateChanged(boolean playing) {
                if (playing) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void onHideKeyboard() {
                chatInputView.hideOtherSendLayout();
                KeyboardUtils.hideKeyboard(getWindow().getDecorView());
            }
        });
        mListView.setAdapter(mAdapter);

        chatSocket.connect();
        if (TextUtils.isEmpty(fromLogId)) {
            viewModel.getChatLogHistory(ChatActivity.this, targetId, channelType, "", Long.MAX_VALUE, PAGE_SIZE);
        } else {
            viewModel.getChatLogFromId(ChatActivity.this, targetId, channelType, fromLogId);
        }
    }

    @Override
    protected void setEvent() {

    }

    private void openRedPacket(ChatMessage message) {
        if (luckREPopupWindow == null) {
            luckREPopupWindow = new LuckREPopupWindow(this, LayoutInflater.from(this)
                    .inflate(R.layout.popup_luck_red_envelopes, null), message);
            luckREPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } else {
            luckREPopupWindow.setMessage(message);
        }
        luckREPopupWindow.setOnOpenListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.receiveRedPacket(message);
            }
        });
        luckREPopupWindow.showAtLocation(mListView, Gravity.BOTTOM, 0, 0);
    }

    private void handleOverdueRedPacket(ChatMessage message) {
        if (isGroupChat) {
            openRedPacketDetail(message.msg.packetUrl, message.msg.packetId);
        } else {
            if (luckREPopupWindow != null && luckREPopupWindow.isShowing()) {
                luckREPopupWindow.dismiss();
            }
            if (overduePopupWindow == null) {
                overduePopupWindow = new RedPacketOverduePopupWindow(this,
                        LayoutInflater.from(this).inflate(R.layout.popup_red_packet_over_due, null));
            }
            overduePopupWindow.showAtLocation(mListView, Gravity.BOTTOM, 0, 0);
        }
    }

    private void openRedPacketDetail(String packetUrl, String packetId) {
        ARouter.getInstance().build("/app/redPacketInfo")
                .withString("packetUrl", packetUrl)
                .withString("packetId", packetId)
                .navigation();
        if (luckREPopupWindow != null && luckREPopupWindow.isShowing()) {
            luckREPopupWindow.dismiss();
        }
    }

    private void updateMessage(ChatMessage message) {
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().chatMessageDao().insert(message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void showOperation(final ChatMessage message) {
        operationPopupWindow = new MutePopupWindow(instance, LayoutInflater.from(instance).inflate(R.layout.popup_sustom_service_operation, null));
        operationPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        operationPopupWindow.setOnTimeSelectListener(new MutePopupWindow.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(long time) {
                viewModel.setMutedSingle(targetId, message.senderId, time);
            }
        });
        operationPopupWindow.showAtLocation(mListView, Gravity.BOTTOM, 0, 0);
    }

    final int REQUEST_LIST_CODE = 2;

    final int REQUEST_TAKE_CODE = 3;

    public static final int REQUEST_DEAD_TIME = 4;

    final int REQUEST_FORWARD = 5;

    final int REQUEST_FILE_PICK = 6;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == UPLOAD_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PictureSelector.create(instance)
                        .openGallery(PictureMimeType.ofAll())
                        .theme(R.style.chat_picture_style)
                        .maxSelectNum(9)
                        .imageSpanCount(4)
                        .previewImage(true)
                        .isCamera(false)
                        .forResult(REQUEST_LIST_CODE);
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_storage));
            }
        } else if (requestCode == ChatInputView.RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_record));
            }
        } else if (requestCode == TAKE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ARouter.getInstance().build(AppRoute.CAMERA_SHOOT).navigation(instance, REQUEST_TAKE_CODE);
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_universal));
            }
        } else if (requestCode == WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FilePicker.from(instance)
                        .chooseForBrowser()
                        .setCompressImage(false)
                        .setMaxCount(9)
                        .requestCode(REQUEST_FILE_PICK)
                        .start();
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_storage));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppPreference.INSTANCE.setCURRENT_TARGET_ID("");
        mAdapter.onPause();
    }

    private void showGroupNotice(GroupNotice notice) {
        if (isDeleted || mPublicLayout.getVisibility() == View.VISIBLE) {
            return;
        }
        mPublicLayout.setVisibility(View.VISIBLE);
        mPublicContent.setText(notice.getContent());
        mPublicContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 点击公告跳转到群公告页面
            }
        });
        myCount = new MyCount(CODE_TIME, 1000);
        myCount.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Chat33.checkService();
        AppPreference.INSTANCE.setCURRENT_TARGET_ID(targetId);
        viewModel.hasRelationship(channelType, targetId, isDeleted);
        try {
            int id = Integer.parseInt(targetId);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(id);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                Integer count = ChatDatabase.getInstance().recentMessageDao().getUnreadMsgCount(channelType, targetId);
                if (count != null && count != 0) {
                    // 清空未读消息计数
                    ChatDatabase.getInstance().recentMessageDao().clearUnreadMsg(channelType, targetId);
                }
                ChatDatabase.getInstance().recentMessageDao().clearAitMsg(channelType, targetId, false);
            }
        });
        disMsgCount = RoomUtils.subscribe(ChatDatabase.getInstance().recentMessageDao().getMsgCountOut(targetId), new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                messageCountOut = integer;
                if (!mAdapter.isSelectable()) {
                    if (messageCountOut > 999) {
                        backButton.setText(getString(R.string.icon_back_left) + " (...)");
                    } else if (messageCountOut > 0) {
                        backButton.setText(getString(R.string.icon_back_left) + " (" + messageCountOut + ")");
                    } else {
                        backButton.setText(R.string.icon_back_left);
                    }
                }
            }
        });
    }

    private void hasRelationshipSuccess(RelationshipBean relationship, boolean oldDelete) {
        if (relationship.isFriend || relationship.isInRoom) {
            isDeleted = false;
            if (!mAdapter.isSelectable()) {
                chatBtnUser.setVisibility(View.VISIBLE);
                chatInputView.setVisibility(View.VISIBLE);
            }
        } else {
            isDeleted = true;
            if (!mAdapter.isSelectable()) {
                chatBtnUser.setVisibility(isGroupChat ? View.INVISIBLE : View.VISIBLE);
                chatInputView.setVisibility(View.GONE);
            }
        }
        // 恢复上次进入的发言模式
        if (!mAdapter.isSelectable() && !isDeleted) {
            if (App.getInstance().snapModeList.contains(channelType + "-" + targetId)) {
                switchSnapMode(2);
            } else {
                switchSnapMode(1);
            }
        }
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                if (oldDelete != isDeleted) {
                    ChatDatabase.getInstance().recentMessageDao()
                            .markDelete(isDeleted, channelType, targetId);
                }
            }
        });
        setupRoomInfo();
    }

    private void hasRelationshipFail() {
        isDeleted = false;
        if (!mAdapter.isSelectable()) {
            chatBtnUser.setVisibility(View.VISIBLE);
            chatInputView.setVisibility(View.VISIBLE);
        }
        // 恢复上次进入的发言模式
        if (!mAdapter.isSelectable() && !isDeleted) {
            if (App.getInstance().snapModeList.contains(channelType + "-" + targetId)) {
                switchSnapMode(2);
            } else {
                switchSnapMode(1);
            }
        }
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().recentMessageDao()
                        .markDelete(isDeleted, channelType, targetId);
            }
        });
        setupRoomInfo();
    }

    private void setupRoomInfo() {
        if (isGroupChat && !isDeleted) {
            // 获取群内相关的信息
            viewModel.getRoomInfo(targetId);
            viewModel.getRoomUsers(targetId);
        } else {
            roomNum.setText("");
        }
    }

    private void getRoomInfoSuccess(RoomInfoBean bean) {
        roomInfo = bean;
        memberLevel = bean.getMemberLevel();
        mAdapter.setCanAddFriend(roomInfo.getCanAddFriend() == 1);
        mAdapter.setMemberLevel(roomInfo.getMemberLevel());
        currentRoomNum = roomInfo.getMemberNumber();
        roomNum.setText("(" + currentRoomNum + ")");
        if (bean.getDisableDeadline() > 0) {
            // 群被封禁
            String tips;
            if (AppConst.TIME_FOREVER == bean.getDisableDeadline()) {
                tips = getString(R.string.room_disable_forever_tips);
            } else {
                String deadline = DateUtils.timeToString(bean.getDisableDeadline(), getString(R.string.chat_date_pattern));
                tips = getString(R.string.room_disable_tips, deadline);
            }
            new EasyDialog.Builder()
                    .setHeaderTitle(getString(R.string.chat_tips_tips))
                    .setBottomRightText(getString(R.string.chat_action_confirm))
                    .setContent(tips)
                    .setBottomRightClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog) {
                            finish();
                        }
                    }).setCancelable(false).create(instance).show();
        }
        if (roomInfo.getSystemMsg() != null && roomInfo.getSystemMsg().number > 0) {
            String logId = roomInfo.getSystemMsg().list.get(0).getLogId();
            String tempLogId = SharedPrefUtil.getInstance().getStringPref(READ_CURRENT_NOTICE + UserInfo.getInstance().uid + targetId, "0");
            if (!logId.equals(tempLogId)) {
                try {
                    if (Integer.valueOf(logId) > Integer.valueOf(tempLogId)) {
                        SharedPrefUtil.getInstance().setStringPref(READ_CURRENT_NOTICE + UserInfo.getInstance().uid + targetId, logId);
                        showGroupNotice(roomInfo.getSystemMsg().list.get(0));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if (roomInfo.getMemberLevel() > 1) {
            chatInputView.getTvMuted().setText("");
            chatInputView.getTvMuted().setVisibility(View.GONE);
        } else {
            if (roomInfo.getRoomMutedType() == 1) {
                chatInputView.getTvMuted().setText("");
                chatInputView.getTvMuted().setVisibility(View.GONE);
            } else if (roomInfo.getRoomMutedType() == 2) {
                if (roomInfo.getMutedType() == 2) {
                    if (AppConst.TIME_FOREVER == roomInfo.getDeadline()) {
                        chatInputView.getTvMuted().setText(R.string.chat_tips_mute_state2);
                        chatInputView.getTvMuted().setVisibility(View.VISIBLE);
                    } else if (roomInfo.getDeadline() > System.currentTimeMillis()) {
                        mutedCount = new MutedCount(roomInfo.getDeadline() - System.currentTimeMillis(), 1000L);
                        mutedCount.start();
                    }
                } else {
                    chatInputView.getTvMuted().setText("");
                    chatInputView.getTvMuted().setVisibility(View.GONE);
                }
            } else if (roomInfo.getRoomMutedType() == 3) {
                if (roomInfo.getMutedType() == 3) {
                    chatInputView.getTvMuted().setText("");
                    chatInputView.getTvMuted().setVisibility(View.GONE);
                } else {
                    chatInputView.getTvMuted().setText(R.string.chat_tips_mute_state2);
                    chatInputView.getTvMuted().setVisibility(View.VISIBLE);
                }
            } else if (roomInfo.getRoomMutedType() == 4) {
                chatInputView.getTvMuted().setText(R.string.chat_tips_mute_state3);
                chatInputView.getTvMuted().setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (aitManager != null) {
            aitManager.onActivityResult(requestCode, resultCode, data);
        }
        // 图片选择结果回调
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == REQUEST_LIST_CODE) {
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    List<String> pathList = new ArrayList<>();
                    for (LocalMedia media : selectList) {
                        pathList.add(media.getPath());
                    }
                    for (final String path : pathList) {
                        String ext = FileUtils.getExtension(path);
                        if (IMAGE_TYPE.contains(ext)) {
                            int[] heightWidth = ToolUtils.getLocalImageHeightWidth(path);
                            ChatFile chatFile = ChatFile.newImage(path, heightWidth[0], heightWidth[1]);
                            ChatMessage message = ChatMessage.create(targetId, channelType, chatFile.getChatFileType(), isSnap, chatFile);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatList.add(message);
                                    mAdapter.notifyDataSetChanged();
                                    smoothScrollToBottom();
                                }
                            });
                            Chat33.getLocalCache().localPathMap.put(message.getMsgId(), path);
                            viewModel.uploadImage(ChatActivity.this, path, message);
                        } else if (VIDEO_TYPE.contains(ext)) {
                            long duration = (long) (selectList.get(pathList.indexOf(path)).getDuration() / 1000 + 0.5f);
                            Bitmap thumb = ToolUtils.getVideoPhoto(path);
                            ChatFile chatFile = ChatFile.newVideo((int) duration, path, thumb.getHeight(), thumb.getWidth());
                            thumb.recycle();
                            ChatMessage message = ChatMessage.create(targetId, channelType, chatFile.getChatFileType(), 2, chatFile);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatList.add(message);
                                    mAdapter.notifyDataSetChanged();
                                    smoothScrollToBottom();
                                }
                            });
                            Chat33.getLocalCache().localPathMap.put(message.getMsgId(), path);
                            viewModel.uploadVideo(ChatActivity.this, path, message);
                        } else {
                            File file = new File(path);
                            ChatFile chatFile = ChatFile.newFile(path, file.getName(),
                                    FileUtils.getLength(path), FileUtils.getFileMD5(file));
                            ChatMessage message = ChatMessage.create(targetId, channelType, chatFile.getChatFileType(), 2, chatFile);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatList.add(message);
                                    mAdapter.notifyDataSetChanged();
                                    smoothScrollToBottom();
                                }
                            });
                            Chat33.getLocalCache().localPathMap.put(message.getMsgId(), path);
                            viewModel.uploadFile(ChatActivity.this, path, message);
                        }
                    }
                } else if (requestCode == REQUEST_TAKE_CODE) {
                    String imageUrl = data.getStringExtra("result");
                    String videoUrl = data.getStringExtra("video");
                    long duration = data.getLongExtra("duration", 0);
                    if (imageUrl != null) {
                        int[] heightWidth = ToolUtils.getLocalImageHeightWidth(imageUrl);
                        ChatFile chatFile = ChatFile.newImage(imageUrl, heightWidth[0], heightWidth[1]);
                        ChatMessage message = ChatMessage.create(targetId, channelType, chatFile.getChatFileType(), isSnap, chatFile);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatList.add(message);
                                mAdapter.notifyDataSetChanged();
                                smoothScrollToBottom();
                            }
                        });
                        Chat33.getLocalCache().localPathMap.put(message.getMsgId(), imageUrl);
                        viewModel.uploadImage(ChatActivity.this, imageUrl, message);
                    } else if (videoUrl != null) {
                        Bitmap thumb = ToolUtils.getVideoPhoto(videoUrl);
                        ChatFile chatFile = ChatFile.newVideo((int) duration, videoUrl, thumb.getHeight(), thumb.getWidth());
                        thumb.recycle();
                        ChatMessage message = ChatMessage.create(targetId, channelType, chatFile.getChatFileType(), 2, chatFile);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatList.add(message);
                                mAdapter.notifyDataSetChanged();
                                smoothScrollToBottom();
                            }
                        });
                        viewModel.uploadVideo(ChatActivity.this, videoUrl, message);
                    }
                } else if (requestCode == REQUEST_DEAD_TIME) {
                    final long destroyTime = data.getLongExtra("destroyTime", 0);
                    final String logId = data.getStringExtra("logId");
                    if (destroyTime != 0) {
                        RoomUtils.run(new Runnable() {
                            @Override
                            public void run() {
                                ChatDatabase.getInstance().chatMessageDao()
                                        .updateDestroyTime(destroyTime, channelType, logId);
                            }
                        });
                        for (ChatMessage msg : chatList) {
                            if (msg.logId.equals(logId)) {
                                msg.destroyTime = destroyTime;
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else if (requestCode == REQUEST_FILE_PICK) {
                    // 文件选择回调
                    ArrayList<EssFile> files = data.getParcelableArrayListExtra("extra_result_selection");
                    for (final EssFile file : files) {
                        File f = new File(file.getAbsolutePath());
                        ChatFile chatFile = ChatFile.newFile(f.getAbsolutePath(), f.getName(),
                                FileUtils.getLength(f.getAbsolutePath()), FileUtils.getFileMD5(f));
                        ChatMessage message = ChatMessage.create(targetId, channelType, chatFile.getChatFileType(), 2, chatFile);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatList.add(message);
                                mAdapter.notifyDataSetChanged();
                                smoothScrollToBottom();
                            }
                        });
                        Chat33.getLocalCache().localPathMap.put(message.getMsgId(), f.getAbsolutePath());
                        viewModel.uploadFile(ChatActivity.this, file.getAbsolutePath(), message);
                    }
                }
            } else {
                if (requestCode == REQUEST_FORWARD) {
                    switchSelectMode();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unBindListener();
        if (mAdapter != null) {
            mAdapter.destroy();
        }
        if (aitManager != null) {
            aitManager.reset();
        }
        if (manager != null) {
            manager.dispose();
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        if (disGroupKey != null && !disGroupKey.isDisposed()) {
            disGroupKey.dispose();
        }
        if (disGroup != null && !disGroup.isDisposed()) {
            disGroup.dispose();
        }
        if (disFriend != null && !disFriend.isDisposed()) {
            disFriend.dispose();
        }
        if (disMsgCount != null && !disMsgCount.isDisposed()) {
            disMsgCount.dispose();
        }
        if (disReward != null && !disReward.isDisposed()) {
            disReward.dispose();
        }
        if (praiseMsg != null && !praiseMsg.isDisposed()) {
            praiseMsg.dispose();
        }

        if (myCount != null) {
            myCount.cancel();
            myCount = null;
        }

        if (mutedCount != null) {
            mutedCount.cancel();
            mutedCount = null;
        }
    }

    private boolean containMessage(ChatMessage message, boolean isRefresh) {
        for (ChatMessage item : chatList) {
            if (item.getLogId().equals(message.getLogId())) {
                return true;
            }
            if (isRefresh) {
                //这里同时更新昵称和头像
                if (!TextUtils.isEmpty(message.senderId) && !TextUtils.isEmpty(item.senderId)
                        && message.senderId.equals(item.senderId)) {
//                    if (!TextUtils.isEmpty(message.avatar)) {
//                        item.avatar = message.avatar;
//                    }
//                    if (!TextUtils.isEmpty(message.name)) {
//                        item.name = message.name;
//                    }
                }
            }
        }
        return false;
    }

    private void smoothScrollToBottom() {
        if (chatList != null && chatList.size() > 0) {
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.smoothScrollToPosition(chatList.size() - 1);
                }
            });
        }
    }

    private void scrollToBottom() {
        if (chatList != null && chatList.size() > 0) {
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.setSelection(chatList.size() - 1);
                }
            });
        }
    }

    private EventReceiver mEventReceiver = new EventReceiver() {
        @SuppressLint("CheckResult")
        @Override
        public void onReceiveEvent(BaseChatEvent event) {
            switch (event.eventType) {
                case MSG_NORMAL_MESSAGE:
                    if (!(event instanceof NewMessageEvent)) {
                        break;
                    }
                    ChatMessage item = ((NewMessageEvent) event).message;
                    String id;
                    if (item.channelType == CHANNEL_ROOM) {
                        id = item.receiveId;
                    } else {
                        if (item.senderId != null && item.senderId.equals(UserInfo.getInstance().id)) {
                            id = item.receiveId;
                        } else {
                            id = item.senderId;
                        }
                    }
                    if (!targetId.equals(id) || channelType != item.channelType) {
                        // 如果不是当前会话消息则不处理
                        return;
                    }
                    if (viewModel.isLocalBlock(id) && !item.isSentType()) {
                        // 如果是收到黑名单消息则不处理
                        return;
                    }
                    if (item.msgType == ChatMessage.Type.NOTIFICATION) {
                        // 处理通知消息
                        if (item.msg.type == REVOKE_MSG) {
                            // 撤回消息
                            RoomUtils.run(new Runnable() {
                                @Override
                                public void run() {
                                    ChatDatabase.getInstance().chatMessageDao()
                                            .deleteMessage(channelType, item.msg.logId);
                                }
                            });
                            for (ChatMessage message : chatList) {
                                if (item.msg.logId.equals(message.logId)) {
                                    chatList.remove(message);
                                    break;
                                }
                            }
                        } else if (item.msg.type == EXIT_GROUP || item.msg.type == KICK_OUT) {
                            currentRoomNum--;
                            roomNum.setText("(" + currentRoomNum + ")");
                        } else if (item.msg.type == JOIN_GROUP) {
                            currentRoomNum++;
                            roomNum.setText("(" + currentRoomNum + ")");
                        } else if (item.msg.type == CHANGE_GROUP_NAME) {
                            // 更改群名
                            targetName = item.msg.roomName;
                            titleTV.setText(targetName);
                            RoomUtils.run(new Runnable() {
                                @Override
                                public void run() {
                                    ChatDatabase.getInstance().roomsDao().updateName(targetName, targetId);
                                    LiveBus.of(BusEvent.class).contactsRefresh().setValue(2);
                                }
                            });
                        } else if (item.msg.type == SNAP_DESTROY) {
                            if (!ChatMessageDao.visibleSnapMsg.contains(channelType + "-" + item.msg.logId)) {
                                for (ChatMessage message : chatList) {
                                    if (item.msg.logId.equals(message.logId)) {
                                        chatList.remove(message);
                                        break;
                                    }
                                }
                            } else {
                                ChatMessageDao.visibleSnapMsg.remove(channelType + "-" + item.msg.logId);
                            }
                        } else if (item.msg.type == RECEIPT_SUCCESS) {
                            for (int i = chatList.size() - 1; i >= 0; i--) {
                                if (item.msg.logId.equals(chatList.get(i).logId)) {
                                    chatList.get(i).msg.recordId = item.msg.recordId;
                                    break;
                                }
                            }
                        } else if (item.msg.type == UPDATE_GROUP_KEY) {
                            // 更新群密钥
                            Observable.just(0)
                                    .subscribeOn(Schedulers.io())
                                    .map(integer -> {
                                        int decrypt = 0;
                                        if (isGroupChat) {
                                            for (int i = chatList.size() - 1; i >= 0; i--) {
                                                if (!TextUtils.isEmpty(chatList.get(i).msg.encryptedMsg)) {
                                                    if (chatList.get(i).msg.kid.equals(item.msg.kid)
                                                            && targetId.equals(item.msg.roomId)) {
                                                        RoomKey roomKey = ChatDatabase.getInstance().roomKeyDao()
                                                                .getRoomKeyById(item.receiveId, item.msg.kid);
                                                        String str = CipherManager.decryptSymmetric(chatList.get(i).msg.encryptedMsg,
                                                                roomKey.getKeySafe());
                                                        chatList.get(i).msg = gson.fromJson(str, ChatFile.class);
                                                        decrypt++;
                                                    }
                                                }
                                            }
                                        }
                                        return decrypt;
                                    })
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(integer -> {
                                        if (integer > 0) {
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }, Throwable::printStackTrace);
                        } else if (item.msg.type == FRIEND_REJECT_MSG) {
                            // 消息被拒收
                            for (int i = chatList.size() - 1; i >= 0; i--) {
                                if (item.msg.logId.equals(chatList.get(i).logId)) {
                                    chatList.get(i).messageState = MessageState.SEND_FAIL;
                                    break;
                                }
                            }
                        } else if (item.msg.type == PRAISE_MESSAGE) {
                            praiseMessage(item);
//                            return;
                        }
                    }
                    if (!item.isSentType()) {
                        // 如果不是本人发的则直接处理
                        // 处理发送消息的回执消息
                        if (containMessage(item, true)) {
                            //todo
                        } else {
                            //同账号其他设备发送的消息，同步本设备
                            int mType = item.msgType;
                            if (mType == ChatMessage.Type.SYSTEM) {
                                SharedPrefUtil.getInstance().setStringPref(READ_CURRENT_NOTICE + UserInfo.getInstance().uid + targetId, item.logId);
                            }
                            boolean scrollToBottom = true;
                            // 当底部不可见消息不超过2条时，自动滚动消息列表
                            if (lastVisible < chatList.size() - 3) {
                                scrollToBottom = false;
                            }
//                            if (mListView.canScrollVertically(1)) {
//                                // 如果当前不在底部，则受到新消息不滚动
//                                scrollToBottom = false;
//                            }
                            if (item.shouldSave()) {
                                chatList.add(item);
                            }
                            mAdapter.notifyDataSetChanged();
                            if (scrollToBottom && item.shouldSave()) {
                                smoothScrollToBottom();
                            } else {
                                if (item.shouldSave()) {
                                    // 消息焚毁通知不计入内
                                    newMsgCount++;
                                }
                                if (newMsgCount > 999) {
                                    tv_new_msg_down.setText(getString(R.string.chat_tips_new_msg, "999+"));
                                } else if (newMsgCount > 0) {
                                    tv_new_msg_down.setText(getString(R.string.chat_tips_new_msg, String.valueOf(newMsgCount)));
                                }
                                if (newMsgCount > 0) {
                                    showNewMsgDown(true);
                                }
                            }
                        }
                    } else {
                        // 如果是自己发送的消息
                        int mType = item.msgType;
                        if (mType == ChatMessage.Type.SYSTEM
                                || mType == ChatMessage.Type.NOTIFICATION) {
                            if (!item.shouldSave()) {
                                if (item.msg.type != Chat33Const.PRAISE_MESSAGE) {
                                    viewModel.notifyDataSetChanged();
                                }
                                // 如果是焚毁通知则不操作
                                return;
                            }
                            if (mType == ChatMessage.Type.SYSTEM) {
                                SharedPrefUtil.getInstance().setStringPref(READ_CURRENT_NOTICE + UserInfo.getInstance().uid + targetId, item.logId);
                            }
                            boolean scrollToBottom = true;
                            // 当底部不可见消息不超过2条时，自动滚动消息列表
                            if (lastVisible < chatList.size() - 3) {
                                scrollToBottom = false;
                            }
//                            if (mListView.canScrollVertically(1)) {
//                                // 如果当前不在底部，则受到新消息不滚动
//                                scrollToBottom = false;
//                            }
                            if (item.shouldSave()) {
                                chatList.add(item);
                            }
                            mAdapter.notifyDataSetChanged();
                            if (scrollToBottom && item.shouldSave()) {
                                smoothScrollToBottom();
                            } else {
                                if (item.shouldSave()) {
                                    // 消息焚毁通知不计入内
                                    newMsgCount++;
                                }
                                if (newMsgCount > 999) {
                                    tv_new_msg_down.setText(getString(R.string.chat_tips_new_msg, "999+"));
                                } else if (newMsgCount > 0) {
                                    tv_new_msg_down.setText(getString(R.string.chat_tips_new_msg, String.valueOf(newMsgCount)));
                                }
                                if (newMsgCount > 0) {
                                    showNewMsgDown(true);
                                }
                            }
                        } else {
                            boolean fromOtherTerminal = true;
                            if (!TextUtils.isEmpty(item.getMsgId())) {
                                for (int i = chatList.size() - 1; i >= 0; i--) {
                                    if (!TextUtils.isEmpty(chatList.get(i).getMsgId()) && chatList.get(i).getMsgId().equals(item.getMsgId())) {
                                        chatList.get(i).messageState = MessageState.SEND_SUCCESS;
                                        chatList.get(i).logId = item.logId;
                                        chatList.get(i).receiveId = item.receiveId;
                                        chatList.get(i).msg.fromKey = item.msg.fromKey;
                                        chatList.get(i).msg.toKey = item.msg.toKey;
                                        chatList.get(i).msg.kid = item.msg.kid;
                                        if (item.msgType == ChatMessage.Type.TRANSFER) {
                                            chatList.get(i).msg.recordId = item.msg.recordId;
                                        }
                                        fromOtherTerminal = false;
                                        break;
                                    }
                                }
                                if (fromOtherTerminal) {
                                    if (item.shouldSave()) {
                                        chatList.add(item);
                                    }
                                }
                                mAdapter.notifyDataSetChanged();
                                smoothScrollToBottom();
                            }
                        }
                    }
                    break;
                case MSG_BANNED_GROUP:// 群聊被封禁
                    if (event.msg.deadline == 0) {
                        // 解封通知
                        return;
                    }
                    if (targetId.equals(event.msg.roomId)) {
                        new EasyDialog.Builder()
                                .setHeaderTitle(getString(R.string.chat_tips_tips))
                                .setBottomRightText(getString(R.string.chat_action_confirm))
                                .setContent(event.msg.content)
                                .setBottomRightClickListener(dialog -> {
                                    dialog.dismiss();
                                    finish();
                                }).setCancelable(false).create(instance).show();
                    }
                    break;
                case MSG_ENTER_GROUP:// 入群通知，更新输入框状态
                    if (targetId.equals(event.msg.roomId)) {
                        isDeleted = false;
                        chatBtnUser.setVisibility(isDeleted ? (channelType != CHANNEL_FRIEND ? View.INVISIBLE : View.VISIBLE) : View.VISIBLE);
                        chatInputView.setVisibility(isDeleted ? View.GONE : View.VISIBLE);
                        chatInputView.getTvMuted().setVisibility(View.GONE);
                    }
                    break;
                case MSG_EXIT_GROUP:// 退出群聊
                    if (targetId.equals(event.msg.roomId) && ActivityUtils.isActivityTop(instance, ChatActivity.class)) {
                        ShowUtils.showToastNormal(instance, event.msg.type == 1 ? getString(R.string.chat_main_tips1) : getString(R.string.chat_main_tips2));
                        finish();
                    }
                    break;
                case MSG_DISMISS_GROUP:// 解散群聊
                    if (targetId.equals(event.msg.roomId)) {
                        ShowUtils.showToastNormal(instance, getString(R.string.chat_main_tips3));
                        finish();
                    }
                    break;
                case MSG_GROUP_MUTE:// 群中被禁言、解禁通知
                    if (isGroupChat && targetId.equals(event.msg.roomId)) {
                        if (roomInfo == null || roomInfo.getMemberLevel() == 1) {
                            if (event.msg.type == 1) {
                                // 禁言
                                if (AppConst.TIME_FOREVER == event.msg.deadline) {
                                    chatInputView.getTvMuted().setText(R.string.chat_tips_mute_state2);
                                    chatInputView.getTvMuted().setVisibility(View.VISIBLE);
                                } else if (event.msg.deadline > System.currentTimeMillis()) {
                                    mutedCount = new MutedCount(event.msg.deadline - System.currentTimeMillis(), 1000L);
                                    mutedCount.start();
                                }
                            } else {// 解禁
                                chatInputView.getTvMuted().setText("");
                                chatInputView.getTvMuted().setVisibility(View.GONE);
                                if (mutedCount != null) {
                                    mutedCount.cancel();
                                }
                            }
                        }
                    }
                    break;
                case MSG_DELETE_FRIEND:// 删除好友
                    if (targetId.equals(event.msg.senderInfo.id) || targetId.equals(event.msg.receiverInfo.id)) {
                        ShowUtils.showToastNormal(instance, getString(R.string.chat_main_tips4));
                        finish();
                    }
                    break;
                case MSG_NEW_DEVICE_PUSH:// 收到批量消息
                case MSG_OFFLINE_PUSH:
                case MSG_FORWARD_PUSH:
                case MSG_ACK_PUSH:
                    List<ChatMessage> list = ((NewMessageListEvent) event).messages;
                    if (list == null) {
                        break;
                    }
                    for (int i = 0; i < list.size(); i++) {
                        // 发送给当前对象的消息(群聊、私聊)
                        boolean cond1 = list.get(i).receiveId.equals(targetId);
                        // 当前对象发送给我的消息(私聊)
                        boolean cond2 = list.get(i).senderId.equals(targetId) && list.get(i).receiveId.equals(UserInfo.getInstance().id);
                        if (cond1 || cond2) {
                            if (chatList.size() == 0 || list.get(i).sendTime > chatList.get(chatList.size() - 1).sendTime) {
                                boolean scrollToBottom = true;
                                // 当底部不可见消息不超过2条时，自动滚动消息列表
                                if (lastVisible < chatList.size() - 3) {
                                    scrollToBottom = false;
                                }
                                for (int j = chatList.size() - 1; j >= 0; j--) {
                                    if (list.get(i).logId.equals(chatList.get(j).logId)) {
                                        break;
                                    }
                                    if (j == 0) {
                                        if (list.get(i).shouldSave()) {
                                            chatList.add(list.get(i));
                                            mAdapter.notifyDataSetChanged();
                                            if (scrollToBottom) {
                                                scrollToBottom();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    RoomUtils.run(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                                // 清空未读消息计数
                                ChatDatabase.getInstance().recentMessageDao().clearUnreadMsg(channelType, targetId);
                                ChatDatabase.getInstance().recentMessageDao().clearAitMsg(channelType, targetId, false);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
            }
        }
    };

    private PublishSubject<ChatMessage> praise = PublishSubject.create();
    private Disposable praiseMsg = praise
            .flatMap(new Function<ChatMessage, ObservableSource<Boolean>>() {
                @Override
                public ObservableSource<Boolean> apply(ChatMessage item) throws Exception {
                    for (int i = chatList.size() - 1; i >= 0; i--) {
                        if (item.msg.logId.equals(chatList.get(i).logId)) {
                            if (item.msg.like > chatList.get(i).praise.like) {
                                chatList.get(i).praise.like = item.msg.like;
                            }
                            if (item.msg.reward > chatList.get(i).praise.reward) {
                                chatList.get(i).praise.reward = item.msg.reward;
                            }
                            if (item.msg.operator.equals(viewModel.getUserId())) {
                                if (item.msg.action.equals(ACTION_LIKE)) {
                                    chatList.get(i).praise.like();
                                } else if (item.msg.action.equals(ACTION_REWARD)) {
                                    chatList.get(i).praise.reward();
                                }
                            }
                            return Observable.just(true);
                        }
                    }
                    return Observable.just(false);
                }
            })
            .throttleLast(500, TimeUnit.MILLISECONDS)
            .subscribe(item -> {
                viewModel.notifyDataSetChanged();
            }, Throwable::printStackTrace);

    private void praiseMessage(ChatMessage item) {
        if (!item.senderId.equals(targetId) && !item.receiveId.equals(targetId)) {
            // 不属于当前聊天，则不处理
            return;
        }
        praise.onNext(item);
    }

    /**
     * 倒计时控制类
     */
    private MyCount myCount;
    // 顶部新弹出公告默认显示2分钟
    private final static int CODE_TIME = 2 * 60 * 1000;

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolbar_back_button) {
            if (mAdapter.isSelectable()) {
                switchSelectMode();
            } else {
                finish();
            }

        } else if (i == R.id.public_hide) {
            mPublicLayout.setVisibility(View.GONE);
//            showSystemChat();
            if (myCount != null) {
                myCount.cancel();
            }
        } else if (i == R.id.chat_btn_user) {
            if (channelType == CHANNEL_GROUP) {

            } else if (channelType == CHANNEL_ROOM) {
                ARouter.getInstance().build(AppRoute.GROUP_INFO).withString("roomId", targetId).navigation(instance);
            } else if (channelType == CHANNEL_FRIEND) {
                ARouter.getInstance().build(AppRoute.USER_DETAIL)
                        .withString("userId", targetId)
                        .withInt("sourceType", FIND_TYPE_DEFAULT)
                        .withBoolean("disableSendBtn", true)
                        .navigation(this);
            }
        } else if (i == R.id.rl_new_reward_up) {
            ARouter.getInstance().build(AppRoute.CHAT_PRAISE)
                    .withInt("channelType", channelType)
                    .withString("targetId", targetId)
                    .withString("targetName", targetName)
                    .navigation();
        } else if (i == R.id.rl_new_msg_up) {
            seeNewMsg = true;
            if (msgCount <= PAGE_SIZE) {
                mListView.setSelection(chatList.size() - msgCount);
                showNewMsgUp(false);
            } else {
                viewModel.getChatLogHistory(ChatActivity.this, targetId, channelType, nextLog, timeStamp, msgCount - PAGE_SIZE);
            }
        } else if (i == R.id.rl_new_msg_down) {
            mListView.smoothScrollToPositionFromTop(chatList.size() - 1 - newMsgCount, 0, 500);
            showNewMsgDown(false);
        } else if (i == R.id.ll_forward) {
            prepareForward(1);
        } else if (i == R.id.ll_batch_forward) {
            prepareForward(2);
        } else if (i == R.id.ll_collect) {

        } else if (i == R.id.ll_delete) {
            final List<ChatMessage> messages = mAdapter.getSelectedMsg();
            if (messages.size() == 0) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tip_choose_msg1));
                return;
            }
            new EasyDialog.Builder()
                    .setHeaderTitle(getString(R.string.chat_tips_tips))
                    .setContent(getString(R.string.chat_dialog_delete))
                    .setBottomLeftText(getString(R.string.chat_action_cancel))
                    .setBottomRightText(getString(R.string.chat_action_delete))
                    .setBottomRightClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog) {
                            dialog.dismiss();
                            for (final ChatMessage msg : messages) {
                                chatList.remove(msg);
                                RoomUtils.run(new Runnable() {
                                    @Override
                                    public void run() {
                                        ChatDatabase.getInstance().chatMessageDao()
                                                .fakeDeleteMessage(msg.channelType, msg.logId);
                                    }
                                });
                            }
                            switchSelectMode();
                        }
                    }).create(instance).show();
        }
    }

    private void prepareForward(int forwardType) {
        if (mAdapter.getSelectedMsg().size() == 0) {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tip_choose_msg2));
            return;
        }
        PreForwardRequest request = new PreForwardRequest(targetId, isGroupChat ? 1 : 2, forwardType, mAdapter.getSelectedMsg());
        ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                .withSerializable("preForward", request)
                .navigation(this, REQUEST_FORWARD);
    }

    @Override
    public void onOpenAitList() {
        if (roomUsers != null && roomUsers.size() > 0) {
            ARouter.getInstance().build(AppRoute.AIT_SELECT)
                    .withInt("memberLevel", memberLevel)
                    .withString("targetId", targetId)
                    .navigation(this, AitSelectorActivity.getREQUEST_CODE());
        }
    }

    /**
     * 倒计时控制类
     */
    public class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            mPublicLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 倒计时控制类
     */
    private MutedCount mutedCount;

    public class MutedCount extends CountDownTimer {

        public MutedCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (chatInputView.getTvMuted().getVisibility() == View.GONE) {
                chatInputView.getTvMuted().setVisibility(View.VISIBLE);
            }
            chatInputView.getTvMuted().setText(getString(R.string.chat_tips_mute_state5, com.fzm.chat33.utils.StringUtils.formatMutedTime(millisUntilFinished)));
        }

        @Override
        public void onFinish() {
            chatInputView.getTvMuted().setText("");
            chatInputView.getTvMuted().setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatMessage(ChatMessage message) {
        if (targetId.equals(message.receiveId)) {
            sendMessage(message, false);
        }
    }

    private boolean checkEncryptKey() {
        if (AppConfig.APP_ENCRYPT && encryptMode && isGroupChat) {
            if (!CipherManager.hasDHKeyPair()) {
                ShowUtils.showToastNormal(instance, R.string.chat_set_chat_password);
                return false;
            } else if (TextUtils.isEmpty(groupKey)) {
                new EasyDialog.Builder()
                        .setHeaderTitle(getString(R.string.chat_tips_tips))
                        .setBottomLeftText(getString(R.string.chat_action_cancel))
                        .setBottomRightText(getString(R.string.chat_action_update))
                        .setContent(getString(R.string.chat_dialog_update_group_key))
                        .setBottomLeftClickListener(null)
                        .setBottomRightClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog) {
                                dialog.dismiss();
                                loading(true);
                                GroupKeyManager.notifyGroupEncryptKey(targetId, new Function0<Unit>() {
                                    @Override
                                    public Unit invoke() {
                                        getWindow().getDecorView().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dismiss();
                                            }
                                        }, 1000);
                                        return null;
                                    }
                                });
                            }
                        })
                        .create(this).show();
                return false;
            }
        }
        return true;
    }

    public void sendMessage(final ChatMessage sentMsg, boolean resend) {
        if (!checkEncryptKey()) {
            // 发送消息前检查加密聊天密钥，不符合条件则不发送
            return;
        }
        //系统消息不添加
        if (!resend) {
            // 重新发送不添加
            chatList.add(sentMsg);
            chatInputView.setText("");
        }
        mAdapter.notifyDataSetChanged();
        Chat33.getLocalCache().localPathMap.put(sentMsg.getMsgId(), sentMsg.msg.getLocalPath());

        if (aitManager != null) {
            aitManager.reset();
        }
        manager.enqueue(sentMsg, false, (success, message) -> {
            if (!success) {
                for (int i = chatList.size() - 1; i >= 0; i--) {
                    if (message.getMsgId().equals(chatList.get(i).getMsgId())) {
                        if (chatList.get(i).messageState == MessageState.SENDING) {
                            chatList.get(i).messageState = MessageState.SEND_FAIL;
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
            return null;
        });
        smoothScrollToBottom();
    }

    private void showUnEncryptedDialog() {
        boolean show = UserInfoPreference.getInstance().getBooleanPref(SHOW_UNENCRYPTED_TIPS + targetId, true);
        if (show) {
            EasyDialog dialog = new EasyDialog.Builder()
                    .setHeaderTitle(getString(R.string.chat_tips_tips))
                    .setBottomLeftText(getString(R.string.chat_action_ack))
                    .setContent(getString(R.string.chat_dialog_no_msg_key))
                    .setBottomLeftClickListener(null)
                    .setBottomRightClickListener(null)
                    .create(this);
            dialog.show();
            UserInfoPreference.getInstance().setBooleanPref(SHOW_UNENCRYPTED_TIPS + targetId, false);
        }
    }

    public void onGetChatLogByTimeSuccess(List<ChatMessage> oldChatList, String nextLogId) {
        nextLog = nextLogId;
        swipeLayout.finishRefresh();
        if (isLoadMore && (oldChatList == null || oldChatList.size() == 0)) {
//            MyLog.toastShort("没有更多了");
            return;
        }
        if (oldChatList.size() > 0) {
            timeStamp = oldChatList.get(0).sendTime;
        } else {
            timeStamp = 0;
        }

        if (isLoadMore) {
            mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        } else {
            //mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        }

        if (isLoadMore || seeNewMsg) {
            if (chatList.size() > 0 && oldChatList.size() > 0) {
                if (chatList.get(0).logId.equals(oldChatList.get(oldChatList.size() - 1).logId)) {
                    oldChatList.remove(oldChatList.size() - 1);
                }
            }
            chatList.addAll(0, oldChatList);
        } else {
            chatList.clear();
            chatList.addAll(oldChatList);
        }

        mAdapter.notifyDataSetChanged();
        if (seeNewMsg) {
            seeNewMsg = false;
            mListView.smoothScrollToPositionFromTop(chatList.size() - msgCount, 0, 500);
        } else if (fromLogId != null) {
            fromLogId = null;
            mListView.setSelection(0);
        } else {
            mListView.clearFocus();
            mListView.setSelection(oldChatList.size() - 1);
        }
        updateOutList();
    }

    /**
     * 更新外部消息列表的消息
     */
    private void updateOutList() {
        if (chatList.size() > 0) {
            ChatMessage chatMessage = null;
            for (int i = chatList.size() - 1; i >= 0; i--) {
                if (chatList.get(i).snapCounting == 0) {
                    chatMessage = chatList.get(i);
                    break;
                }
            }
            if (chatMessage == null) {
                chatMessage = chatList.get(chatList.size() - 1);
            }
            final ChatMessage finalChatMessage = chatMessage;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    RecentMessage current = ChatDatabase.getInstance().recentMessageDao().getRecentMsgById(channelType, targetId);
                    if (current != null && finalChatMessage.logId.equals(current.getLastLog().getLogId())) {
                        if (current.getLastLog().getMsg().content != null
                                || current.getLastLog().getMsg().fileName != null
                                || current.getLastLog().getMsg().sourceLog != null) {
                            // 如果当前recentMessage的id和要插入的消息id相同，并且内容不为空，则不操作
                            return;
                        }
                    }
                    int sticky = 2;
                    int disturb = 2;
                    String address = null;
                    long deadline = 0;
                    if (!isDeleted) {
                        if (channelType == CHANNEL_ROOM) {
                            RoomListBean roomListBean = viewModel.getLocalRoomById(targetId);
                            if (roomListBean != null) {
                                sticky = roomListBean.getOnTop();
                                disturb = roomListBean.getNoDisturbing();
                                address = roomListBean.getDepositAddress();
                                deadline = roomListBean.getDisableDeadline();
                            }
                        } else {
                            FriendBean friendBean = viewModel.getLocalFriendById(targetId);
                            if (friendBean != null) {
                                sticky = friendBean.getOnTop();
                                disturb = friendBean.getNoDisturbing();
                                address = friendBean.getDepositAddress();
                            }
                        }
                    }
                    RecentMessage oldMsg = ChatDatabase.getInstance().recentMessageDao().getRecentMsgById(channelType, targetId);
                    RecentMessage.PraiseNum praise = oldMsg == null ? new RecentMessage.PraiseNum() : oldMsg.getPraise();
                    RecentMessage message = new RecentMessage(targetId, address, deadline, 0, sticky, disturb, isDeleted, false,
                            praise, new RecentMessage.LastLogBean(finalChatMessage));
                    ChatDatabase.getInstance().recentMessageDao().insert(message);
                }
            });
        }
    }

    @Override
    protected void onHideKeyboard() {

    }

    @Override
    protected void onShowKeyboard(int keyboardHeight) {
        chatInputView.hideOtherSendLayout();
        scrollToBottom();
    }

    @Override
    public void onBackPressed() {
        if (guideView != null && guideView.onBackPressed()) {
            return;
        }
        if (mAdapter.isSelectable()) {
            switchSelectMode();
            return;
        }
        if (chatInputView.hideOtherSendLayout()) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * 切换阅后即焚模式
     */
    private void switchSnapMode(int isSnap) {
        if (isSnap == 1) {
            this.isSnap = 2;
            App.getInstance().snapModeList.remove(channelType + "-" + targetId);
            chat_input_snap.setVisibility(View.GONE);
            chatInputView.setVisibility(View.VISIBLE);
            chatInputView.chatEditInput.requestFocus();
        } else {
            this.isSnap = 1;
            if (!App.getInstance().snapModeList.contains(channelType + "-" + targetId)) {
                App.getInstance().snapModeList.add(channelType + "-" + targetId);
            }
            chat_input_snap.setVisibility(View.VISIBLE);
            chat_input_snap.chatEditInput.requestFocus();
            KeyboardUtils.showKeyboard(chat_input_snap.chatEditInput);
            chatInputView.setVisibility(View.GONE);
        }
    }

    private void switchSnapMode() {
        switchSnapMode(isSnap);
    }

    /**
     * 切换消息选择模式
     */
    private void switchSelectMode() {
        if (mAdapter.isSelectable()) {
            for (ChatMessage message : chatList) {
                message.isSelected = false;
            }
            if (messageCountOut > 999) {
                backButton.setText(getString(R.string.icon_back_left) + " (...)");
            } else if (messageCountOut > 0) {
                backButton.setText(getString(R.string.icon_back_left) + " (" + messageCountOut + ")");
            } else {
                backButton.setText(R.string.icon_back_left);
            }
            chatBtnUser.setVisibility(View.VISIBLE);
            mAdapter.clearSelectedMsg();
            mAdapter.setSelectable(false);
            mAdapter.notifyDataSetChanged();
            ll_select_options.setVisibility(View.GONE);
            if (!isDeleted) {
                if (isSnap == 1) {
                    chatInputView.setVisibility(View.GONE);
                    chat_input_snap.setVisibility(View.VISIBLE);
                    chat_input_snap.chatEditInput.requestFocus();
                } else {
                    chat_input_snap.setVisibility(View.GONE);
                    chatInputView.setVisibility(View.VISIBLE);
                    chatInputView.chatEditInput.requestFocus();
                }
            } else {
                chatInputView.setVisibility(View.GONE);
                chat_input_snap.setVisibility(View.GONE);
            }
        } else {
            chatBtnUser.setVisibility(View.INVISIBLE);
            backButton.setText(getString(R.string.chat_action_cancel));
            chatInputView.setVisibility(View.GONE);
            chat_input_snap.setVisibility(View.GONE);
            ll_select_options.setVisibility(View.VISIBLE);
            mAdapter.setSelectable(true);
            mAdapter.notifyDataSetChanged();
        }
    }

    private ChatInputView.IChatInputView iChatInputView = new ChatInputView.IChatInputView() {
        @Override
        public void onAudioRecorderFinished(float seconds, String filePath) {
            viewModel.uploadAudio(ChatActivity.this, targetId, channelType, isSnap, seconds, filePath);
        }

        @Override
        public void onImageButtonClick(View view) {
            if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(UPLOAD_IMAGE_PERMISSION, ChatActivity.this)) {
                return;
            }

            PictureSelector.create(instance)
                    .openGallery(PictureMimeType.ofAll())
                    .theme(R.style.chat_picture_style)
                    .maxSelectNum(9)
                    .imageSpanCount(4)
                    .previewImage(true)
                    .isCamera(false)
                    .forResult(REQUEST_LIST_CODE);
        }

        @Override
        public void onTakePictureButtonClick(View view) {
            if (!com.fzm.chat33.utils.FileUtils.isGrantShoot(TAKE_IMAGE_PERMISSION, ChatActivity.this)) {
                return;
            }
            ARouter.getInstance().build(AppRoute.CAMERA_SHOOT).withInt("mode", 3).navigation(instance, REQUEST_TAKE_CODE);
        }

        @Override
        public void onSendButtonClick(View view, String content) {
            ChatFile chatFile = new ChatFile();
            chatFile.content = content;
            if (isGroupChat) {
                List<String> members = aitManager.getAitMembers();
                if (members != null && members.size() > 0) {
                    chatFile.aitList = members;
                }
            }
            sendMessage(ChatMessage.create(targetId, channelType,1, isSnap, chatFile), false);
        }

        @Override
        public void onRedPacketClick(View view, int mode) {
            ARouter.getInstance().build("/app/redPacket")
                    .withString("targetId", targetId)
                    .withBoolean("isGroup", isGroupChat)
                    .withInt("mode", mode)
                    .navigation();
        }

        @Override
        public void onFileClick(View view) {
            if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(WRITE_EXTERNAL_STORAGE, ChatActivity.this)) {
                return;
            }
            FilePicker.from(instance)
                    .chooseForBrowser()
                    .setCompressImage(false)
                    .setMaxCount(9)
                    .requestCode(REQUEST_FILE_PICK)
                    .start();
        }

        @Override
        public void onSnapChatClick(View view) {
            switchSnapMode();
        }

        @SuppressLint("CheckResult")
        @Override
        public void onTransferClick(View view) {
            FriendBean friendBean = Chat33.loadFriendFromCache(targetId);
            if (friendBean != null) {
                ARouter.getInstance().build(AppRoute.DEPOSIT_OUT)
                        .withSerializable("target", friendBean)
                        .navigation();
            } else {
                ShowUtils.showToast(instance, R.string.chat_friend_no_exits);
            }
        }

        @Override
        public void onReceiptMoneyClick(View view) {
            FriendBean friendBean = Chat33.loadFriendFromCache(targetId);
            if (friendBean != null) {
                ARouter.getInstance().build(AppRoute.DEPOSIT_RECEIPT)
                        .withSerializable("target", friendBean)
                        .navigation();
            } else {
                ShowUtils.showToast(instance, R.string.chat_friend_no_exits);
            }
        }

        @Override
        public void scrollChatLogHistory() {
            scrollToBottom();
        }

        @Override
        public void needShowKeyboard(View view) {
            KeyboardUtils.showKeyboard(view);
        }

        @Override
        public void needHideKeyboard() {
            KeyboardUtils.hideKeyboard(getWindow().getDecorView());
        }

        @Override
        public void login() {

        }

        @Override
        public void selectMessageType(int type) {
            csMessageType = type;
        }
    };

    private SnapChatInputView.SnapChatViewCallback snapChatViewCallback = new SnapChatInputView.SnapChatViewCallback() {
        @Override
        public void onAudioRecorderFinished(float seconds, String filePath) {
            viewModel.uploadAudio(ChatActivity.this, targetId, channelType, isSnap, seconds, filePath);
        }

        @Override
        public void onCloseClick(View view) {
            switchSnapMode();
        }

        @Override
        public void onSendClick(View view, String content) {
            ChatFile chatFile = new ChatFile();
            chatFile.content = content;
            sendMessage(ChatMessage.create(targetId, channelType,1, isSnap, chatFile), false);
        }

        @Override
        public void onImageClick(View view) {
            needHideKeyboard();
            if (!com.fzm.chat33.utils.FileUtils.isGrantCamera(TAKE_IMAGE_PERMISSION, ChatActivity.this)) {
                return;
            }
            BottomPopupWindow popupWindow = new BottomPopupWindow(instance, imageOptions, new BottomPopupWindow.OnItemClickListener() {
                @Override
                public void onItemClick(View view, PopupWindow popupWindow, int position) {
                    popupWindow.dismiss();
                    if (position == 0) {
                        if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(TAKE_IMAGE_PERMISSION, ChatActivity.this)) {
                            return;
                        }
                        ARouter.getInstance().build(AppRoute.CAMERA_SHOOT).navigation(instance, REQUEST_TAKE_CODE);
                    } else if (position == 1) {
                        if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(UPLOAD_IMAGE_PERMISSION, ChatActivity.this)) {
                            return;
                        }

                        PictureSelector.create(instance)
                                .openGallery(PictureMimeType.ofImage())
                                .theme(R.style.chat_picture_style)
                                .maxSelectNum(9)
                                .imageSpanCount(4)
                                .previewImage(true)
                                .isCamera(false)
                                .forResult(REQUEST_LIST_CODE);
                    }
                }
            });
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        }

        @Override
        public void scrollChatLogHistory() {
            scrollToBottom();
        }

        @Override
        public void needHideKeyboard() {
            KeyboardUtils.hideKeyboard(getWindow().getDecorView());
        }
    };
}
