package com.fzm.chat33.main.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.param.AddQuestionParam;
import com.fzm.chat33.core.utils.UserInfoPreference;
import com.fzm.chat33.main.mvvm.SettingViewModel;
import com.fuzamei.componentservice.widget.CommonTitleBar;
import com.fzm.chat33.widget.SwitchView;

import javax.inject.Inject;

/**
 * @author zhengjy
 * @since 2018/12/24
 * Description:
 */
@Route(path = AppRoute.SETTING)
public class SettingActivity extends DILoadableActivity implements SwitchView.OnStateChangedListener, View.OnClickListener {

    public static final int REQUEST_VERIFY_QUESTION = 1000;

    private CommonTitleBar ctbTitle;
    private View ll_default_group, ll_clear_cache, rl_verify_question, ll_wallet_protocol;
    private SwitchView sv_need_verify, sv_need_question, sv_confirm_invite, sv_msg_push, sv_msg_detail, sv_sound, sv_vibration;
    private TextView tv_version_question, tv_version_answer;
    private boolean alreadyOpen;

    @Inject
    public ViewModelProvider.Factory provider;
    private SettingViewModel viewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected boolean enableSlideBack() {
        return true;
    }

    @Override
    protected void initView() {
        viewModel = ViewModelProviders.of(this, provider).get(SettingViewModel.class);
        ctbTitle = findViewById(R.id.ctb_title);
        ll_default_group = findViewById(R.id.ll_default_group);
        rl_verify_question = findViewById(R.id.rl_verify_question);
        ll_clear_cache = findViewById(R.id.ll_clear_cache);
        ll_wallet_protocol = findViewById(R.id.ll_wallet_protocol);
        sv_need_verify = findViewById(R.id.sv_need_verify);
        sv_need_question = findViewById(R.id.sv_need_question);
        sv_confirm_invite = findViewById(R.id.sv_confirm_invite);
        sv_msg_push = findViewById(R.id.sv_msg_push);
        sv_msg_detail = findViewById(R.id.sv_msg_detail);
        sv_sound = findViewById(R.id.sv_sound);
        sv_vibration = findViewById(R.id.sv_vibration);
        tv_version_question = findViewById(R.id.tv_version_question);
        tv_version_answer = findViewById(R.id.tv_version_answer);
        viewModel.getLoading().observe(this, this::setupLoading);
        viewModel.getSetAddVerify().observe(this, it -> {
            if(it.getResult() == null) {
                sv_need_verify.toggleSwitch(it.getEnable() != 1);
            } else {
                UserInfoPreference.getInstance().setIntPref(UserInfoPreference.NEED_CONFIRM, it.getEnable());
            }
        });
        viewModel.getSetAddQuestion().observe(this, it -> {
            if(it.getResult() == null) {
                rl_verify_question.setVisibility(View.VISIBLE);
                sv_need_question.toggleSwitch(true);
            } else {
                UserInfoPreference.getInstance().setIntPref(UserInfoPreference.NEED_ANSWER, it.getEnable());
                tv_version_question.setText("");
                tv_version_answer.setText("");
            }
        });
        viewModel.getSetInviteConfirm().observe(this, it -> {
            if(it.getResult() == null) {
                sv_confirm_invite.toggleSwitch(it.getEnable() != 1);
            } else {
                UserInfoPreference.getInstance().setIntPref(UserInfoPreference.NEED_CONFIRM_INVITE, it.getEnable());
            }
        });
    }

    @Override
    protected void initData() {
        ctbTitle.setMiddleText(getString(R.string.chat_title_settings));
        ctbTitle.setRightVisible(false);
        int needVerify = UserInfoPreference.getInstance().getIntPref(UserInfoPreference.NEED_CONFIRM, 1);
        sv_need_verify.setOpened(needVerify == 1);
        int needAnswer = UserInfoPreference.getInstance().getIntPref(UserInfoPreference.NEED_ANSWER, 2);
        sv_need_question.setOpened(needAnswer == 1);
        if (needAnswer == 1) {
            String question = UserInfoPreference.getInstance().getStringPref(UserInfoPreference.VERIFY_QUESTION, "");
            String answer = UserInfoPreference.getInstance().getStringPref(UserInfoPreference.VERIFY_ANSWER, "");
            tv_version_question.setText(question);
            tv_version_answer.setText(answer);
            rl_verify_question.setVisibility(View.VISIBLE);
        } else {
            rl_verify_question.setVisibility(View.GONE);
            tv_version_question.setText("");
            tv_version_answer.setText("");
        }
        int needConfirmInvite = UserInfoPreference.getInstance().getIntPref(UserInfoPreference.NEED_CONFIRM_INVITE, 2);
        sv_confirm_invite.setOpened(needConfirmInvite == 1);

    }

    @Override
    protected void setEvent() {
        ctbTitle.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        sv_need_verify.setOnStateChangedListener(this);
        sv_need_question.setOnStateChangedListener(this);
        sv_confirm_invite.setOnStateChangedListener(this);
        sv_msg_push.setOnStateChangedListener(this);
        sv_msg_detail.setOnStateChangedListener(this);
        sv_sound.setOnStateChangedListener(this);
        sv_vibration.setOnStateChangedListener(this);
        rl_verify_question.setOnClickListener(this);
        ll_wallet_protocol.setOnClickListener(this);
    }

    private void setAddVerify(final int enable) {
        viewModel.setAddVerify(enable);
    }

    private void setAddQuestion(final int enable) {
        if (enable == 1) {
            alreadyOpen = false;
            ARouter.getInstance().build(AppRoute.VERIFY_QUESTION).navigation(this, REQUEST_VERIFY_QUESTION);
        } else {
            rl_verify_question.setVisibility(View.GONE);
            viewModel.setAddQuestion(new AddQuestionParam(), enable);
        }
    }

    private void setInviteConfirm(final int enable) {
        viewModel.setInviteConfirm(enable);
    }

    private void setMsgPush(int enable) {

    }

    private void setMsgDetail(int enable) {

    }

    private void setEnableSound(int enable) {

    }

    private void setEnableVibration(int enable) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_VERIFY_QUESTION) {
                if (data == null) {
                    sv_need_question.toggleSwitch(false);
                    rl_verify_question.setVisibility(View.GONE);
                    tv_version_question.setText("");
                    tv_version_answer.setText("");
                } else {
                    String question = data.getStringExtra("question");
                    String answer = data.getStringExtra("answer");
                    sv_need_question.toggleSwitch(true);
                    rl_verify_question.setVisibility(View.VISIBLE);
                    tv_version_question.setText(question);
                    tv_version_answer.setText(answer);
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_VERIFY_QUESTION) {
                sv_need_question.toggleSwitch(alreadyOpen);
            }
        }
    }

    @Override
    public void toggleToOn(SwitchView view) {
        int id = view.getId();
        if (id == R.id.sv_need_verify) {
            view.toggleSwitch(true);
            setAddVerify(1);
        } else if (id == R.id.sv_need_question) {
            setAddQuestion(1);
        } else if (id == R.id.sv_confirm_invite) {
            view.toggleSwitch(true);
            setInviteConfirm(1);
        } else if (id == R.id.sv_msg_push) {
            view.toggleSwitch(true);
            setMsgPush(1);
        } else if (id == R.id.sv_msg_detail) {
            view.toggleSwitch(true);
            setMsgDetail(1);
        } else if (id == R.id.sv_sound) {
            view.toggleSwitch(true);
            setEnableSound(1);
        } else if (id == R.id.sv_vibration) {
            view.toggleSwitch(true);
            setEnableVibration(1);
        }
    }

    @Override
    public void toggleToOff(SwitchView view) {
        int id = view.getId();
        view.toggleSwitch(false);
        if (id == R.id.sv_need_verify) {
            setAddVerify(2);
        } else if (id == R.id.sv_need_question) {
            setAddQuestion(2);
        } else if (id == R.id.sv_confirm_invite) {
            setInviteConfirm(2);
        } else if (id == R.id.sv_msg_push) {
            setMsgPush(2);
        } else if (id == R.id.sv_msg_detail) {
            setMsgDetail(2);
        } else if (id == R.id.sv_sound) {
            setEnableSound(2);
        } else if (id == R.id.sv_vibration) {
            setEnableVibration(2);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rl_verify_question) {
            alreadyOpen = true;
            ARouter.getInstance().build(AppRoute.VERIFY_QUESTION)
                    .navigation(this, REQUEST_VERIFY_QUESTION);
        } else if (id == R.id.ll_wallet_protocol) {
            ARouter.getInstance().build(AppRoute.WEB_BROWSER)
                    .withString("title", getString(R.string.chat_tips_privacy_title))
                    .withString("url", "https://biqianbao.net/terms.html")
                    .withBoolean("showOptions", false)
                    .navigation();
        }
    }
}
