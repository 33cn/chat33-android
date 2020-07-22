package com.fzm.chat33.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.common.utils.KeyboardUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.common.view.ScrollPagerAdapter;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.param.DecryptParams;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.Contact;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.core.net.OssModel;
import com.fzm.chat33.core.request.chat.PreForwardRequest;
import com.fzm.chat33.global.AppConst;
import com.fzm.chat33.main.fragment.SelectFriendFragment;
import com.fzm.chat33.main.fragment.SelectGroupFragment;
import com.fzm.chat33.main.fragment.SelectRecentFragment;
import com.fzm.chat33.main.fragment.SelectSearchFragment;
import com.fzm.chat33.main.mvvm.ContactSelectViewModel;
import com.fzm.chat33.utils.FileUtils;
import com.fzm.chat33.widget.ChatAvatarView;
import com.fzm.chat33.widget.ChatSearchView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * @author zhengjy
 * @since 2018/12/26
 * Description:转发联系人选择，支持多选
 */
@Route(path = AppRoute.CONTACT_SELECT, extras = AppConst.NEED_LOGIN)
public class ContactSelectActivity extends DILoadableActivity implements View.OnClickListener {

    public static final int FORWARD_SELECT = 1;
    public static final int ADDRESS_SELECT = 2;

    public static final int REQUEST_CONTACT = 100;

    private View ll_selected, tv_return, rl_confirm;
    private ImageView iv_search;
    private ChatSearchView searchView;
    private RecyclerView rv_select;
    private CommonAdapter<Contact> adapter;
    private List<Contact> mContacts = new ArrayList<>();
    private TextView tv_select_count, tv_often, tv_friend, tv_group;
    private ViewPager vp_contact;
    private FrameLayout fl_search;
    private List<Fragment> fragments;

    private List<String> titles = new ArrayList<>();
    private ScrollPagerAdapter pagerAdapter;

    private SelectRecentFragment recentFragment;
    private SelectFriendFragment friendFragment;
    private SelectGroupFragment groupFragment;
    private SelectSearchFragment searchFragment;

    private List<Contact> contactList = new ArrayList<>();
    private List<String> friendIds = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();

    @Inject
    public ViewModelProvider.Factory provider;
    private ContactSelectViewModel viewModel;

    @Autowired
    public int selectType = FORWARD_SELECT;
    @Autowired
    public DecryptParams params;
    @Autowired
    public PreForwardRequest preForward;
    @Autowired
    public ChatFile chatFile;
    @Autowired
    public boolean multiChoose = true;
    @Autowired
    public Bundle data;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        selectType = getIntent().getIntExtra("selectType", FORWARD_SELECT);
        preForward = (PreForwardRequest) getIntent().getSerializableExtra("preForward");
        chatFile = (ChatFile) getIntent().getSerializableExtra("chatFile");
        multiChoose = getIntent().getBooleanExtra("multiChoose", true);
        data = getIntent().getBundleExtra("data");
        if (data != null && chatFile == null) {
            chatFile = (ChatFile) data.getSerializable("chatFile");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_contact_select;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        viewModel = ViewModelProviders.of(this, provider).get(ContactSelectViewModel.class);
        if (data != null && chatFile == null) {
            chatFile = (ChatFile) data.getSerializable("chatFile");
        }
        ll_selected = findViewById(R.id.ll_selected);
        tv_return = findViewById(R.id.tv_return);
        rv_select = findViewById(R.id.rv_select);
        tv_often = findViewById(R.id.tv_often);
        tv_friend = findViewById(R.id.tv_friend);
        tv_group = findViewById(R.id.tv_group);
        vp_contact = findViewById(R.id.vp_contact);
        fl_search = findViewById(R.id.fl_search);
        tv_select_count = findViewById(R.id.tv_select_count);
        rl_confirm = findViewById(R.id.rl_confirm);

        ll_selected.setVisibility(multiChoose ? View.VISIBLE : View.GONE);
        rl_confirm.setVisibility(multiChoose ? View.VISIBLE : View.GONE);
        if (selectType == FORWARD_SELECT) {
            tv_group.setVisibility(View.VISIBLE);
        } else if (selectType == ADDRESS_SELECT) {
            tv_group.setVisibility(View.GONE);
        }
        iv_search = findViewById(R.id.iv_search);
        iv_search.setOnClickListener(this);
        searchView = findViewById(R.id.chat_search);
        searchView.setOnSearchCancelListener(() -> {
            KeyboardUtils.hideKeyboard(searchView.getFocusView());
            searchView.reduce();
            fl_search.setVisibility(View.GONE);
        });
        searchView.setOnTextChangeListener(this::searchKeyword);
        if (multiChoose) {
            rv_select.setLayoutManager(new LinearLayoutManager(instance, LinearLayoutManager.HORIZONTAL, false));
            adapter = new CommonAdapter<Contact>(instance, R.layout.adapter_select_contact, mContacts) {
                @Override
                protected void convert(final ViewHolder holder, Contact contact, int position) {
                    if (contact.channelType() == Chat33Const.CHANNEL_ROOM) {
                        Glide.with(mContext).load(contact.getAvatar())
                                .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_room))
                                .into((ImageView) holder.getView(R.id.head));
                        ((ChatAvatarView) holder.getView(R.id.head)).setIconRes(
                                !TextUtils.isEmpty(contact.getIdentificationInfo()) ? R.drawable.ic_group_identified : -1);
                    } else if (contact.channelType() == Chat33Const.CHANNEL_FRIEND) {
                        Glide.with(mContext).load(contact.getAvatar())
                                .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_round))
                                .into((ImageView) holder.getView(R.id.head));
                        ((ChatAvatarView) holder.getView(R.id.head)).setIconRes(
                                !TextUtils.isEmpty(contact.getIdentificationInfo()) ? R.drawable.ic_user_identified : -1);
                    }
                }
            };
            adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    Contact data = mContacts.get(position);
                    if (data.channelType() == Chat33Const.CHANNEL_FRIEND) {
                        friendFragment.removeCheck(data.getId());
                        contactList.remove(data);
                        friendIds.remove(data.getId());
                    } else if (mContacts.get(position).channelType() == Chat33Const.CHANNEL_ROOM) {
                        groupFragment.removeCheck(data.getId());
                        groupIds.remove(data.getId());
                    }
                    recentFragment.removeCheck(data.getId(), data.channelType());
                    searchFragment.removeCheck(data.getId(), data.channelType());
                    mContacts.remove(position);
                    adapter.notifyItemRangeRemoved(position, 1);
                    rv_select.scrollToPosition(mContacts.size() - 1);
                    tv_select_count.setText(String.valueOf(mContacts.size()));
                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                    return false;
                }
            });
            rv_select.setAdapter(adapter);
        }
        setupFragments();
        viewModel.getForwardResult().observe(this, response -> {
            if (response != null) {
                dismiss();
                if (response.state == 0) {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_contact_select1));
                } else {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_contact_select3, response.state));
                }
                setResult(RESULT_OK);
                finish();
            }
        });
        viewModel.getForwardSingle().observe(this, o -> {
            dismiss();
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_contact_select1));
            setResult(RESULT_OK);
            finish();
        });
        viewModel.getLoading().observe(this, this::setupLoading);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void setEvent() {
        tv_return.setOnClickListener(this);
        rl_confirm.setOnClickListener(this);
        tv_often.setOnClickListener(this);
        tv_friend.setOnClickListener(this);
        tv_group.setOnClickListener(this);
    }

    private void checkFriend(Contact bean) {
        if (!friendIds.contains(bean.getId())) {
            friendIds.add(bean.getId());
        }
        if (!contactList.contains(bean)) {
            contactList.add(bean);
        }
        recentFragment.addCheck(bean.getId(), bean.channelType());
        searchFragment.addCheck(bean.getId(), bean.channelType());
        friendFragment.checkFriend(bean.getId());
        if (multiChoose) {
            if (notExist(bean)) {
                mContacts.add(bean);
                adapter.notifyItemInserted(mContacts.size() - 1);
            }
        } else {
            try {
                Intent intent = new Intent();
                intent.putExtra("contact", (Serializable) contactList.get(0));
                setResult(RESULT_OK, intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        }
    }

    /**
     * 取消选中好友
     */
    private void uncheckFriend(Contact bean) {
        contactList.add(bean);
        friendIds.remove(bean.getId());
        recentFragment.removeCheck(bean.getId(), bean.channelType());
        searchFragment.removeCheck(bean.getId(), bean.channelType());
        friendFragment.removeCheck(bean.getId());
        int index = indexOf(bean);
        remove(bean);
        if (multiChoose) {
            if (index != -1) {
                adapter.notifyItemRangeRemoved(index, 1);
            }
        }
    }

    private void checkGroup(Contact bean) {
        if (!groupIds.contains(bean.getId())) {
            groupIds.add(bean.getId());
        }
        recentFragment.addCheck(bean.getId(), bean.channelType());
        searchFragment.addCheck(bean.getId(), bean.channelType());
        groupFragment.checkGroup(bean.getId());
        if (multiChoose) {
            if (notExist(bean)) {
                mContacts.add(bean);
                adapter.notifyItemInserted(mContacts.size() - 1);
            }
        }
    }

    /**
     * 取消选中群
     */
    private void uncheckGroup(Contact bean) {
        groupIds.remove(bean.getId());
        recentFragment.removeCheck(bean.getId(), bean.channelType());
        searchFragment.removeCheck(bean.getId(), bean.channelType());
        groupFragment.removeCheck(bean.getId());
        int index = indexOf(bean);
        remove(bean);
        if (multiChoose) {
            if (index != -1) {
                adapter.notifyItemRangeRemoved(index, 1);
            }
        }
    }

    /**
     * 列表中是否存在联系人
     */
    private boolean notExist(Contact contact) {
        for (Contact item : mContacts) {
            if (item.getKey().equals(contact.getKey())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 移除列表中的联系人
     */
    private void remove(Contact contact) {
        for (Contact item : mContacts) {
            if (item.getKey().equals(contact.getKey())) {
                mContacts.remove(item);
                return;
            }
        }
    }

    /**
     * 联系人在列表中的位置
     */
    private int indexOf(Contact contact) {
        for (int i = 0; i < mContacts.size(); i++) {
            if (mContacts.get(i).getKey().equals(contact.getKey())) {
                return i;
            }
        }
        return -1;
    }

    private void setupFragments() {
        fragments = new ArrayList<>();
        recentFragment = new SelectRecentFragment();
        recentFragment.setSelectable(multiChoose);
        recentFragment.setSelectType(selectType);
        recentFragment.setOnCheckChangeListener((view, checked, bean) -> {
            if (checked) {
                if (bean.channelType() == Chat33Const.CHANNEL_ROOM) {
                    checkGroup(bean);
                } else if (bean.channelType() == Chat33Const.CHANNEL_FRIEND) {
                    checkFriend(bean);
                }
            } else {
                if (bean.channelType() == Chat33Const.CHANNEL_ROOM) {
                    uncheckGroup(bean);
                } else if (bean.channelType() == Chat33Const.CHANNEL_FRIEND) {
                    uncheckFriend(bean);
                }
            }
            rv_select.scrollToPosition(mContacts.size() - 1);
            tv_select_count.setText(String.valueOf(mContacts.size()));
        });
        fragments.add(recentFragment);
        titles.add("");
        friendFragment = new SelectFriendFragment();
        friendFragment.setSelectable(multiChoose);
        friendFragment.setOnCheckChangeListener((view, checked, bean) -> {
            if (checked) {
                checkFriend(bean);
            } else {
                uncheckFriend(bean);
            }
            rv_select.scrollToPosition(mContacts.size() - 1);
            tv_select_count.setText(String.valueOf(mContacts.size()));
        });
        fragments.add(friendFragment);
        titles.add("");
        if (selectType == FORWARD_SELECT) {
            groupFragment = new SelectGroupFragment();
            groupFragment.setSelectable(multiChoose);
            groupFragment.setOnCheckChangeListener((view, checked, bean) -> {
                if (checked) {
                    checkGroup(bean);
                } else {
                    uncheckGroup(bean);
                }
                rv_select.scrollToPosition(mContacts.size() - 1);
                tv_select_count.setText(String.valueOf(mContacts.size()));
            });
            fragments.add(groupFragment);
            titles.add("");
        }
        pagerAdapter = new ScrollPagerAdapter(getSupportFragmentManager(), titles, fragments);
        vp_contact.setAdapter(pagerAdapter);
        vp_contact.setOffscreenPageLimit(2);
        vp_contact.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switchChoose(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    friendFragment.toggleSideBar(true);
                } else {
                    friendFragment.toggleSideBar(false);
                }
            }
        });
        switchChoose(0);
        vp_contact.setCurrentItem(0);
        searchFragment = new SelectSearchFragment();
        searchFragment.setSelectable(multiChoose);
        searchFragment.setSelectType(selectType);
        searchFragment.setOnCheckChangeListener((view, checked, bean) -> {
            if (checked) {
                if (bean.channelType() == Chat33Const.CHANNEL_ROOM) {
                    checkGroup(bean);
                } else if (bean.channelType() == Chat33Const.CHANNEL_FRIEND) {
                    checkFriend(bean);
                }
            } else {
                if (bean.channelType() == Chat33Const.CHANNEL_ROOM) {
                    uncheckGroup(bean);
                } else if (bean.channelType() == Chat33Const.CHANNEL_FRIEND) {
                    uncheckFriend(bean);
                }
            }
            rv_select.scrollToPosition(mContacts.size() - 1);
            tv_select_count.setText(String.valueOf(mContacts.size()));
        });
        getSupportFragmentManager().beginTransaction().add(R.id.fl_search, searchFragment).commit();
    }

    private void switchChoose(int index) {
        switch (index) {
            case 0:
                tv_often.setTextColor(ContextCompat.getColor(this, R.color.chat_color_accent));
                tv_often.setBackgroundResource(R.drawable.shape_common_table);
                tv_friend.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
                tv_friend.setBackgroundResource(0);
                tv_group.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
                tv_group.setBackgroundResource(0);
                break;
            case 1:
                tv_often.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
                tv_often.setBackgroundResource(0);
                tv_friend.setTextColor(ContextCompat.getColor(this, R.color.chat_color_accent));
                tv_friend.setBackgroundResource(R.drawable.shape_common_table);
                tv_group.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
                tv_group.setBackgroundResource(0);
                break;
            case 2:
                tv_often.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
                tv_often.setBackgroundResource(0);
                tv_friend.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light));
                tv_friend.setBackgroundResource(0);
                tv_group.setTextColor(ContextCompat.getColor(this, R.color.chat_color_accent));
                tv_group.setBackgroundResource(R.drawable.shape_common_table);
                break;
            default:
                break;
        }
    }

    private void forwardMessage() {
        viewModel.forwardSingleMessage(params, chatFile, friendIds, groupIds);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_return) {
            finish();
        } else if (id == R.id.rl_confirm) {
            if (friendIds.size() == 0 && groupIds.size() == 0) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_contact_select2));
                return;
            }
            if (chatFile != null) {
                if (chatFile.getChatFileType() == ChatMessage.Type.IMAGE && TextUtils.isEmpty(chatFile.getImageUrl())) {
                    compressImage();
                } else if (chatFile.getChatFileType() == ChatMessage.Type.VIDEO && TextUtils.isEmpty(chatFile.getMediaUrl())) {
                    uploadAndSendFile(OssModel.VIDEO);
                } else if (chatFile.getChatFileType() == ChatMessage.Type.FILE && TextUtils.isEmpty(chatFile.fileUrl)) {
                    uploadAndSendFile(OssModel.FILE);
                } else {
                    forwardMessage();
                }
                return;
            }
            UserInfo info = viewModel.getCurrentUser().getValue();
            if (info != null) {
                preForward.setForwardUsername(info.username);
            } else {
                preForward.setForwardUsername("");
            }
            if (AppConfig.APP_ENCRYPT) {
                viewModel.forwardEncryptMessage(preForward, friendIds, groupIds);
            } else {
                viewModel.forwardMessage(preForward, friendIds, groupIds);
            }
        } else if (id == R.id.tv_often) {
            vp_contact.setCurrentItem(0);
            switchChoose(0);
        } else if (id == R.id.tv_friend) {
            vp_contact.setCurrentItem(1);
            switchChoose(1);
        } else if (id == R.id.tv_group) {
            vp_contact.setCurrentItem(2);
            switchChoose(2);
        } else if(id == R.id.iv_search) {
            searchView.expand();
            searchView.postDelayed(() -> KeyboardUtils.showKeyboard(searchView.getFocusView()), 100);
            fl_search.setVisibility(View.VISIBLE);
        }
    }

    private void compressImage() {
        if (!new File(chatFile.getLocalPath()).exists()) {
            ShowUtils.showToast(this, getString(R.string.chat_tips_img_not_exist));
            return;
        }
        if (chatFile.getLocalPath().endsWith("gif")) {
            // gif图不压缩
            uploadAndSendFile(OssModel.PICTURE);
        } else {
            Luban.with(this)
                    .load(chatFile.getLocalPath())
                    .ignoreBy(100)
                    .setTargetDir(FileUtils.getImageCachePath(this))
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                            loading(true);
                        }

                        @Override
                        public void onSuccess(File file) {
                            if (!file.exists()) {
                                ShowUtils.showToast(instance, getString(R.string.chat_tips_img_zip_fail));
                                return;
                            }
                            int[] heightWidth = ToolUtils.getLocalImageHeightWidth(file.getAbsolutePath());
                            if(heightWidth[0]<=0 || heightWidth[1]<=0 ){
                                ShowUtils.showToast(instance, getString(R.string.chat_tips_contact_select4));
                                return;
                            }
                            chatFile.setLocalPath(file.getAbsolutePath());
                            uploadAndSendFile(OssModel.PICTURE);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dismiss();
                            ShowUtils.showToast(instance, getString(R.string.chat_tips_img_zip_fail));
                            e.printStackTrace();
                        }
                    }).launch();
        }
    }

    private void uploadAndSendFile(int type) {
        loading(true);
        OssModel.getInstance().uploadMedia(null, chatFile.getLocalPath(), type, new OssModel.UpLoadCallBack() {
            @Override
            public void onSuccess(@NotNull String url) {
                if (type == OssModel.PICTURE) {
                    int[] heightWidth = ToolUtils.getLocalImageHeightWidth(chatFile.getLocalPath());
                    if (heightWidth[0] <= 0 || heightWidth[1] <= 0) {
                        ShowUtils.showToast(instance, getString(R.string.chat_tips_contact_select5));
                    } else {
                        chatFile.setImageUrl(url);
                    }
                } else if (type == OssModel.FILE) {
                    chatFile.fileUrl = url;
                } else if (type == OssModel.VIDEO) {
                    chatFile.setMediaUrl(url);
                }
                forwardMessage();
            }

            @Override
            public void onProgress(long currentSize, long totalSize) {

            }

            @Override
            public void onFailure(@NotNull String path) {
                dismiss();
                ShowUtils.showToast(instance, getString(R.string.chat_tips_contact_select5));
            }
        });
    }

    private void searchKeyword(String keyword) {
        searchFragment.searchKeyword(keyword);
    }

    @Override
    public void onBackPressed() {
        if(!searchView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public interface OnCheckChangedListener {
        void onCheckChanged(View view, boolean checked, Contact bean);
    }
}
