package com.fzm.chat33.widget;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuzamei.common.widget.IconView;
import com.fzm.chat33.R;
import com.fuzamei.common.utils.ShowUtils;
import com.fzm.chat33.ait.AitTextChangeListener;
import com.fzm.chat33.core.global.Chat33Const;

/**
 * Description:聊天消息输入框
 */
public class ChatInputView extends LinearLayout implements View.OnClickListener, AitTextChangeListener {
    public final static int RECORD_AUDIO_PERMISSION = 0x3238;

    ImageView chatBtnInputType;
    AudioRecordButton chatRecordBtn;
    public EditText chatEditInput;
    private TextWatcher aitTextWatcher;

    IconView chatBtnEmoji;
    Button chatBtnSend;
    View chatBtnOther;

    Context mContext;

    RelativeLayout chatBottomBar;

    ImageView ivHongbao;
    ImageView ivImage;
    ImageView ivTakePhoto;
    ImageView ivSnapChat;
    View iv_file;
    View iv_text_packet;
    View ll_transfer;
    View ll_receipt;

    LinearLayout otherSendContent;
    FrameLayout chatBottomLayout;

    TextView login;
    TextView tvMessageType;
    TextView tvMuted;

    int channelType = Chat33Const.CHANNEL_ROOM;

    private boolean isAdmin = false;

    private void initView() {
        login = findViewById(R.id.login);
        login.setOnClickListener(this);
        tvMuted = findViewById(R.id.tv_muted);
        tvMuted.setOnClickListener(this);

        otherSendContent = findViewById(R.id.other_send_content);
        otherSendContent.setOnClickListener(this);
        chatBottomLayout = findViewById(R.id.chat_bottom_layout);
        chatBottomLayout.setOnClickListener(this);

        chatBtnInputType = findViewById(R.id.chat_btn_input_type);
        chatBtnInputType.setOnClickListener(this);

        chatRecordBtn = findViewById(R.id.chat_record_btn);
        chatRecordBtn.setSnap(2);
        chatRecordBtn.setOnClickListener(this);

        chatEditInput = findViewById(R.id.chat_edit_input);
        chatEditInput.setOnClickListener(this);

        chatBtnEmoji = findViewById(R.id.chat_btn_emoji);
        chatBtnEmoji.setOnClickListener(this);

        chatBtnSend = findViewById(R.id.chat_btn_send);
        chatBtnSend.setOnClickListener(this);

        chatBtnOther = findViewById(R.id.chat_btn_other);
        chatBtnOther.setOnClickListener(this);

        chatBottomBar = findViewById(R.id.chat_bottom_bar);
        chatBottomBar.setOnClickListener(this);

        ivHongbao = findViewById(R.id.iv_hongbao);
        ivHongbao.setOnClickListener(this);
        ivImage = findViewById(R.id.iv_image);
        ivImage.setOnClickListener(this);
        ivTakePhoto = findViewById(R.id.iv_take_photo);
        ivTakePhoto.setOnClickListener(this);
        ivSnapChat = findViewById(R.id.iv_snap_chat);
        ivSnapChat.setOnClickListener(this);
        iv_file = findViewById(R.id.iv_file);
        iv_text_packet = findViewById(R.id.iv_text_packet);
        iv_text_packet.setOnClickListener(this);
        iv_file.setOnClickListener(this);
        ll_transfer = findViewById(R.id.ll_transfer);
        ll_transfer.setOnClickListener(this);
        ll_receipt = findViewById(R.id.ll_receipt);
        ll_receipt.setOnClickListener(this);
    }

    IChatInputView mIChatInputView;
    private int messageType;

    public ChatInputView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ChatInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public ChatInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public void setChannelType(int channelType) {
        this.channelType = channelType;
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            findViewById(R.id.ll_transfer).setVisibility(INVISIBLE);
            findViewById(R.id.ll_receipt).setVisibility(INVISIBLE);
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            findViewById(R.id.ll_transfer).setVisibility(VISIBLE);
            findViewById(R.id.ll_receipt).setVisibility(VISIBLE);
        } else {
            findViewById(R.id.ll_transfer).setVisibility(INVISIBLE);
            findViewById(R.id.ll_receipt).setVisibility(INVISIBLE);
        }
    }

    public TextView getLogin() {
        return login;
    }

    public TextView getTvMuted() {
        return tvMuted;
    }

    private void init() {
        inflate(getContext(), R.layout.layout_chat_bottom_bar, this);
        initView();
    }

    public void addAitTextWatcher(TextWatcher textWatcher) {
        this.aitTextWatcher = textWatcher;
    }

    public int getSelectionStart() {
        return chatEditInput.getSelectionStart();
    }

    public void setData(Activity activity, IChatInputView iChatInputView) {
        SpannableString ss = new SpannableString(mContext.getString(R.string.chat_tips_input_say_sth));//定义hint的值
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(14, true);//设置字体大小 true表示单位是sp
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        chatEditInput.setHint(new SpannedString(ss));
        chatRecordBtn.setVisibility(View.GONE);
        chatEditInput.setVisibility(View.VISIBLE);
        otherSendContent.setVisibility(View.GONE);

        //设置other_more入口显示时的动画，调和与键盘弹出隐藏的闪烁冲突
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(80);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        chatBottomLayout.setLayoutTransition(transition);
        chatBtnSend.setVisibility(View.INVISIBLE);
        chatBtnOther.setVisibility(View.VISIBLE);

        mIChatInputView = iChatInputView;

        chatRecordBtn.setPermissionParams(activity, RECORD_AUDIO_PERMISSION);
        chatRecordBtn.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {

            @Override
            public void onFinished(float seconds, String filePath) {
                if (mIChatInputView != null) {
                    if (seconds > 60) {
                        seconds = 60;
                    }
                    mIChatInputView.onAudioRecorderFinished(seconds, filePath);
                }
            }
        });

        chatEditInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (aitTextWatcher != null) {
                    aitTextWatcher.beforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    if (chatBtnOther.getVisibility() == View.INVISIBLE) {
                        chatBtnOther.setVisibility(View.VISIBLE);
                        chatBtnSend.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (chatBtnOther.getVisibility() == View.VISIBLE) {
                        chatBtnOther.setVisibility(View.INVISIBLE);
                        chatBtnSend.setVisibility(View.VISIBLE);
                    }
                }
                if (aitTextWatcher != null) {
                    aitTextWatcher.onTextChanged(s, start, before, count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (aitTextWatcher != null) {
                    aitTextWatcher.afterTextChanged(s);
                }
            }
        });
        chatEditInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //键盘弹起事件
                if (otherSendContent.getVisibility() == View.VISIBLE) {
                    otherSendContent.setVisibility(View.GONE);
                }
            }
        });
        chatEditInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //键盘弹起事件
                    if (otherSendContent.getVisibility() == View.VISIBLE) {
                        otherSendContent.setVisibility(View.GONE);
                    }
                } else {

                }
            }
        });

    }

    public void setText(String text) {
        chatEditInput.setText(text);
    }

    public boolean showOtherSendLayout() {
        if (otherSendContent.getVisibility() == View.GONE) {
            otherSendContent.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    public boolean hideOtherSendLayout() {
        if (otherSendContent.getVisibility() == View.VISIBLE) {
            otherSendContent.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_muted) {

        } else if (i == R.id.chat_record_btn) {
        } else if (i == R.id.login) {
            if (mIChatInputView != null) {
                mIChatInputView.login();
            }
        } else if (i == R.id.chat_edit_input) {
        } else if (i == R.id.chat_btn_emoji) {
        } else if (i == R.id.chat_bottom_bar) {
        } else if (i == R.id.other_send_content) {
        } else if (i == R.id.chat_bottom_layout) {
            //文字发送按钮
        } else if (i == R.id.chat_btn_send) {
            final String content = chatEditInput.getText().toString();
            if (TextUtils.isEmpty(content.trim())) {
                ShowUtils.showToast(mContext, mContext.getString(R.string.chat_tips_input_msg1));
                return;
            }
            if (mIChatInputView != null) {
                mIChatInputView.onSendButtonClick(view, content);
            }

            //切换文字语音按钮
        } else if (i == R.id.chat_btn_input_type) {
            if (chatEditInput.getVisibility() == View.VISIBLE) {
                chatRecordBtn.setVisibility(View.VISIBLE);
                chatEditInput.setVisibility(View.GONE);
                chatBtnInputType.setImageResource(R.drawable.icon_chat_keyboard);
                if (mIChatInputView != null) {
                    mIChatInputView.needHideKeyboard();
                }
                if (otherSendContent.getVisibility() == View.VISIBLE) {
                    otherSendContent.setVisibility(View.GONE);
                }
            } else {
                chatBtnInputType.setImageResource(R.drawable.icon_chat_voice);
                chatRecordBtn.setVisibility(View.GONE);
                chatEditInput.setVisibility(View.VISIBLE);
            }

            //附件点击按钮，红包，图片，拍照
        } else if (i == R.id.chat_btn_other) {//如果是系统消息则
            if (messageType == 2) {
                ShowUtils.showToast(mContext, mContext.getString(R.string.chat_tips_input_msg2));
                return;
            }
            if (otherSendContent.getVisibility() == View.VISIBLE) {
                otherSendContent.setVisibility(View.GONE);
            } else {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                boolean isOpen = imm.isActive();
                if (!isOpen) {//键盘隐藏状态
                    otherSendContent.setVisibility(View.VISIBLE);
                } else {
                    if (mIChatInputView != null) {
                        mIChatInputView.needHideKeyboard();
                    }
                    //键盘消失有延迟，延迟显示入口，避免闪烁
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            otherSendContent.setVisibility(View.VISIBLE);
                            mIChatInputView.scrollChatLogHistory();
                        }
                    }, 100);
                }
            }
        } else if (i == R.id.iv_hongbao) {
            //发红包按钮
            if (mIChatInputView != null) {
                mIChatInputView.onRedPacketClick(view, 0);
            }
        } else if (i == R.id.iv_image) {
            //发图片按钮
            if (mIChatInputView != null) {
                mIChatInputView.onImageButtonClick(view);
            }
        } else if (i == R.id.iv_take_photo) {
            //拍照按钮
            if (mIChatInputView != null) {
                mIChatInputView.onTakePictureButtonClick(view);
            }
        } else if (i == R.id.iv_snap_chat) {
            if (mIChatInputView != null) {
                mIChatInputView.onSnapChatClick(view);
            }
        } else if (i == R.id.iv_file) {
            if (mIChatInputView != null) {
                mIChatInputView.onFileClick(view);
            }
        } else if (i == R.id.iv_text_packet) {
            if (mIChatInputView != null) {
                mIChatInputView.onRedPacketClick(view, 1);
            }
        } else if (i == R.id.ll_transfer) {
            if (mIChatInputView != null) {
                mIChatInputView.onTransferClick(view);
            }

        } else if (i == R.id.ll_receipt) {
            if (mIChatInputView != null) {
                mIChatInputView.onReceiptMoneyClick(view);
            }
        }
    }

    @Override
    public void onTextAdd(String content, int start, int length) {
        if (getVisibility() == VISIBLE) {
            if (chatEditInput.getVisibility() != VISIBLE) {
                chatBtnInputType.performClick();
                chatEditInput.requestFocus();
            }
            mIChatInputView.needShowKeyboard(chatEditInput);
            chatEditInput.getEditableText().insert(start, content);
        }
    }

    @Override
    public void onTextDelete(int start, int length) {
        if (getVisibility() == VISIBLE) {
            if (chatEditInput.getVisibility() != VISIBLE) {
                chatBtnInputType.performClick();
                chatEditInput.requestFocus();
            }
            int end = start + length - 1;
            chatEditInput.getEditableText().replace(start, end, "");
        }
    }

    public interface IChatInputView {
        void onAudioRecorderFinished(float seconds, String filePath);

        void onImageButtonClick(View view);

        void onTakePictureButtonClick(View view);

        void onSendButtonClick(View view, String content);

        void onRedPacketClick(View view, int mode);

        void onFileClick(View view);

        void onSnapChatClick(View view);

        void onTransferClick(View view);

        void onReceiptMoneyClick(View view);

        void scrollChatLogHistory();

        void needShowKeyboard(View view);

        void needHideKeyboard();

        void login();

        void selectMessageType(int type);
    }

}
