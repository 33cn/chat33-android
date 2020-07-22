package com.fzm.chat33.main.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.bus.LiveBus;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.app.BusEvent;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.event.NicknameRefreshEvent;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.main.mvvm.SettingViewModel;
import com.fzm.chat33.utils.SimpleTextWatcher;
import com.fuzamei.componentservice.widget.CommonTitleBar;

import javax.inject.Inject;

/**
 * @author zhengjy
 * @since 2018/10/23
 * Description:修改自己昵称，编辑群名称页面
 */
@Route(path = AppRoute.EDIT_NAME)
public class EditInfoActivity extends DILoadableActivity implements View.OnClickListener {

    @Autowired
    public String id;
    @Autowired
    public String name;
    @Autowired
    public String desc;

    private int channelType;

    private CommonTitleBar ctb_title;
    private TextView tv_name_tips, tv_name_count;
    private EditText et_name;
    private View tv_submit;
    private boolean editSelf;
    @Inject
    public ViewModelProvider.Factory provider;
    private SettingViewModel viewModel;


    @Override
    protected boolean enableSlideBack() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        ARouter.getInstance().inject(this);
        editSelf = TextUtils.isEmpty(id);
        channelType = editSelf ? Chat33Const.CHANNEL_FRIEND : Chat33Const.CHANNEL_ROOM;
        return R.layout.activity_edit_self;
    }

    @Override
    protected void initView() {
        viewModel = ViewModelProviders.of(this, provider).get(SettingViewModel.class);
        ctb_title = findViewById(R.id.ctb_title);
        tv_submit = findViewById(R.id.tv_submit);
        et_name = findViewById(R.id.et_name);
        tv_name_tips = findViewById(R.id.tv_name_tips);
        tv_name_count = findViewById(R.id.tv_name_count);

        viewModel.getLoading().observe(this, this::setupLoading);
        viewModel.getEditName().observe(this, it-> {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_edit_success));
            if (!editSelf) {
                // 设置群名
                LiveBus.of(BusEvent.class).nicknameRefresh()
                        .setValue(new NicknameRefreshEvent(id, it));
                LiveBus.of(BusEvent.class).contactsRefresh().setValue(2);
                RoomUtils.run(() -> ChatDatabase.getInstance().roomsDao().updateName(id, it));
            } else {
                // 设置自己的昵称
                UserInfo.getInstance().setUsername(it);
                LiveBus.of(BusEvent.class).nicknameRefresh().setValue(new NicknameRefreshEvent(it));
            }
            dismiss();
            finish();
        });
    }

    @Override
    protected void initData() {
        ctb_title.setMiddleText(getString(R.string.chat_title_edit_info));
        ctb_title.setRightVisible(false);
        ctb_title.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (editSelf) {
            tv_name_tips.setText(R.string.chat_tips_edit_info1);
            et_name.setHint(R.string.chat_tips_edit_info2);
        } else {
            tv_name_tips.setText(R.string.chat_tips_edit_info3);
            et_name.setHint(R.string.chat_tips_edit_info4);
        }
        if (!TextUtils.isEmpty(name)) {
            if (name.length() > 20) {
                name = name.substring(0, 20);
            }
            et_name.setText(name);
            et_name.setSelection(name.length());
            tv_name_count.setText(getString(R.string.chat_tips_num_20, name.length()));
        }
    }

    @Override
    protected void setEvent() {
        tv_submit.setOnClickListener(this);
        et_name.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    tv_name_count.setText(getString(R.string.chat_tips_num_20, 0));
                } else {
                    tv_name_count.setText(getString(R.string.chat_tips_num_20, s.length()));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tv_submit) {
            final String newName = et_name.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_edit_group_empty));
                return;
            }
            if (!newName.equals(name)) {
                viewModel.editName(channelType, id, newName);
            } else {
                finish();
            }
        }
    }
}
