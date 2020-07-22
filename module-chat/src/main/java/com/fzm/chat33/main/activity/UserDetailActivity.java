package com.fzm.chat33.main.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.common.bus.LiveBus;
import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.app.BusEvent;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.event.ChangeTabEvent;
import com.fuzamei.componentservice.event.NicknameRefreshEvent;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.SearchScope;
import com.fzm.chat33.core.bean.ChatTarget;
import com.fzm.chat33.core.bean.RemarkPhone;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.core.manager.CipherManager;
import com.fzm.chat33.global.AppConst;
import com.fzm.chat33.hepler.glide.GlideApp;
import com.fzm.chat33.hepler.glide.SingleKeyEncrypt;
import com.fzm.chat33.main.mvvm.UserDetailViewModel;
import com.fzm.chat33.widget.AddVerifyDialog;
import com.fzm.chat33.widget.ChatAvatarView;
import com.fzm.chat33.widget.SwitchView;
import com.fuzamei.componentservice.widget.dialog.DialogInterface;
import com.fuzamei.componentservice.widget.dialog.EasyDialog;
import com.fzm.chat33.widget.popup.MutePopupWindow;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.fzm.chat33.core.global.Chat33Const.LEVEL_USER;

/**
 * @author zhengjy
 * @since 2018/10/19
 * Description:好友详情界面
 */
@Route(path = AppRoute.USER_DETAIL, extras = AppConst.NEED_LOGIN)
public class UserDetailActivity extends DILoadableActivity implements View.OnClickListener {

    View iv_back, iv_more;

    // 用户信息块
    private ChatAvatarView iv_avatar;
    private TextView tv_remark, tv_uid, tv_identification, tv_nickname, tv_desc;

    // 备注信息块
    private View ll_remark_info, ll_desc, ll_picture, v_divide;
    private RecyclerView rv_phone, rv_picture;

    // 设置块
    private View ll_options;
    private View ll_friend_options;
    private View ll_chat_history, ll_chat_file;
    private SwitchView sb_dnd, sb_stick_top;

    // 设置相关
    private View ll_group_settings;
    private View ll_mute;
    private TextView tv_mute_state;
    private View ll_black;
    private TextView tv_black_state;
    private TextView tv_black_message;
    // 添加好友来源
    private View ll_from, ll_group_from;
    private TextView tv_from_type;

    // 操作块
    private View tv_send_msg, tv_delete_friend;
    private TextView tv_add_friend;

    private final int REQUEST_MODIFY_REMARK = 1;

    @Autowired
    public String userId;
    @Autowired
    public String roomId;
    @Autowired
    public boolean fetchInfoById = true;
    @Autowired
    public boolean disableSendBtn = false;
    @Autowired
    public boolean canAddFriend = false;
    @Autowired
    public int memberLevel = LEVEL_USER;
    @Autowired
    public int sourceType;
    @Autowired
    public String sourceId;

    private CommonAdapter<RemarkPhone> phoneAdapter;
    private CommonAdapter<String> imageAdapter;
    private List<RemarkPhone> phones = new ArrayList<>();
    private List<String> images = new ArrayList<>();

    private RoomUserBean roomUser;
    private FriendBean friendBean;
    private MuteCountDown timer;
    private CountDownTimer dialogTimer;

    @Inject
    public ViewModelProvider.Factory provider;
    private UserDetailViewModel viewModel;
    private Dialog mDialog;

    @Override
    protected boolean enableSlideBack() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_detail;
    }

    @Override
    protected void initView() {
        viewModel = ViewModelProviders.of(this, provider).get(UserDetailViewModel.class);
        iv_back = findViewById(R.id.iv_back);
        iv_more = findViewById(R.id.iv_more);
        iv_avatar = findViewById(R.id.iv_avatar);
        tv_remark = findViewById(R.id.tv_remark);
        tv_uid = findViewById(R.id.tv_uid);
        tv_identification = findViewById(R.id.tv_identification);
        tv_nickname = findViewById(R.id.tv_name);
        tv_from_type = findViewById(R.id.tv_from_type);
        sb_dnd = findViewById(R.id.sb_dnd);
        sb_stick_top = findViewById(R.id.sb_stick_top);
        tv_send_msg = findViewById(R.id.tv_send_msg);
        tv_delete_friend = findViewById(R.id.tv_delete_friend);
        tv_add_friend = findViewById(R.id.tv_add_friend);
        tv_mute_state = findViewById(R.id.tv_mute_state);
        ll_group_settings = findViewById(R.id.ll_group_settings);
        tv_desc = findViewById(R.id.tv_desc);
        ll_from = findViewById(R.id.ll_from);
        ll_desc = findViewById(R.id.ll_desc);
        ll_picture = findViewById(R.id.ll_picture);
        v_divide = findViewById(R.id.v_divide);
        ll_chat_history = findViewById(R.id.ll_chat_history);
        ll_chat_file = findViewById(R.id.ll_chat_file);
        ll_options = findViewById(R.id.ll_options);
        ll_friend_options = findViewById(R.id.ll_friend_options);
        ll_mute = findViewById(R.id.ll_mute);
        ll_black = findViewById(R.id.ll_black);
        tv_black_state = findViewById(R.id.tv_black_state);
        tv_black_message = findViewById(R.id.tv_black_message);
        ll_group_from = findViewById(R.id.ll_group_from);
        ll_remark_info = findViewById(R.id.ll_remark_info);
        rv_phone = findViewById(R.id.rv_phone);
        rv_picture = findViewById(R.id.rv_picture);

        viewModel.getLoading().observe(this, this::setupLoading);

        viewModel.getBlockUser().observe(this, it -> {
            ShowUtils.showToastNormal(instance, R.string.chat_add_black_success);
            friendBean.setIsBlocked(1);
            initBlackViewData();
        });

        viewModel.getUnBlockUser().observe(this, it -> {
            ShowUtils.showToastNormal(instance, R.string.chat_remove_black_success);
            friendBean.setIsBlocked(0);
            initBlackViewData();
        });
        viewModel.getUserInfo().observe(this, it -> {
            friendBean = it;
            LiveBus.of(BusEvent.class).nicknameRefresh()
                    .setValue(new NicknameRefreshEvent(friendBean.getId(), friendBean.getDisplayName(), false));
            setupView(friendBean);
        });
        viewModel.getUidSearchBean().observe(this, it -> {
            friendBean = it.getUserInfo();
            if (friendBean == null) {
                ShowUtils.showToast(instance, getString(R.string.chat_tips_no_user));
            } else {
                setupView(friendBean);
            }
        });

        viewModel.getRoomUserBean().observe(this, this::initRoomUserData);

        viewModel.getStickyOnTop().observe(this, it -> sb_stick_top.toggleSwitch(it != 1));

        viewModel.getSetDND().observe(this, it -> sb_dnd.toggleSwitch(it != 1));

        viewModel.getDeleteFriend().observe(this, it -> {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_delete_success));
            ARouter.getInstance().build(AppRoute.MAIN).navigation();
        });

        viewModel.getCheckAnswer().observe(this, it -> {
            if (it.getResponse().success) {
                if(mDialog != null) {
                    mDialog.dismiss();
                    mDialog = null;
                }
                ARouter.getInstance().build(AppRoute.FRIEND_VERIFY)
                        .withString("id", friendBean.getId())
                        .withString("answer", it.getContent())
                        .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                        .withInt("sourceType", sourceType)
                        .withString("sourceId", sourceId)
                        .navigation();
            } else {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_wrong_answer));
            }
        });

        viewModel.getAddFriendNeedAnswer().observe(this, it -> {
            if (it.state == 1) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_wrong_answer));
            } else if (it.state == 3) {
                if(mDialog != null) {
                    mDialog.dismiss();
                    mDialog = null;
                }
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_add_success));
                fetchData();
            }
        });

        viewModel.getAddFriend().observe(this, it -> {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_add_success));
            ChatDatabase.getInstance().friendsDao().insert(friendBean);
            fetchData();
        });

        viewModel.getSetMutedSingle().observe(this, it -> {
            if (timer != null) {
                timer.cancel();
            }
            fetchData();
            if (it == 0L) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_disable_mute));
            } else {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_enable_mute));
            }
        });
    }

    @Override
    protected void initData() {
        ARouter.getInstance().inject(this);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> preCheckedUsers = new ArrayList<>();
                preCheckedUsers.add(userId);
                ARouter.getInstance().build(AppRoute.CREATE_GROUP)
                        .withStringArrayList("preCheckedUsers", preCheckedUsers)
                        .navigation();
            }
        });
        LiveBus.of(BusEvent.class).nicknameRefresh().observe(this, new Observer<NicknameRefreshEvent>() {
            @Override
            public void onChanged(NicknameRefreshEvent event) {
                // 备注修改刷新
                if (friendBean != null && event.updateSelf && friendBean.getId().equals(event.id)) {
                    friendBean.setRemark(event.nickname);
                    setupView(friendBean);
                }
            }
        });
        if (TextUtils.isEmpty(roomId)) {
            canAddFriend = true;
        } else {
            sourceType = Chat33Const.FIND_TYPE_GROUP;
            sourceId = roomId;
        }
        if (sourceType == 0) {
            sourceType = Chat33Const.FIND_TYPE_SEARCH;
        }
        fetchData();
    }

    private void fetchData() {
        if (fetchInfoById) {
            viewModel.getUserInfo(userId);
        } else {
            viewModel.searchByUid(userId);
        }
    }

    private void setupView(final FriendBean bean) {
        if (!TextUtils.isEmpty(bean.getAvatar())) {
            Glide.with(instance).load(bean.getAvatar())
                    .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_round)).into(iv_avatar);
        } else {
            iv_avatar.setImageResource(R.mipmap.default_avatar_round);
        }
        iv_avatar.setIconRes(bean.isIdentified() ? R.drawable.ic_user_identified : -1);
        tv_uid.setText(getString(R.string.chat_tips_user_uid, bean.getAddress()));
        if (!TextUtils.isEmpty(bean.getRemark())) {
            tv_remark.setText(bean.getRemark());
            tv_nickname.setText(getString(R.string.chat_tips_user_nickname, bean.getName()));
            tv_nickname.setVisibility(View.VISIBLE);
        } else {
            tv_remark.setText(bean.getName());
            tv_nickname.setVisibility(View.GONE);
        }
        if (bean.isIdentified()) {
            tv_identification.setVisibility(View.VISIBLE);
            tv_identification.setText(getString(R.string.chat_tips_identification_tip2, bean.getIdentificationInfo()));
        } else {
            tv_identification.setVisibility(View.GONE);
        }
        if (bean.getIsFriend() == 1) {
            // 好友关系
            sb_dnd.setOpened(bean.getNoDisturbing() == 1);
            sb_stick_top.setOpened(bean.getOnTop() == 1);
            if (!TextUtils.isEmpty(bean.getExtRemark().description)) {
                ll_desc.setVisibility(View.VISIBLE);
                tv_desc.setText(bean.getExtRemark().description);
            } else {
                ll_desc.setVisibility(View.GONE);
            }
            iv_more.setVisibility(View.VISIBLE);
            tv_uid.setVisibility(View.VISIBLE);
            // 2020年2月19日 09:57:58去中心化版本去掉来源
            ll_group_from.setVisibility(View.GONE);
            ll_options.setVisibility(View.VISIBLE);
            tv_send_msg.setVisibility(disableSendBtn ? View.GONE : View.VISIBLE);
            tv_delete_friend.setVisibility(View.VISIBLE);
            tv_add_friend.setVisibility(View.GONE);
            Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.icon_my_edit);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_remark.setCompoundDrawables(null, null, drawable, null);
            tv_remark.setCompoundDrawablePadding(20);
            if (TextUtils.isEmpty(bean.getSource())) {
                ll_from.setVisibility(View.GONE);
            } else {
                ll_from.setVisibility(View.VISIBLE);
                tv_from_type.setText(bean.getSource());
            }
            if (friendBean.getExtRemark().phones != null) {
                phones.clear();
                phones.addAll(friendBean.getExtRemark().phones);
            }
            // 备注电话
            if (phones.size() > 0) {
                rv_phone.setVisibility(View.VISIBLE);
                rv_phone.setLayoutManager(new LinearLayoutManager(this));
                rv_phone.setHasFixedSize(true);
                rv_phone.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL,
                        0.5f, ContextCompat.getColor(this, R.color.chat_color_line)));
                phoneAdapter = new CommonAdapter<RemarkPhone>(this, R.layout.adapter_user_phone, phones) {
                    @Override
                    protected void convert(ViewHolder holder, final RemarkPhone remarkPhone, int position) {
                        if (position == 0) {
                            holder.setVisible(R.id.tv_phone_tips, true);
                        } else {
                            holder.setInVisible(R.id.tv_phone_tips);
                        }
                        holder.setText(R.id.tv_phone, remarkPhone.phone);
                        holder.setText(R.id.tv_phone_remark, remarkPhone.remark);
                        holder.setOnClickListener(R.id.tv_phone, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + remarkPhone.phone));
                                startActivity(intent);
                            }
                        });
                        holder.setOnClickListener(R.id.rl_container, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ARouter.getInstance().build(AppRoute.EDIT_USER_INFO)
                                        .withString("id", friendBean.getId())
                                        .withSerializable("userInfo", friendBean)
                                        .navigation(instance, REQUEST_MODIFY_REMARK);
                            }
                        });
                    }
                };
                rv_phone.setAdapter(phoneAdapter);
            } else {
                rv_phone.setVisibility(View.GONE);
            }
            // 备注描述
            if (!TextUtils.isEmpty(friendBean.getExtRemark().description)) {
                ll_desc.setVisibility(View.VISIBLE);
                tv_desc.setText(friendBean.getExtRemark().description);
            } else {
                ll_desc.setVisibility(View.GONE);
            }
            // 备注图片
            if (friendBean.getExtRemark().images != null) {
                images.clear();
                images.addAll(friendBean.getExtRemark().images);
            }
            if (images.size() > 0) {
                ll_picture.setVisibility(View.VISIBLE);
                rv_picture.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                imageAdapter = new CommonAdapter<String>(this, R.layout.adapter_user_image, images) {
                    @Override
                    protected void convert(ViewHolder holder, final String remarkImage, int position) {
                        GlideApp.with(instance).load(new SingleKeyEncrypt(remarkImage, CipherManager.getPublicKey()))
                                .apply(new RequestOptions().placeholder(R.drawable.bg_image_placeholder))
                                .into((ImageView) holder.getView(R.id.iv_image));
                        holder.setOnClickListener(R.id.iv_image, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int index = images.indexOf(remarkImage);
                                Intent intent = new Intent(instance, ShowBigImageActivity.class);
                                intent.putStringArrayListExtra("images", (ArrayList<String>) images);
                                intent.putExtra("currentIndex", index);
                                instance.startActivity(intent,
                                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                                instance, v, "shareImage").toBundle());
                            }
                        });
                    }
                };
                rv_picture.setAdapter(imageAdapter);
            } else {
                ll_picture.setVisibility(View.GONE);
            }
            if (ll_desc.getVisibility() == View.VISIBLE && ll_picture.getVisibility() == View.VISIBLE) {
                v_divide.setVisibility(View.VISIBLE);
            } else {
                v_divide.setVisibility(View.GONE);
            }

            if (rv_phone.getVisibility() == View.VISIBLE
                    || ll_desc.getVisibility() == View.VISIBLE
                    || ll_picture.getVisibility() == View.VISIBLE) {
                ll_remark_info.setVisibility(View.VISIBLE);
            } else {
                ll_remark_info.setVisibility(View.GONE);
            }
        } else {
            iv_more.setVisibility(View.GONE);
            tv_send_msg.setVisibility(View.GONE);
            tv_delete_friend.setVisibility(View.GONE);
            ll_group_from.setVisibility(View.GONE);
            if (friendBean.getAddress().equals(UserInfo.getInstance().uid)) {
                tv_add_friend.setVisibility(View.GONE);
            } else {
                if (canAddFriend) {
                    tv_uid.setVisibility(View.VISIBLE);
                    tv_add_friend.setText(R.string.chat_action_add_friend);
                    tv_add_friend.setEnabled(true);
                } else {
                    tv_uid.setVisibility(View.GONE);
                    tv_add_friend.setText(R.string.chat_tips_user_cant_add);
                    tv_add_friend.setEnabled(false);
                }
                tv_add_friend.setVisibility(View.VISIBLE);
            }
            tv_remark.setCompoundDrawables(null, null, null, null);

        }
        if (!TextUtils.isEmpty(roomId)) {
            viewModel.getRoomUserInfo(roomId, friendBean.getId());
        }
        friendBean.setIsBlocked(viewModel.isBlock(friendBean.getId()) ? 1 : 0);
        initBlackViewData();
    }

    private void initRoomUserData(RoomUserBean roomUserBean) {
        roomUser = roomUserBean;
        if (roomUserBean.getMemberLevel() == LEVEL_USER) {
            if (roomUserBean.getRoomMutedType() == 1) {
                tv_mute_state.setText(R.string.chat_tips_mute_state1);
                ll_mute.setVisibility(View.VISIBLE);
                ll_group_settings.setVisibility(View.VISIBLE);
            } else if (roomUserBean.getRoomMutedType() == 2) {
                if (roomUserBean.getMutedType() == 2) {
                    if (roomUserBean.getDeadline() == AppConst.TIME_FOREVER) {
                        tv_mute_state.setText(R.string.chat_tips_mute_state2);
                    } else {
                        timer = new MuteCountDown(roomUserBean.getDeadline() - System.currentTimeMillis(), 1000L);
                        timer.start();
                    }
                    ll_mute.setVisibility(View.VISIBLE);
                    ll_group_settings.setVisibility(View.VISIBLE);
                } else {
                    tv_mute_state.setText(R.string.chat_tips_mute_state1);
                    ll_mute.setVisibility(View.VISIBLE);
                    ll_group_settings.setVisibility(View.VISIBLE);
                }
            } else if (roomUserBean.getRoomMutedType() == 3) {
                if (roomUserBean.getMutedType() == 3) {
                    tv_mute_state.setText(R.string.chat_tips_mute_state1);
                    ll_mute.setVisibility(View.VISIBLE);
                    ll_group_settings.setVisibility(View.VISIBLE);
                } else {
                    if (roomUserBean.getDeadline() == AppConst.TIME_FOREVER) {
                        tv_mute_state.setText(R.string.chat_tips_mute_state2);
                    } else {
                        timer = new MuteCountDown(roomUserBean.getDeadline() - System.currentTimeMillis(), 1000L);
                        timer.start();
                    }
                    ll_mute.setVisibility(View.VISIBLE);
                    ll_group_settings.setVisibility(View.VISIBLE);
                }
            } else if (roomUserBean.getRoomMutedType() == 4) {
                tv_mute_state.setText(R.string.chat_tips_mute_state3);
                ll_mute.setVisibility(View.VISIBLE);
                ll_group_settings.setVisibility(View.VISIBLE);
            }
        } else {
            ll_mute.setVisibility(View.GONE);
            ll_group_settings.setVisibility(View.GONE);
        }
    }

    private void initBlackViewData() {
        if(friendBean.getIsFriend() == 1) {
            ll_options.setVisibility(View.VISIBLE);
            ll_friend_options.setVisibility(View.VISIBLE);
            ll_black.setVisibility(View.VISIBLE);
        } else if(friendBean.isBlocked()) {
            ll_options.setVisibility(View.VISIBLE);
            ll_friend_options.setVisibility(View.GONE);
            ll_black.setVisibility(View.VISIBLE);
        } else {
            ll_options.setVisibility(View.GONE);
            ll_friend_options.setVisibility(View.GONE);
            ll_black.setVisibility(View.GONE);
        }
        if(friendBean.isBlocked()) {
            tv_black_state.setText(R.string.chat_remove_black_list);
            tv_black_message.setText(R.string.chat_user_black_message);
        } else {
            tv_black_state.setText(R.string.chat_add_black_list);
            tv_black_message.setText("");
        }
    }

    @Override
    protected void setEvent() {
        tv_remark.setOnClickListener(this);
        ll_desc.setOnClickListener(this);
        tv_send_msg.setOnClickListener(this);
        tv_delete_friend.setOnClickListener(this);
        tv_add_friend.setOnClickListener(this);
        iv_avatar.setOnClickListener(this);
        ll_mute.setOnClickListener(this);
        ll_black.setOnClickListener(this);
        ll_chat_history.setOnClickListener(this);
        ll_chat_file.setOnClickListener(this);
        ll_picture.setOnClickListener(this);
        sb_dnd.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {
                view.toggleSwitch(true);
                setDND(view, 1);
            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                setDND(view, 2);
            }
        });
        sb_stick_top.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {
                view.toggleSwitch(true);
                stickyOnTop(view, 1);
            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                stickyOnTop(view, 2);
            }
        });
    }

    private void setDND(final SwitchView view, final int dnd) {
        if (friendBean == null) {
            return;
        }
        viewModel.friendNoDisturb(friendBean.getId(), dnd);
    }

    private void stickyOnTop(final SwitchView view, final int sticky) {
        if (friendBean == null) {
            return;
        }
        viewModel.friendStickyOnTop(friendBean.getId(), sticky);
    }

    @Override
    public void onClick(View v) {
        if (friendBean == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.iv_avatar) {
            Intent intent = new Intent(this, LargePhotoActivity.class);
            intent.putExtra(LargePhotoActivity.IMAGE_URL, friendBean.getAvatar());
            intent.putExtra(LargePhotoActivity.CHANNEL_TYPE, Chat33Const.CHANNEL_FRIEND);
            startActivity(intent, ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, v, "shareImage").toBundle());
        } else if (id == R.id.ll_chat_history) {
            ARouter.getInstance()
                    .build(AppRoute.SEARCH_LOCAL_SCOPE)
                    .withInt("scope", SearchScope.CHATLOG)
                    .withSerializable("chatTarget", new ChatTarget(Chat33Const.CHANNEL_FRIEND, userId))
                    .withBoolean("popKeyboard", true)
                    .navigation();
        } else if (id == R.id.ll_chat_file) {
            ARouter.getInstance()
                    .build(AppRoute.CHAT_FILE)
                    .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                    .withString("targetId", friendBean.getId())
                    .navigation();
        } else if (id == R.id.tv_send_msg) {
            ARouter.getInstance()
                    .build(AppRoute.CHAT)
                    .withBoolean("isGroupChat", false)
                    .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                    .withString("targetName", friendBean.getDisplayName())
                    .withString("targetId", friendBean.getId())
                    .navigation();
            LiveBus.of(BusEvent.class).changeTab().setValue(new ChangeTabEvent(0, 1));
            finish();
        } else if (id == R.id.tv_delete_friend) {
            String content = getString(R.string.chat_dialog_delete_friend, AppConfig.APP_ACCENT_COLOR_STR, friendBean.getDisplayName());
            EasyDialog dialog = new EasyDialog.Builder()
                    .setHeaderTitle(getString(R.string.chat_tips_tips))
                    .setBottomLeftText(getString(R.string.chat_action_cancel))
                    .setBottomRightText(getString(R.string.chat_action_confirm))
                    .setContent(Html.fromHtml(content))
                    .setBottomLeftClickListener(null)
                    .setBottomRightClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog) {
                            dialog.dismiss();
                            viewModel.deleteFriend(friendBean.getId(), friendBean.getAddress());
                        }
                    }).create(this);
            dialog.show();
        } else if (id == R.id.tv_add_friend) {
            viewModel.addFriend(friendBean.getId(), "", sourceType, sourceId, friendBean.getAddress());
            /*if (friendBean.getNeedConfirm() == 1) {
                if (friendBean.getNeedAnswer() == 1) {
                    mDialog = new AddVerifyDialog.Builder(this)
                            .setContent(friendBean.getQuestion())
                            .setRightButton(new AddVerifyDialog.OnSubmitListener() {
                                @Override
                                public void onSubmit(final Dialog dialog, final String content) {
                                    viewModel.checkAnswer(friendBean.getId(), content);
                                }
                            }).show();
                } else {
                    ARouter.getInstance().build(AppRoute.FRIEND_VERIFY)
                            .withString("id", friendBean.getId())
                            .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                            .withInt("sourceType", sourceType)
                            .withString("sourceId", sourceId)
                            .navigation();
                }
            } else {
                if (friendBean.getNeedAnswer() == 1) {
                    mDialog = new AddVerifyDialog.Builder(this)
                            .setContent(friendBean.getQuestion())
                            .setRightButton(new AddVerifyDialog.OnSubmitListener() {
                                @Override
                                public void onSubmit(final Dialog dialog, final String content) {
                                    viewModel.addFriend(friendBean.getId(), "", content, sourceType, sourceId);
                                }
                            }).show();
                } else {
                    viewModel.addFriend(friendBean.getId(), "", sourceType, sourceId);
                }
            }*/
        } else if (id == R.id.ll_desc || id == R.id.tv_remark || id == R.id.ll_picture) {
            if (friendBean.getIsFriend() == 1) {
                ARouter.getInstance().build(AppRoute.EDIT_USER_INFO)
                        .withString("id", friendBean.getId())
                        .withSerializable("userInfo", friendBean)
                        .navigation(this, REQUEST_MODIFY_REMARK);
            }
        } else if (id == R.id.ll_mute) {
            if (memberLevel == LEVEL_USER || roomUser == null) {
                return;
            }
            final MutePopupWindow mutePopupWindow = new MutePopupWindow(this, LayoutInflater.from(this).inflate(R.layout.popup_sustom_service_operation, null));
            mutePopupWindow.setTitle(roomUser.getDisplayName(), ContextCompat.getColor(this, R.color.chat_color_accent));
            boolean show = false;
            if (roomUser.getMemberLevel() == LEVEL_USER) {
                if (roomUser.getRoomMutedType() == 1) {
                    show = false;
                } else if (roomUser.getRoomMutedType() == 2) {
                    show = roomUser.getMutedType() == 2;
                } else if (roomUser.getRoomMutedType() == 3) {
                    show = roomUser.getMutedType() != 3;
                } else if (roomUser.getRoomMutedType() == 4) {
                    show = true;
                }
            }
            mutePopupWindow.showCancelButton(show);
            if (show) {
                dialogTimer = new CountDownTimer(roomUser.getDeadline() - System.currentTimeMillis(), 1000L) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        mutePopupWindow.setCountDownText(getString(R.string.chat_tips_mute_state4, com.fzm.chat33.utils.StringUtils.formatMutedTime(millisUntilFinished)), true);
                    }

                    @Override
                    public void onFinish() {
                        mutePopupWindow.setCountDownText("", false);
                        mutePopupWindow.showCancelButton(false);
                    }
                };
                dialogTimer.start();
            }
            mutePopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            mutePopupWindow.setOnTimeSelectListener(new MutePopupWindow.OnTimeSelectListener() {
                @Override
                public void onTimeSelect(final long time) {
                    viewModel.setMutedSingle(roomId, friendBean.getId(), time);
                }
            });
            mutePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (dialogTimer != null) {
                        dialogTimer.cancel();
                    }
                }
            });
            mutePopupWindow.showAtLocation(ll_mute, Gravity.BOTTOM, 0, 0);
        } else if(id == R.id.ll_black) {
            if(friendBean.isBlocked()) {
                viewModel.unblockUser(friendBean.getId(), friendBean.getAddress());
            } else {
                String content = getString(R.string.chat_add_black_warn, friendBean.getDisplayName());
                EasyDialog dialog = new EasyDialog.Builder()
                        .setHeaderTitle(getString(R.string.chat_tips_tips))
                        .setBottomLeftText(getString(R.string.chat_action_cancel))
                        .setBottomRightText(getString(R.string.chat_action_confirm))
                        .setContent(Html.fromHtml(content))
                        .setBottomLeftClickListener(null)
                        .setBottomRightClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog) {
                                dialog.dismiss();
                                viewModel.blockUser(friendBean.getId(), friendBean.getAddress());
                            }
                        }).create(this);
                dialog.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_MODIFY_REMARK) {
                fetchData();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (dialogTimer != null) {
            dialogTimer.cancel();
        }
    }

    /**
     * 倒计时控制类
     */
    class MuteCountDown extends CountDownTimer {

        public MuteCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tv_mute_state.setText(getString(R.string.chat_tips_mute_state5, com.fzm.chat33.utils.StringUtils.formatMutedTime(millisUntilFinished)));
        }

        @Override
        public void onFinish() {
            tv_mute_state.setText(R.string.chat_tips_mute_state1);
        }
    }
}
