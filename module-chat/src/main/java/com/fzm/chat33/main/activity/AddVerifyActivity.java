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
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.param.AddFriendParam;
import com.fzm.chat33.core.bean.param.JoinGroupParam;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.main.mvvm.AddVerifyViewModel;
import com.fzm.chat33.utils.SimpleTextWatcher;
import com.fuzamei.componentservice.widget.CommonTitleBar;

import javax.inject.Inject;

/**
 * @author zhengjy
 * @since 2018/12/24
 * Description:
 */
@Route(path = AppRoute.FRIEND_VERIFY)
public class AddVerifyActivity extends DILoadableActivity implements View.OnClickListener {

    @Autowired
    public String roomId;
    @Autowired
    public String id;
    @Autowired
    public String answer;
    @Autowired
    public int sourceType;
    @Autowired
    public String sourceId;
    @Autowired
    public int channelType = Chat33Const.CHANNEL_FRIEND;

    private CommonTitleBar ctb_title;
    private TextView tv_verify_count;
    private EditText et_verify_info;
    private View tv_submit;

    @Inject
    public ViewModelProvider.Factory provider;
    private AddVerifyViewModel viewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_verify;
    }

    @Override
    protected boolean enableSlideBack() {
        return true;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        viewModel = ViewModelProviders.of(this, provider).get(AddVerifyViewModel.class);
        ctb_title = findViewById(R.id.ctb_title);
        et_verify_info = findViewById(R.id.et_verify_info);
        tv_verify_count = findViewById(R.id.tv_verify_count);
        tv_submit = findViewById(R.id.tv_submit);
        viewModel.getLoading().observe(this, this::setupLoading);
        viewModel.getAddFriend().observe(this, it -> {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_verify4));
            finish();
        });
        viewModel.getJoinRoomApply().observe(this, it -> {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_verify5));
            finish();
        });
    }

    @Override
    protected void initData() {
        ctb_title.setMiddleText(channelType == Chat33Const.CHANNEL_FRIEND
                ? getString(R.string.chat_title_add_verify1)
                : getString(R.string.chat_title_add_verify2));
        ctb_title.setRightVisible(false);
        ctb_title.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        et_verify_info.setHint(getString(R.string.chat_tips_verify3, UserInfo.getInstance().username));
        et_verify_info.setSelection(et_verify_info.getText().length());
        tv_verify_count.setText(getString(R.string.chat_tips_num_50, et_verify_info.getText().length()));
    }

    @Override
    protected void setEvent() {
        tv_submit.setOnClickListener(this);
        et_verify_info.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    tv_verify_count.setText(getString(R.string.chat_tips_num_50, 0));
                } else {
                    tv_verify_count.setText(getString(R.string.chat_tips_num_50, s.length()));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tv_submit) {
            String reason = et_verify_info.getText().toString().trim();
            if (channelType == Chat33Const.CHANNEL_FRIEND) {
                viewModel.addFriend(new AddFriendParam(id, reason, answer, sourceType, sourceId));
            } else if (channelType == Chat33Const.CHANNEL_ROOM) {
                viewModel.joinRoomApply(new JoinGroupParam(roomId, id, reason, sourceType, sourceId));
            }
        }
    }
}
