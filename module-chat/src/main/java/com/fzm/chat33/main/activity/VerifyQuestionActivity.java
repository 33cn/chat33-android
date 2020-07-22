package com.fzm.chat33.main.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.param.AddQuestionParam;
import com.fzm.chat33.core.utils.UserInfoPreference;
import com.fzm.chat33.main.mvvm.SettingViewModel;
import com.fzm.chat33.utils.SimpleTextWatcher;
import com.fuzamei.componentservice.widget.CommonTitleBar;

import javax.inject.Inject;

import static com.fzm.chat33.core.utils.UserInfoPreference.VERIFY_ANSWER;
import static com.fzm.chat33.core.utils.UserInfoPreference.VERIFY_QUESTION;

/**
 * @author zhengjy
 * @since 2018/12/25
 * Description:
 */
@Route(path = AppRoute.VERIFY_QUESTION)
public class VerifyQuestionActivity extends DILoadableActivity {

    private CommonTitleBar ctbTitle;
    private EditText et_question, et_answer;
    private TextView tv_question_count, tv_answer_count;
    private View tv_submit;

    private String oldQuestion;
    private String oldAnswer;
    @Inject
    public ViewModelProvider.Factory provider;
    private SettingViewModel viewModel;

    @Override
    protected boolean enableSlideBack() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_verify_question;
    }

    @Override
    protected void initView() {
        viewModel = ViewModelProviders.of(this, provider).get(SettingViewModel.class);
        ctbTitle = findViewById(R.id.ctb_title);
        et_question = findViewById(R.id.et_question);
        et_answer = findViewById(R.id.et_answer);
        tv_question_count = findViewById(R.id.tv_question_count);
        tv_answer_count = findViewById(R.id.tv_answer_count);
        tv_submit = findViewById(R.id.tv_submit);

        viewModel.getLoading().observe(this, this::setupLoading);
        viewModel.getSetAddVerifyQuestion().observe(this, it-> {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_verify_set));
            UserInfoPreference.getInstance().setStringPref(VERIFY_QUESTION, it.getQuestion());
            UserInfoPreference.getInstance().setStringPref(VERIFY_ANSWER, it.getAnswer());
            Intent intent = new Intent();
            intent.putExtra("question", it.getQuestion());
            intent.putExtra("answer", it.getAnswer());
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    protected void initData() {
        ctbTitle.setLeftText(getString(R.string.chat_action_cancel));
        ctbTitle.setMiddleText(getString(R.string.chat_title_set_verify_question));
        ctbTitle.setRightVisible(false);

        oldQuestion = UserInfoPreference.getInstance().getStringPref(VERIFY_QUESTION, "");
        oldAnswer = UserInfoPreference.getInstance().getStringPref(VERIFY_ANSWER, "");
        if (!TextUtils.isEmpty(oldQuestion)) {
            et_question.setText(oldQuestion);
            et_question.setSelection(oldQuestion.length());
        }
        if (!TextUtils.isEmpty(oldAnswer)) {
            et_answer.setText(oldAnswer);
            et_answer.setSelection(oldAnswer.length());
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void setEvent() {
        ctbTitle.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        et_question.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    tv_question_count.setText(getString(R.string.chat_tips_num_20, 0));
                } else {
                    tv_question_count.setText(getString(R.string.chat_tips_num_20, s.length()));
                }
            }
        });
        et_answer.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    tv_answer_count.setText(getString(R.string.chat_tips_num_20, 0));
                } else {
                    tv_answer_count.setText(getString(R.string.chat_tips_num_20, s.length()));
                }
            }
        });
        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVerifyQuestion();
            }
        });
    }

    private void setVerifyQuestion() {
        final String question = et_question.getText().toString().trim();
        final String answer = et_answer.getText().toString().trim();
        if (TextUtils.isEmpty(question)) {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_empty_question));
            return;
        }
        if (TextUtils.isEmpty(answer)) {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_empty_answer));
            return;
        }
        viewModel.setAddVerifyQuestion(new AddQuestionParam(question, answer));
    }
}
