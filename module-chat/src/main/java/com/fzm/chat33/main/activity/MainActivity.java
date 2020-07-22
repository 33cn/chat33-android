package com.fzm.chat33.main.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.bus.LiveBus;
import com.fuzamei.common.net.rxjava.ApiException;
import com.fuzamei.common.utils.ActivityUtils;
import com.fuzamei.common.utils.BadgeUtil;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.InstallUtil;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.task.Task;
import com.fuzamei.common.utils.task.TaskManager;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.app.BusEvent;
import com.fuzamei.componentservice.app.RouterHelper;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.config.AppPreference;
import com.fuzamei.componentservice.event.CaptureEvent;
import com.fuzamei.componentservice.event.NewFriendRequestEvent;
import com.fuzamei.componentservice.event.NicknameRefreshEvent;
import com.fzm.chat33.BuildConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.app.App;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.bean.NotificationBean;
import com.fzm.chat33.core.consts.SocketCode;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.event.BaseChatEvent;
import com.fzm.chat33.core.event.NotificationEvent;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.EventReceiver;
import com.fzm.chat33.core.listener.NetworkChangeListener;
import com.fzm.chat33.core.manager.CipherManager;
import com.fzm.chat33.core.manager.FileEncryption;
import com.fzm.chat33.core.manager.MessageManager;
import com.fzm.chat33.core.net.socket.ChatSocket;
import com.fzm.chat33.core.service.MessageService;
import com.fzm.chat33.core.utils.UserInfoPreference;
import com.fzm.chat33.global.LocalData;
import com.fzm.chat33.main.fragment.BookFragment;
import com.fzm.chat33.main.fragment.MessageFragment;
import com.fzm.chat33.main.fragment.MyFragment;
import com.fzm.chat33.main.mvvm.MainViewModel;
import com.fzm.chat33.widget.EditNameDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.fzm.chat33.core.global.Chat33Const.MSG_ADD_FRIEND;
import static com.fzm.chat33.core.global.Chat33Const.MSG_BANNED_GROUP;
import static com.fzm.chat33.core.global.Chat33Const.MSG_BANNED_USER;
import static com.fzm.chat33.core.global.Chat33Const.MSG_DELETE_FRIEND;
import static com.fzm.chat33.core.global.Chat33Const.MSG_DISMISS_GROUP;
import static com.fzm.chat33.core.global.Chat33Const.MSG_ENTER_GROUP;
import static com.fzm.chat33.core.global.Chat33Const.MSG_EXIT_GROUP;
import static com.fzm.chat33.core.global.Chat33Const.MSG_GROUP_REQUEST;
import static com.fzm.chat33.core.global.Chat33Const.MSG_NORMAL_MESSAGE;
import static com.fzm.chat33.core.global.Chat33Const.MSG_OTHER_LOGIN;

/**
 * @author zhengjy
 * @since 2018/10/18
 * Description:app主界面
 */
@Route(path = AppRoute.MAIN)
public class MainActivity extends DILoadableActivity implements View.OnClickListener {

    private ViewGroup mCurrentTab;
    private RelativeLayout messageLayout, walletLayout, bookLayout, myLayout;
    private MessageFragment messageFragment;
    private BookFragment bookFragment;
    private MyFragment myFragment;
    private ImageView iv_main_msg, iv_main_wallet, iv_main_contact, iv_main_me;
    private TextView tv_message_num, tv_wallet_num, tv_book_num;
    private View dot_my;
    private TextView tv_message, tv_wallet, tv_book, tv_my;
    private int bookNum;
    private Intent messageIntent;
    private NotificationManager manager;
    @Inject
    public ChatSocket chatSocket;
    @Inject
    public ViewModelProvider.Factory provider;
    private MainViewModel viewModel;

    private Dialog mDialog;

    public File installApk;

    private Handler handler = new Handler();
    private Runnable checkUpdate = new Runnable() {
        @Override
        public void run() {
            if (App.getInstance().ignoreUpdate) {
                handler.removeCallbacks(checkUpdate);
            } else {
                if (!ActivityUtils.isBackground()) {
                    LocalData.checkUpdate(instance, false, apkFile -> installApk = apkFile);
                }
                handler.postDelayed(this, 60 * 1000L);
            }
        }
    };

    /**
     * 一般是由Socket返回的需要显示的信息
     */
    private String serverTips = null;

    private NetworkChangeListener netWorkChangeListener = new NetworkChangeListener() {
        @Override
        public void onMobileAvailable() {
            resumeFromDisconnected();
        }

        @Override
        public void onWifiAvailable() {
            resumeFromDisconnected();
        }

        @Override
        public void onDisconnected() {

        }
    };

    @Autowired
    public Bundle data;

    public Uri route;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main_chat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        ARouter.getInstance().inject(this);
        if (data != null) {
            route = data.getParcelable("route");
        } else {
            route = getIntent().getParcelableExtra("route");
        }
        messageIntent = new Intent(this, MessageService.class);
        Chat33.setNetworkChangeListener(netWorkChangeListener);

        if (!viewModel.isLogin()) {
            tokenLogin();
        } else {
            setTabSelection(0);
            initDatas();
        }
    }

    @Override
    protected void setEvent() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        data = intent.getBundleExtra("data");
        if (data != null) {
            route = data.getParcelable("route");
        } else {
            route = intent.getParcelableExtra("route");
        }
        if (!viewModel.isLogin()) {
            tokenLogin();
        } else {
            setTabSelection(0);
            initDatas();
        }
    }

    /**
     * 从断开网络连接状态恢复
     */
    private void resumeFromDisconnected() {
        if (AppConfig.SERVER_LOGIN) {
            return;
        }
        if (!viewModel.isLogin()) {
            String token = AppPreference.INSTANCE.getTOKEN();
            if (!TextUtils.isEmpty(token)) {
                tokenLogin();
            }
        } else if (!chatSocket.isAlive()) {
            Chat33.checkService();
            chatSocket.connect();
        }
    }

    private void tokenLogin() {
        AppConfig.SERVER_LOGIN = true;
        viewModel.login();
    }

    @SuppressLint("CheckResult")
    private void initDatas() {
        viewModel.updateFriendsList();
        viewModel.updateRoomList();
        viewModel.loadRoomUsers();
        viewModel.loadInfoCache();
        messageLayout.setOnClickListener(this);
        walletLayout.setOnClickListener(this);
        bookLayout.setOnClickListener(this);
        myLayout.setOnClickListener(this);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Chat33.registerEventReceiver(new EventReceiver() {
            @Override
            public void onReceiveEvent(BaseChatEvent event) {
                switch (event.eventType) {
                    case MSG_NORMAL_MESSAGE:
                        if (event instanceof NotificationEvent) {
                            // 显示通知和桌面角标
                            NotificationEvent tmp = (NotificationEvent) event;
                            int notificationId;
                            try {
                                notificationId = Integer.parseInt(tmp.notification.id);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                notificationId = 0;
                            }
                            if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi")) {
                                BadgeUtil.setBadgeOfMIUI(instance, tmp.notification.count, notificationId, showNotification(tmp.notification));
                            } else {
                                Notification notification = showNotification(tmp.notification);
                                if (manager != null) {
                                    manager.notify(notificationId, notification);
                                }
                            }
                        }
                        break;
                    case MSG_OTHER_LOGIN:
                    case MSG_BANNED_USER:
                        if (!ActivityUtils.isActivityTop(instance, ServerTipsActivity.class)/* && !ActivityUtils.isBackground()*/) {
                            ARouter.getInstance().build(AppRoute.SERVER_TIPS).withString("tips", event.msg.content).navigation();
                        }
                        break;
                    case MSG_BANNED_GROUP:// 群组被封禁
                    case MSG_ENTER_GROUP:// 入群通知：1.创建群 2.被邀请者入群 3.直接入群回复
                    case MSG_EXIT_GROUP:// 退群通知
                    case MSG_DISMISS_GROUP:// 解散群通知
                        LiveBus.of(BusEvent.class).contactsRefresh().setValue(2);
                        break;
                    case MSG_GROUP_REQUEST:
                        if (event.msg.receiverInfo.id != null) {
                            if (event.msg.status == 1) {
                                if (event.msg.senderInfo.id.equals(viewModel.getUserId())) {
                                    if (!App.getInstance().newFriendRequest.containsKey(event.msg.receiverInfo.id)) {
                                        NewFriendRequestEvent event1 = new NewFriendRequestEvent(event.msg.receiverInfo.id, event.msg.receiverInfo.avatar, false);
                                        App.getInstance().newFriendRequest.put(event.msg.receiverInfo.id, event1);
                                        LiveBus.of(BusEvent.class).newFriends().setValue(event1);
                                    }
                                } else {
                                    if (!App.getInstance().newFriendRequest.containsKey(event.msg.senderInfo.id)) {
                                        NewFriendRequestEvent event1 = new NewFriendRequestEvent(event.msg.senderInfo.id, event.msg.senderInfo.avatar, false);
                                        App.getInstance().newFriendRequest.put(event.msg.senderInfo.id, event1);
                                        LiveBus.of(BusEvent.class).newFriends().setValue(event1);
                                    }
                                }
                            } else if (event.msg.status == 3) {
                                if (event.msg.senderInfo.id.equals(viewModel.getUserId())) {
                                    LiveBus.of(BusEvent.class).contactsRefresh().setValue(event.msg.type == 1 ? 2 : 1);
                                }
                            }
                        }
                        break;
                    case MSG_ADD_FRIEND:
                        if (event.msg.receiverInfo.id.equals(viewModel.getUserId()) && event.msg.status == 1) {
                            if (!App.getInstance().newFriendRequest.containsKey(event.msg.senderInfo.id)) {
                                NewFriendRequestEvent event2 = new NewFriendRequestEvent(event.msg.senderInfo.id, event.msg.senderInfo.avatar, false);
                                App.getInstance().newFriendRequest.put(event.msg.senderInfo.id, event2);
                                LiveBus.of(BusEvent.class).newFriends().setValue(event2);
                            }
                        } else if (event.msg.senderInfo.id.equals(viewModel.getUserId()) && event.msg.status == 3) {
                            LiveBus.of(BusEvent.class).contactsRefresh().setValue(2);
                        } else if (event.msg.receiverInfo.id.equals(viewModel.getUserId()) && event.msg.status == 3) {
                            LiveBus.of(BusEvent.class).contactsRefresh().setValue(2);
                        }
                        break;
                    case MSG_DELETE_FRIEND:// 删除好友通知
                        LiveBus.of(BusEvent.class).contactsRefresh().setValue(1);
                        break;
                    case SocketCode.SOCKET_OTHER_LOGIN:// 其他端登录
                    case SocketCode.SOCKET_OTHER_UPDATE_WORDS:// 其他端修改助记词
                        serverTips = event.msg.content;
                        // 在其他端登录，且App在前台则弹出窗口
                        if (!ActivityUtils.isBackground()) {
                            showServerTips();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        RoomUtils.subscribe(ChatDatabase.getInstance().recentMessageDao().getAllMsgCount(), new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                if (integer <= 0) {
                    tv_message_num.setVisibility(View.GONE);
                } else if (integer > 99 && !BuildConfig.DEBUG) {
                    // debug下显示总消息数
                    tv_message_num.setText("...");
                    tv_message_num.setVisibility(View.VISIBLE);
                } else {
                    tv_message_num.setText(String.valueOf(integer));
                    tv_message_num.setVisibility(View.VISIBLE);
                }
                BadgeUtil.setBadgeCount(instance, integer);
            }
        });
        getNewFriendsApplyList();

        if (!Chat33.isServiceWorked() && !ActivityUtils.isBackground()) {
            startService(messageIntent);
        }

        // 首次登陆提示设置昵称
        if (viewModel.getCurrentUser().getValue().firstLogin) {
            viewModel.updateInfo(userInfo -> {
                userInfo.firstLogin = false;
                return null;
            });
            TaskManager.create()
                    .addTask(new Task() {
                        @Override
                        public void work() {
                            if (!AppConfig.FILE_ENCRYPT) {
                                new EditNameDialog.Builder(instance)
                                        .setRightButton(new EditNameDialog.OnSubmitNameListener() {
                                            @Override
                                            public void onSubmitName(Dialog dialog, final String name) {
                                                if (TextUtils.isEmpty(name)) {
                                                    ShowUtils.showToastNormal(MainActivity.this, getString(R.string.chat_main_tips5));
                                                    return;
                                                }
                                                mDialog = dialog;
                                                viewModel.editName(Chat33Const.CHANNEL_FRIEND, viewModel.getUserId(), name);
                                            }
                                        })
                                        .setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(android.content.DialogInterface dialog) {
                                                done();
                                            }
                                        })
                                        .show();
                            } else {
                                done();
                            }
                        }
                    })
                    .addTask(new Task() {
                        @Override
                        public void work() {
                            if (AppConfig.APP_RECOMMENDED_GROUP) {
                                ARouter.getInstance().build(AppRoute.RECOMMEND_GROUPS).navigation();
                            }
                        }
                    })
                    .start();

        }
        // 版本更新或卸载重装后的首次启动
        if (UserInfoPreference.getInstance().isFirstStart()) {
            Observable
                    .create((ObservableOnSubscribe<Boolean>) emitter -> {
                        emitter.onNext(CipherManager.hasDHKeyPair() && CipherManager.getMnemonicString() == null);
                        emitter.onComplete();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isSetPwd -> {
                        UserInfoPreference.getInstance().setBooleanPref(UserInfoPreference.USER_HAS_CHAT_KEY_PWD, isSetPwd);
                    }, throwable -> {
                    });
        }

        handler.postDelayed(checkUpdate, 60 * 1000L);

        if (route != null) {
            handleRoute();
        }
    }

    private void handleRoute() {
        // fzmchat33://chat33.fzm.com?type=shareUserDetail&a=b&x=y...
        String type = route.getQueryParameter("type");
        String path = RouterHelper.routeMap.get(type);
        if ("systemShare".equals(type)) {
            // 来自系统分享
            ARouter.getInstance().build(path).withSerializable("chatFile", data.getSerializable("chatFile")).navigation();
        } else if (!TextUtils.isEmpty(path)) {
            Uri uri = Uri.parse(RouterHelper.APP_LINK + path + "?" + route.getEncodedQuery());
            ARouter.getInstance().build(uri).navigation();
        }
        route = null;
    }

    private static final int REQUEST_IMPORT_MNEMONIC = 100;

    private void setupUserPublicKey(String serviceMnemonic) {
        if (AppConfig.APP_ENCRYPT) {
            String localMnemonic = CipherManager.getEncMnemonicString();
            if (serviceMnemonic != null && serviceMnemonic.startsWith("0x")) {
                serviceMnemonic = serviceMnemonic.substring(2);
            }
            if (localMnemonic != null && localMnemonic.startsWith("0x")) {
                localMnemonic = localMnemonic.substring(2);
            }
            if(TextUtils.isEmpty(localMnemonic) || TextUtils.isEmpty(serviceMnemonic) || !TextUtils.equals(localMnemonic, serviceMnemonic)) {
                ARouter.getInstance().build(AppRoute.IMPORT_MNEMONIC_WORD).navigation(this, REQUEST_IMPORT_MNEMONIC);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(checkUpdate);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void initView() {
        viewModel = ViewModelProviders.of(this, provider).get(MainViewModel.class);
        iv_main_msg = findViewById(R.id.iv_main_msg);
        iv_main_wallet = findViewById(R.id.iv_main_wallet);
        iv_main_contact = findViewById(R.id.iv_main_contact);
        iv_main_me = findViewById(R.id.iv_main_me);

        messageLayout = findViewById(R.id.message_layout);
        walletLayout = findViewById(R.id.wallet_layout);
        bookLayout = findViewById(R.id.book_layout);
        myLayout = findViewById(R.id.my_layout);

        tv_message = findViewById(R.id.tv_message);
        tv_wallet = findViewById(R.id.tv_wallet);
        tv_book = findViewById(R.id.tv_book);
        tv_my = findViewById(R.id.tv_my);

        tv_message_num = findViewById(R.id.tv_message_num);
        tv_wallet_num = findViewById(R.id.tv_wallet_num);
        tv_book_num = findViewById(R.id.tv_book_num);
        dot_my = findViewById(R.id.dot_my);

        LiveBus.of(BusEvent.class).loginExpire().observeForever(obj -> {
            if (viewModel.isLogin()) {
                ShowUtils.showSysToast(new ApiException(-1004).getMessage());
                viewModel.logout();
            }
        });
        LiveBus.of(BusEvent.class).loginEvent().observeForever(login -> {
            AppConfig.SERVER_LOGIN = false;
            if (login) {
                viewModel.uploadDeviceToken();
                setTabSelection(0);
                initDatas();
            } else {
                MessageManager.reset();
                // 退出登录时销毁所有fragment视图
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                mCurrentTab = null;
                if (messageFragment != null) {
                    transaction.remove(messageFragment);
                    messageFragment = null;
                }
                if (bookFragment != null) {
                    transaction.remove(bookFragment);
                    bookFragment = null;
                }
                if (myFragment != null) {
                    transaction.remove(myFragment);
                    myFragment = null;
                }
                transaction.commitAllowingStateLoss();
                tv_message_num.setVisibility(View.GONE);
                if (!ActivityUtils.isActivityTop(Chat33.getContext(), ServerTipsActivity.class)) {
                    ARouter.getInstance().build(AppRoute.LOGIN).navigation();
                    ActivityUtils.exitToLogin();
                }
            }
        });

        LiveBus.of(BusEvent.class).newFriends().observe(this, event -> {
            if (event.clear) {
                bookNum = 0;
                tv_book_num.setVisibility(View.GONE);
            } else {
                bookNum++;
                if (bookNum > 99) {
                    tv_book_num.setText("99+");
                } else {
                    tv_book_num.setText(String.valueOf(bookNum));
                }
                tv_book_num.setVisibility(View.VISIBLE);
            }
        });

        LiveBus.of(BusEvent.class).changeTab().observe(this, event -> setTabSelection(event.tab));

        LiveBus.of(BusEvent.class).contactsRefresh().observe(this, position -> {
            if (bookFragment == null) {
                if (position == 1) {
                    viewModel.updateFriendsList();
                } else if (position == 2) {
                    viewModel.updateRoomList();
                }
            }
        });

        ActivityUtils.addOnAppStateChangedListener(new ActivityUtils.OnAppStateChangedListener() {
            @Override
            public void onResumeApp() {
                // 回到前台，如果有其他端登录，则弹出窗口
                if (serverTips != null) {
                    showServerTips();
                }
            }

            @Override
            public void onLeaveApp() {
                FileEncryption.clearCache();
            }
        });

        viewModel.getCurrentUser().observe(this, userInfo -> {
            // 设置用户助记词公钥
            if(userInfo.isLogin()) {
                setupUserPublicKey(userInfo.privateKey);
            }
        });

        // 持续监听登录失败事件，即使跳转到其他界面也可以响应
        viewModel.getLoginFail().observeForever(e -> {
            // 登录过期或者失败
            viewModel.performLogout();
            if (e != null && e.getErrorCode() == -2030) {
                if (!ActivityUtils.isActivityTop(instance, ServerTipsActivity.class)/* && !ActivityUtils.isBackground()*/) {
                    ARouter.getInstance().build(AppRoute.SERVER_TIPS).withString("tips", e.getMessage()).navigation();
                }
            } else {
                ARouter.getInstance().build(AppRoute.LOGIN).withBundle("data", data).navigation();
                ActivityUtils.exitToLogin();
            }
        });

        viewModel.getUnreadNumber().observe(this, unread -> {
            if (unread == null) {
                return;
            }
            bookNum = unread.number;
            if (bookNum == 0) {
                tv_book_num.setVisibility(View.GONE);
            } else {
                if (bookNum > 99) {
                    tv_book_num.setText("99+");
                } else {
                    tv_book_num.setText(String.valueOf(bookNum));
                }
                tv_book_num.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getEditSelfName().observe(this, name -> {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
            if (name == null) {
                return;
            }
            ShowUtils.showToastNormal(MainActivity.this, getString(R.string.chat_main_tips6));
            viewModel.updateInfo(userInfo -> {
                userInfo.username = name;
                return null;
            });
            LiveBus.of(BusEvent.class).nicknameRefresh()
                    .setValue(new NicknameRefreshEvent(name));
        });

        viewModel.getLoading().observe(this, this::setupLoading);
    }

    private void showServerTips() {
        if (!ActivityUtils.isActivityTop(Chat33.getContext(), ServerTipsActivity.class)) {
            ARouter.getInstance().build(AppRoute.SERVER_TIPS).withString("tips", serverTips).navigation();
        }
        serverTips = null;
    }

    private void restIcon(int index) {
        if (index == 0) {
            tv_message.setTextColor(ContextCompat.getColor(this, R.color.chat_color_accent));
            tv_wallet.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_book.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_my.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            iv_main_msg.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_msg));
            iv_main_wallet.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_wallet_unselected));
            iv_main_contact.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_contact_unselected));
            iv_main_me.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_me_unselected));
        } else if (index == 1) {
            tv_message.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_wallet.setTextColor(ContextCompat.getColor(this, R.color.chat_color_accent));
            tv_book.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_my.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            iv_main_msg.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_msg_unselected));
            iv_main_wallet.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_wallet));
            iv_main_contact.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_contact_unselected));
            iv_main_me.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_me_unselected));
        } else if (index == 2) {
            tv_message.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_wallet.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_book.setTextColor(ContextCompat.getColor(this, R.color.chat_color_accent));
            tv_my.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            iv_main_msg.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_msg_unselected));
            iv_main_wallet.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_wallet_unselected));
            iv_main_contact.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_contact));
            iv_main_me.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_me_unselected));
        } else if (index == 3) {
            tv_message.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_wallet.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_book.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
            tv_my.setTextColor(ContextCompat.getColor(this, R.color.chat_color_accent));
            iv_main_msg.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_msg_unselected));
            iv_main_wallet.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_wallet_unselected));
            iv_main_contact.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_contact_unselected));
            iv_main_me.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_main_me));
        }
    }

    public void getNewFriendsApplyList() {
        viewModel.getUnreadApplyNumber();
    }

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.transparent), 0);
        BarUtils.setStatusBarLightMode(this, true);
    }

    /**
     * 根据传入的index参数来设置选中的tab页
     *
     * @param index 每个tab页对应的下标
     */
    public void setTabSelection(int index) {//首页要用
        if (!viewModel.isLogin() && index == 1) {
            ARouter.getInstance().build(AppRoute.LOGIN).withBundle("data", data).navigation();
            ActivityUtils.exitToLogin();
            return;
        }
        try {
            // 开启一个Fragment事务
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
            hideFragments(fragmentTransaction);
            restIcon(index);
            switch (index) {
                case 0:
                    if (mCurrentTab == messageLayout) {
                        fragmentTransaction.show(messageFragment);
                    } else {
                        if (mCurrentTab != null)
                            mCurrentTab.setSelected(false);
                        messageLayout.setSelected(true);
                        mCurrentTab = messageLayout;
                        if (messageFragment == null) {
                            messageFragment = new MessageFragment();
                            fragmentTransaction.add(R.id.fl_content_layout, messageFragment, "MessageFragment");
                        } else {
                            fragmentTransaction.show(messageFragment);
                        }
                    }
                    BarUtils.setStatusBarLightMode(this, true);
                    break;
//                case 1:
//                    if (mCurrentTab == walletLayout) {
//                        fragmentTransaction.show(homeFragment);
//                    } else {
//                        if (mCurrentTab != null)
//                            mCurrentTab.setSelected(false);
//                        walletLayout.setSelected(true);
//                        mCurrentTab = walletLayout;
//                        if (homeFragment == null) {
//                            homeFragment = (Fragment) ARouter.getInstance().build(AppRoute.DEPOSIT_HOME).navigation();
//                            fragmentTransaction.add(R.id.fl_content_layout, homeFragment, "DepositHomeFragment");
//                        } else {
//                            fragmentTransaction.show(homeFragment);
//                        }
//                    }
//                    BarUtils.setStatusBarLightMode(this, false);
//                    break;
                case 2:
                    if (mCurrentTab == bookLayout) {
                        fragmentTransaction.show(bookFragment);
                    } else {
                        if (mCurrentTab != null)
                            mCurrentTab.setSelected(false);
                        bookLayout.setSelected(true);
                        mCurrentTab = bookLayout;
                        if (bookFragment == null) {
                            bookFragment = new BookFragment();
                            fragmentTransaction.add(R.id.fl_content_layout, bookFragment, "BookFragment");
                        } else {
                            fragmentTransaction.show(bookFragment);
                        }
                    }
                    BarUtils.setStatusBarLightMode(this, true);
                    break;

                case 3:
                    if (mCurrentTab == myLayout) {
                        fragmentTransaction.show(myFragment);
                    } else {
                        if (mCurrentTab != null)
                            mCurrentTab.setSelected(false);
                        myLayout.setSelected(true);
                        mCurrentTab = myLayout;
                        if (myFragment == null) {
                            myFragment = new MyFragment();
                            fragmentTransaction.add(R.id.fl_content_layout, myFragment, "MyFragment");
                        } else {
                            fragmentTransaction.show(myFragment);
                        }
                    }
                    BarUtils.setStatusBarLightMode(this, true);
                    break;
                default:
                    break;
            }
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (messageFragment == null) {
            messageFragment = (MessageFragment) getSupportFragmentManager().findFragmentByTag("MessageFragment");
            if (messageFragment != null) {
                transaction.hide(messageFragment);
            }
        } else {
            transaction.hide(messageFragment);
        }
        if (bookFragment == null) {
            bookFragment = (BookFragment) getSupportFragmentManager().findFragmentByTag("BookFragment");
            if (bookFragment != null) {
                transaction.hide(bookFragment);
            }
        } else {
            transaction.hide(bookFragment);
        }
        if (myFragment == null) {
            myFragment = (MyFragment) getSupportFragmentManager().findFragmentByTag("MyFragment");
            if (myFragment != null) {
                transaction.hide(myFragment);
            }
        } else {
            transaction.hide(myFragment);
        }
    }

    //---------------------------------扫码返回-------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCaptureEvent(CaptureEvent event) {
        int type = event.getType();
        String text = event.getText();
        switch (type) {
            case 1:
                ARouter.getInstance().build(AppRoute.DEPOSIT_OUT)
                        .withString("address", text)
                        .navigation();
                break;
            case 2:
                Toast.makeText(this, R.string.chat_parsing_qr_fails, Toast.LENGTH_LONG).show();
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.message_layout) {
            setTabSelection(0);

        } else if (i == R.id.wallet_layout) {
            setTabSelection(1);

        } else if (i == R.id.book_layout) {
            setTabSelection(2);

        } else if (i == R.id.my_layout) {
            setTabSelection(3);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MyFragment.REQUEST_VERIFY) {
                if (myFragment != null) {
                    myFragment.refreshState();
                }
            } else if (requestCode == InstallUtil.UNKNOWN_CODE) {
                InstallUtil.install(instance, installApk);
            } else if (requestCode == REQUEST_IMPORT_MNEMONIC) {
                // 导入私钥后再更新好友列表
                viewModel.updateFriendsList();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {// 捕捉返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(viewModel.getUserId())) {
            Chat33.checkService();
        }
    }

    public Notification showNotification(NotificationBean bean) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chatMessage");
        builder.setSmallIcon(R.mipmap.ic_launcher_chat33);
        builder.setContentTitle(bean.title);
        builder.setContentText(bean.content);
        builder.setNumber(bean.count);
        builder.setAutoCancel(true);

        Intent intent;
        if (!viewModel.isLogin()) {
            Uri uri = Uri.parse(RouterHelper.APP_LINK + "?type=chatNotification&channelType=" + bean.channelType + "&targetId=" + bean.id);
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("route", uri);
        } else {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("channelType", bean.channelType);
            intent.putExtra("targetId", bean.id);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // 指定点击跳转页面
        builder.setContentIntent(contentIntent);

        return builder.build();
    }
}
