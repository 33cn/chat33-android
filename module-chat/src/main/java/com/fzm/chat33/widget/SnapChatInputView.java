package com.fzm.chat33.widget;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.View;

import com.fzm.chat33.R;
import com.fuzamei.common.utils.ShowUtils;
import com.fzm.chat33.utils.SimpleTextWatcher;

import static com.fzm.chat33.widget.ChatInputView.RECORD_AUDIO_PERMISSION;

/**
 * @author zhengjy
 * @since 2018/12/13
 * Description:
 */
public class SnapChatInputView extends LinearLayout implements View.OnClickListener {

    private ImageView chatBtnInputType, chatBtnImage, chatBtnClose;
    private AudioRecordButton chatRecordBtn;
    public EditText chatEditInput;
    private Button chatBtnSend;
    private Context mContext;

    private SnapChatViewCallback callback;

    public SnapChatInputView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public SnapChatInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public SnapChatInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_snap_chat_bottom, this);
        chatBtnInputType = findViewById(R.id.snap_chat_btn_input_type);
        chatBtnInputType.setOnClickListener(this);

        chatRecordBtn = findViewById(R.id.snap_chat_record_btn);
        chatRecordBtn.setSnap(1);
        chatRecordBtn.setOnClickListener(this);

        chatBtnImage = findViewById(R.id.snap_chat_btn_image);
        chatBtnImage.setOnClickListener(this);

        chatBtnSend = findViewById(R.id.snap_chat_btn_send);
        chatBtnSend.setOnClickListener(this);

        chatBtnClose = findViewById(R.id.snap_chat_btn_close);
        chatBtnClose.setOnClickListener(this);

        chatEditInput = findViewById(R.id.snap_chat_edit_input);

        chatEditInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    if (chatBtnClose.getVisibility() == View.INVISIBLE) {
                        chatBtnClose.setVisibility(View.VISIBLE);
                        chatBtnSend.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (chatBtnClose.getVisibility() == View.VISIBLE) {
                        chatBtnClose.setVisibility(View.INVISIBLE);
                        chatBtnSend.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.snap_chat_btn_input_type) {
            if (chatEditInput.getVisibility() == View.VISIBLE) {
                chatRecordBtn.setVisibility(View.VISIBLE);
                chatEditInput.setVisibility(View.GONE);
                chatBtnInputType.setImageResource(R.drawable.icon_chat_keyboard_snap);
                if (callback != null) {
                    callback.needHideKeyboard();
                }
            } else {
                chatBtnInputType.setImageResource(R.drawable.icon_chat_voice_snap);
                chatRecordBtn.setVisibility(View.GONE);
                chatEditInput.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.snap_chat_btn_image) {
            if (callback != null) {
                callback.onImageClick(view);
            }
        } else if (id == R.id.snap_chat_btn_send) {
            final String content = chatEditInput.getText().toString();
            if (TextUtils.isEmpty(content.trim())) {
                ShowUtils.showToast(mContext, mContext.getString(R.string.chat_tips_input_msg1));
                return;
            }
            chatEditInput.setText("");
            if (callback != null) {
                callback.onSendClick(view, content);
            }
        } else if (id == R.id.snap_chat_btn_close) {
            if (callback != null) {
                callback.onCloseClick(view);
            }
        }
    }

    public void setSnapChatViewCallback(Activity activity, final SnapChatViewCallback callback) {
        this.callback = callback;
        chatBtnSend.setVisibility(View.INVISIBLE);
        chatBtnClose.setVisibility(View.VISIBLE);
        chatRecordBtn.setPermissionParams(activity, RECORD_AUDIO_PERMISSION);
        chatRecordBtn.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {

            @Override
            public void onFinished(float seconds, String filePath) {
                if (callback != null) {
                    if (seconds > 60) {
                        seconds = 60;
                    }
                    callback.onAudioRecorderFinished(seconds, filePath);
                }
            }
        });
    }

    public interface SnapChatViewCallback {

        void onAudioRecorderFinished(float seconds, String filePath);

        void onCloseClick(View view);

        void onSendClick(View view, String content);

        void onImageClick(View view);

        void scrollChatLogHistory();

        void needHideKeyboard();
    }
}
