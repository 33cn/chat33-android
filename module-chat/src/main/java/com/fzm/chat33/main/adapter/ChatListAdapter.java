package com.fzm.chat33.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;

import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.utils.LogUtils;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.utils.VibrateUtils;
import com.fuzamei.common.widget.IconView;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.ait.AitManager;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.bean.MessageState;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.RecentMessage;
import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.hepler.FileDownloadManager;
import com.fzm.chat33.main.activity.ChatActivity;
import com.fzm.chat33.main.activity.LargePhotoActivity;
import com.fzm.chat33.main.activity.ShowBigImageActivity;
import com.fzm.chat33.main.listener.AdapterItemClickListener;
import com.fzm.chat33.main.mvvm.ChatViewModel;
import com.fzm.chat33.record.MediaManager;
import com.fzm.chat33.widget.ChatInputView;
import com.fzm.chat33.widget.chatrow.ChatRowAudio;
import com.fzm.chat33.widget.chatrow.ChatRowBase;
import com.fzm.chat33.widget.chatrow.ChatRowEncrypted;
import com.fzm.chat33.widget.chatrow.ChatRowFile;
import com.fzm.chat33.widget.chatrow.ChatRowForward;
import com.fzm.chat33.widget.chatrow.ChatRowForwardText;
import com.fzm.chat33.widget.chatrow.ChatRowImage;
import com.fzm.chat33.widget.chatrow.ChatRowInvitation;
import com.fzm.chat33.widget.chatrow.ChatRowNotification;
import com.fzm.chat33.widget.chatrow.ChatRowReceipt;
import com.fzm.chat33.widget.chatrow.ChatRowRedPacket;
import com.fzm.chat33.widget.chatrow.ChatRowSystem;
import com.fzm.chat33.widget.chatrow.ChatRowText;
import com.fzm.chat33.widget.chatrow.ChatRowTextPacket;
import com.fzm.chat33.widget.chatrow.ChatRowTransfer;
import com.fzm.chat33.widget.chatrow.ChatRowUnsupported;
import com.fzm.chat33.widget.chatrow.ChatRowVideo;
import com.fzm.chat33.widget.popup.ChatMessagePopupWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fzm.chat33.core.consts.PraiseAction.ACTION_LIKE;
import static com.fzm.chat33.core.consts.PraiseState.LIKE;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.AUDIO;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.FILE;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.FORWARD;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.IMAGE;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.INVITATION;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.NOTIFICATION;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.RECEIPT;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.RED_PACKET;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.SYSTEM;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.TEXT;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.TRANSFER;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.VIDEO;

public class ChatListAdapter extends BaseAdapter {
    private final static String TAG = ChatListAdapter.class.getSimpleName();

    private static final int MESSAGE_TYPE_SYSTEM = 0;
    private static final int MESSAGE_TYPE_NOTIFICATION = 1;

    private static final int MESSAGE_TYPE_SENT_TXT = 2;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 3;
    private static final int MESSAGE_TYPE_SENT_AUDIO = 4;
    private static final int MESSAGE_TYPE_SENT_REDBAG = 5;
    private static final int MESSAGE_TYPE_SENT_REDBAG_TEXT = 33;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 6;
    private static final int MESSAGE_TYPE_SENT_FORWARD = 7;
    private static final int MESSAGE_TYPE_SENT_FILE = 8;
    private static final int MESSAGE_TYPE_SENT_TRANSFER = 25;
    private static final int MESSAGE_TYPE_SENT_RECEIPT = 26;
    private static final int MESSAGE_TYPE_SENT_ENCRYPTED = 29;
    private static final int MESSAGE_TYPE_SENT_INVITATION = 31;

    private static final int MESSAGE_TYPE_RECEIVE_TXT = 9;
    private static final int MESSAGE_TYPE_RECEIVE_IMAGE = 10;
    private static final int MESSAGE_TYPE_RECEIVE_AUDIO = 11;
    private static final int MESSAGE_TYPE_RECEIVE_REDBAG = 12;
    private static final int MESSAGE_TYPE_RECEIVE_REDBAG_TEXT = 34;
    private static final int MESSAGE_TYPE_RECEIVE_VIDEO = 13;
    private static final int MESSAGE_TYPE_RECEIVE_FORWARD = 14;
    private static final int MESSAGE_TYPE_RECEIVE_FILE = 15;
    private static final int MESSAGE_TYPE_RECEIVE_TRANSFER = 27;
    private static final int MESSAGE_TYPE_RECEIVE_RECEIPT = 28;
    private static final int MESSAGE_TYPE_RECEIVE_ENCRYPTED = 30;
    private static final int MESSAGE_TYPE_RECEIVE_INVITATION = 32;

    private static final int MESSAGE_TYPE_RECEIVE_F_AUDIO = 16;
    private static final int MESSAGE_TYPE_RECEIVE_F_FORWARD = 17;
    private static final int MESSAGE_TYPE_RECEIVE_F_REDBAG = 18;
    private static final int MESSAGE_TYPE_RECEIVE_F_SYSTEM = 19;

    private static final int MESSAGE_TYPE_SENT_F_AUDIO = 20;
    private static final int MESSAGE_TYPE_SENT_F_FORWARD = 21;
    private static final int MESSAGE_TYPE_SENT_F_REDBAG = 22;
    private static final int MESSAGE_TYPE_SENT_F_SYSTEM = 23;

    private static final int MESSAGE_TYPE_UNSUPPORTED = 24;

    private List<ChatMessage> data;
    private List<ChatMessage> selectedMsg;
    private Map<String, RoomUserBean> roomUsers;
    private FragmentActivity activity;

    private int mMinItemWith;// 设置对话框的最大宽度和最小宽度
    private int mMaxItemWith;

    private AdapterItemClickListener listener;

    public MessageCallBack messageCallBack;

    private boolean isGroupChat;

    private boolean selectable = false;

    private int memberLevel = 1;

    private boolean canAddFriend = false;

    private AitManager aitManager;
    private ChatInputView chatInputView;
    private ChatViewModel viewModel;

    public ChatListAdapter(List<ChatMessage> data, FragmentActivity activity, ChatViewModel viewModel) {
        this.data = data;
        this.activity = activity;
        this.viewModel = viewModel;
        mMaxItemWith = (int) (ScreenUtils.getScreenWidth(activity) * 0.55f);
        mMinItemWith = (int) (ScreenUtils.getScreenWidth(activity) * 0.20f);
    }

    public void setGroupChat(boolean groupChat) {
        isGroupChat = groupChat;
    }

    public void setRoomUsers(Map<String, RoomUserBean> roomUsers) {
        this.roomUsers = roomUsers;
    }

    public Map<String, RoomUserBean> getRoomUsers() {
        return roomUsers;
    }

    public void setListener(AdapterItemClickListener listener) {
        this.listener = listener;
    }

    public void setMemberLevel(int memberLevel) {
        this.memberLevel = memberLevel;
    }

    public void setupAit(AitManager manager, ChatInputView chatInputView) {
        this.aitManager = manager;
        this.chatInputView = chatInputView;
    }

    @Override
    public int getCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    public void setMessageCallBack(MessageCallBack messageCallBack) {
        this.messageCallBack = messageCallBack;
    }

    @Override
    public ChatMessage getItem(int position) {
        if (data == null || position >= data.size()) {
            return null;
        }
        return data.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage message = getItem(position);
        ChatRowBase chatRowBase;
        if (convertView == null) {
            chatRowBase = createChatRow(message, position);
            convertView = chatRowBase.getRootView();
            convertView.setTag(chatRowBase);
        } else {
            chatRowBase = (ChatRowBase) convertView.getTag();
        }

        //refresh ui with messages
        chatRowBase.setView(message, position, selectable, itemClickListener);

        if (chatRowBase instanceof ChatRowAudio) {
            ((ChatRowAudio) chatRowBase).setAudioView(isPlaying, playingUrl);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        long last = 0;
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                if (i == 0) {
                    last = data.get(i).sendTime;
                    data.get(i).showTime = true;
                } else {
                    long cur = data.get(i).sendTime;
                    if (cur - last > 60 * 10 * 1000) {
                        last = cur;
                        data.get(i).showTime = true;
                    } else {
                        data.get(i).showTime = false;
                    }
                }
            }
        }
        super.notifyDataSetChanged();
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public List<ChatMessage> getSelectedMsg() {
        if (selectedMsg == null) {
            selectedMsg = new ArrayList<>();
        }
        return selectedMsg;
    }

    public void clearSelectedMsg() {
        if (selectedMsg != null) {
            selectedMsg.clear();
        }
    }

    @Override
    public int getViewTypeCount() {
        return 35;
    }

    protected ChatRowBase createChatRow(ChatMessage message, int position) {
        ChatRowBase chatRow = null;
        if (!TextUtils.isEmpty(message.msg.encryptedMsg)) {
            return new ChatRowEncrypted(activity, message, position, this);
        }
        switch (message.msgType) {
            case SYSTEM:
                if (message.msg.forwardType == 1) {
                    chatRow = new ChatRowForwardText(activity, message, position, this);
                } else {
                    chatRow = new ChatRowSystem(activity, message, position, this);
                }
                break;
            case TEXT:
                chatRow = new ChatRowText(activity, message, position, this);
                break;
            case IMAGE:
                chatRow = new ChatRowImage(activity, message, position, this);
                break;
            case AUDIO:
                if (message.msg.forwardType == 1) {
                    chatRow = new ChatRowForwardText(activity, message, position, this);
                } else {
                    chatRow = new ChatRowAudio(activity, message, position, this, mMaxItemWith, mMinItemWith);
                }
                break;
            case RED_PACKET:
                if (message.msg.forwardType == 1) {
                    chatRow = new ChatRowForwardText(activity, message, position, this);
                } else {
                    if (message.msg.packetMode == 0) {
                        chatRow = new ChatRowRedPacket(activity, message, position, this);
                    } else {
                        chatRow = new ChatRowTextPacket(activity, message, position, this);
                    }
                }
                break;
            case VIDEO:
                chatRow = new ChatRowVideo(activity, message, position, this);
                break;
            case NOTIFICATION:
                chatRow = new ChatRowNotification(activity, message, position, this);
                break;
            case FORWARD:
                if (message.msg.forwardType == 1) {
                    chatRow = new ChatRowForwardText(activity, message, position, this);
                } else {
                    chatRow = new ChatRowForward(activity, message, position, this);
                }
                break;
            case FILE:
                chatRow = new ChatRowFile(activity, message, position, this);
                break;
            case TRANSFER:
                chatRow = new ChatRowTransfer(activity, message, position, this);
                break;
            case RECEIPT:
                chatRow = new ChatRowReceipt(activity, message, position, this);
                break;
            case INVITATION:
                chatRow = new ChatRowInvitation(activity, message, position, this);
                break;
            default:
                chatRow = new ChatRowUnsupported(activity, message, position, this);
                break;
        }
        return chatRow;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage item = getItem(position);
        if (item == null) {
            return -1;
        }
        int msgType = item.msgType;
        if (item.isSentType()) {//我的消息、发送、右边
            if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
                return MESSAGE_TYPE_SENT_ENCRYPTED;
            }
            //文字，图片，语音
            if (msgType == SYSTEM) {
                if (item.msg.forwardType == 1) {
                    return MESSAGE_TYPE_SENT_F_SYSTEM;
                } else {
                    return MESSAGE_TYPE_SYSTEM;
                }
            } else if (msgType == ChatMessage.Type.TEXT) {
                return MESSAGE_TYPE_SENT_TXT;
            } else if (msgType == ChatMessage.Type.IMAGE) {
                return MESSAGE_TYPE_SENT_IMAGE;
            } else if (msgType == AUDIO) {
                if (item.msg.forwardType == 1) {
                    return MESSAGE_TYPE_SENT_F_AUDIO;
                } else {
                    return MESSAGE_TYPE_SENT_AUDIO;
                }
            } else if (msgType == ChatMessage.Type.RED_PACKET) {
                if (item.msg.forwardType == 1) {
                    return MESSAGE_TYPE_SENT_F_REDBAG;
                } else {
                    if (item.msg.packetMode == 0) {
                        return MESSAGE_TYPE_SENT_REDBAG;
                    } else {
                        return MESSAGE_TYPE_SENT_REDBAG_TEXT;
                    }
                }
            } else if (msgType == ChatMessage.Type.VIDEO) {
                return MESSAGE_TYPE_SENT_VIDEO;
            } else if (msgType == NOTIFICATION) {
                return MESSAGE_TYPE_NOTIFICATION;
            } else if (msgType == ChatMessage.Type.FORWARD) {
                if (item.msg.forwardType == 1) {
                    return MESSAGE_TYPE_SENT_F_FORWARD;
                } else {
                    return MESSAGE_TYPE_SENT_FORWARD;
                }
            } else if (msgType == ChatMessage.Type.FILE) {
                return MESSAGE_TYPE_SENT_FILE;
            } else if (msgType == TRANSFER) {
                return MESSAGE_TYPE_SENT_TRANSFER;
            } else if (msgType == RECEIPT) {
                return MESSAGE_TYPE_SENT_RECEIPT;
            } else if (msgType == INVITATION) {
                return MESSAGE_TYPE_SENT_INVITATION;
            } else {
                return MESSAGE_TYPE_UNSUPPORTED;
            }
        } else {//他人消息、接收、左边
            if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
                return MESSAGE_TYPE_RECEIVE_ENCRYPTED;
            }
            //文字，图片，语音
            if (msgType == SYSTEM) {
                if (item.msg.forwardType == 1) {
                    return MESSAGE_TYPE_RECEIVE_F_SYSTEM;
                } else {
                    return MESSAGE_TYPE_SYSTEM;
                }
            } else if (msgType == ChatMessage.Type.TEXT) {
                return MESSAGE_TYPE_RECEIVE_TXT;
            } else if (msgType == ChatMessage.Type.IMAGE) {
                return MESSAGE_TYPE_RECEIVE_IMAGE;
            } else if (msgType == AUDIO) {
                if (item.msg.forwardType == 1) {
                    return MESSAGE_TYPE_RECEIVE_F_AUDIO;
                } else {
                    return MESSAGE_TYPE_RECEIVE_AUDIO;
                }
            } else if (msgType == ChatMessage.Type.RED_PACKET) {
                if (item.msg.forwardType == 1) {
                    return MESSAGE_TYPE_RECEIVE_F_REDBAG;
                } else {
                    if (item.msg.packetMode == 0) {
                        return MESSAGE_TYPE_RECEIVE_REDBAG;
                    } else {
                        return MESSAGE_TYPE_RECEIVE_REDBAG_TEXT;
                    }
                }
            } else if (msgType == ChatMessage.Type.VIDEO) {
                return MESSAGE_TYPE_RECEIVE_VIDEO;
            } else if (msgType == NOTIFICATION) {
                return MESSAGE_TYPE_NOTIFICATION;
            } else if (msgType == ChatMessage.Type.FORWARD) {
                if (item.msg.forwardType == 1) {
                    return MESSAGE_TYPE_RECEIVE_F_FORWARD;
                } else {
                    return MESSAGE_TYPE_RECEIVE_FORWARD;
                }
            } else if (msgType == ChatMessage.Type.FILE) {
                return MESSAGE_TYPE_RECEIVE_FILE;
            } else if (msgType == TRANSFER) {
                return MESSAGE_TYPE_RECEIVE_TRANSFER;
            } else if (msgType == RECEIPT) {
                return MESSAGE_TYPE_RECEIVE_RECEIPT;
            } else if (msgType == INVITATION) {
                return MESSAGE_TYPE_RECEIVE_INVITATION;
            } else {
                return MESSAGE_TYPE_UNSUPPORTED;
            }
        }
    }

    public void setCanAddFriend(boolean canAddFriend) {
        this.canAddFriend = canAddFriend;
    }

    public void destroy() {
        cancelSnapChatTimer();
        cancelDownload();
    }

    private void cancelDownload() {
        if (data == null) {
            return;
        }
        for (ChatMessage message : data) {
            if (message.msgType == ChatMessage.Type.VIDEO) {
                FileDownloadManager.INS.cancel(message.msg.getMediaUrl());
            }
            if (message.msgType == ChatMessage.Type.FILE) {
                FileDownloadManager.INS.cancel(message.msg.fileUrl);
            }
        }
    }

    private void cancelSnapChatTimer() {
        if (data == null) {
            return;
        }
        for (ChatMessage message : data) {
            if (message.timer != null) {
                message.timer.cancel();
                message.timer = null;
            }
        }
    }

    private View.OnTouchListener mOnTouchListener;

    public void setOnItemTouchListener(View.OnTouchListener listener) {
        this.mOnTouchListener = listener;
    }

    ChatRowBase.MessageListItemClickListener itemClickListener = new ChatRowBase.MessageListItemClickListener() {
        @Override
        public boolean onTouchChatMainView(View v, MotionEvent event) {
            if (mOnTouchListener != null) {
                return mOnTouchListener.onTouch(v, event);
            }
            return false;
        }

        @Override
        public void onResendClick(View v, ChatMessage message) {
            if (messageCallBack != null) {
                messageCallBack.onResendClick(message);
            }
        }

        @Override
        public void onBubbleClick(View v, ChatMessage message, ChatRowBase chatRow) {
//            if (chatRow.thumbUpView() != null) {
//                if (!message.isSentType() && message.msgType == TEXT) {
//                    if ((message.praise.getState() & LIKE) != LIKE) {
//                        viewModel.messageLike(message.channelType, message.logId, ACTION_LIKE);
//                    }
//                }
//            }
            if (message.msgType == TEXT) {

            } else if (message.msgType == AUDIO) {
                ((ChatRowAudio) chatRow).clickBubble();
            } else if (message.msgType == IMAGE) {
                if (message.isSnap == 1) {
                    Intent intent = new Intent(activity, LargePhotoActivity.class);
                    intent.putExtra(LargePhotoActivity.CHAT_MESSAGE, message);
                    activity.startActivityForResult(intent, ChatActivity.REQUEST_DEAD_TIME,
                            ActivityOptionsCompat.makeSceneTransitionAnimation(activity, v, "shareImage").toBundle());
                } else {
                    ArrayList<ChatMessage> image_list = new ArrayList<>();
                    int index = 1;
                    for (int i = 0; i < data.size(); i++) {
                        if (data.get(i).msgType == 3 && data.get(i).isSnap == 2) {
                            image_list.add(data.get(i));
                            if (message.logId.equals(data.get(i).logId)) {
                                index = image_list.size() - 1;
                            }
                        }
                    }
                    Intent intent = new Intent(activity, ShowBigImageActivity.class);
                    intent.putExtra("image_list", image_list);
                    intent.putExtra("currentIndex", index);
                    activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, v, "shareImage").toBundle());
                }
            } else if (message.msgType == RED_PACKET) {
                itemClickListener.onRedBagClick(v, message);
            } else if (message.msgType == VIDEO) {
                ((ChatRowVideo) chatRow).clickBubble();
            } else if (message.msgType == FORWARD) {
                ARouter.getInstance().build(AppRoute.FORWARD_MESSAGE)
                        .withSerializable("message", message)
                        .navigation();
            } else if (message.msgType == FILE) {
                ((ChatRowFile) chatRow).clickBubble();
            } else if (message.msgType == TRANSFER) {
                if (message.messageState == MessageState.SEND_SUCCESS) {
                    ARouter.getInstance().build(AppRoute.DEPOSIT_TX_DETAIL)
                            .withString("recordId", message.msg.recordId)
                            .navigation();
                }
            } else if (message.msgType == RECEIPT) {
                if (!TextUtils.isEmpty(message.msg.recordId)) {
                    ARouter.getInstance().build(AppRoute.DEPOSIT_TX_DETAIL)
                            .withString("recordId", message.msg.recordId)
                            .navigation();
                } else {
                    if (message.isSentType()) {
                        ARouter.getInstance().build(AppRoute.DEPOSIT_RECEIPT)
                                .withSerializable("target", Chat33.loadFriendFromCache(message.receiveId))
                                .withString("amount", message.msg.amount)
                                .withString("coinName", message.msg.coinName)
                                .navigation();
                    } else {
                        ARouter.getInstance().build(AppRoute.DEPOSIT_OUT)
                                .withSerializable("target", Chat33.loadFriendFromCache(message.senderId))
                                .withString("amount", message.msg.amount)
                                .withString("coinName", message.msg.coinName)
                                .withString("logId", message.logId)
                                .navigation();
                    }
                }
            } else if (message.msgType == INVITATION) {
                viewModel.hasRelationship(Chat33Const.CHANNEL_ROOM, message.msg.roomId).observe(activity, result -> {
                    if (result != null && result.isInRoom) {
                        ARouter.getInstance().build(AppRoute.CHAT)
                                .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                                .withString("targetName", message.msg.roomName)
                                .withString("targetId", message.msg.roomId)
                                .navigation();
                    } else {
                        ARouter.getInstance().build(AppRoute.JOIN_ROOM)
                                .withString("markId", message.msg.markId)
                                .withString("sourceId", message.msg.inviterId)
                                .withInt("sourceType", Chat33Const.FIND_TYPE_INVITE)
                                .withBoolean("ignoreDisable", true)
                                .navigation();
                    }
                });
            }
        }

        @Override
        public void onBubbleDoubleClick(View v, ChatMessage message) {
//            if (AppConfig.APP_MESSAGE_REWARD) {
//                if (!message.isSentType() && message.isSnap != 1 && message.msgType == TEXT) {
//                    // 不是自己的消息才能打赏
//                    ARouter.getInstance().build(AppRoute.REWARD_PACKET)
//                            .withInt("channelType", message.channelType)
//                            .withString("logId", message.logId)
//                            .navigation();
//                }
//            }
        }

        @Override
        public void onBubbleLongClick(final View v, final ChatMessage message) {
            if (!TextUtils.isEmpty(message.msg.encryptedMsg)) {
                // 加密消息长按不弹出菜单
                return;
            }
            //长按消息item
            if (message.msgType == SYSTEM || message.msgType == TEXT || message.msgType == AUDIO || message.msgType == IMAGE
                    || message.msgType == RED_PACKET || message.msgType == VIDEO || message.msgType == FORWARD || message.msgType == FILE
                    || message.msgType == TRANSFER || message.msgType == RECEIPT || message.msgType == INVITATION) {
                ChatMessagePopupWindow popupWindow = new ChatMessagePopupWindow(activity, message, memberLevel);
                popupWindow.setChooseCallBack(new ChatMessagePopupWindow.ChooseCallBack() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.itemClick(message, 0, view);
                        }
                    }
                });
                if (message.msgType != SYSTEM && message.msgType != IMAGE && !(message.msgType == RED_PACKET && message.msg.packetMode == 0)
                        && message.msgType != VIDEO && message.msgType != FILE && message.msgType != TRANSFER
                        && message.msgType != RECEIPT && message.msgType != INVITATION) {
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            v.setBackgroundResource(message.isSentType() ? R.drawable.chat_send_selector : R.drawable.chat_receive_selector);
                        }
                    });
                }
                popupWindow.show(v);
            }
        }

        @Override
        public void onRedBagClick(View v, ChatMessage message) {
            if(messageCallBack !=null){
                messageCallBack.onRedBagClick(message);
            }
        }

        @Override
        public void onUserAvatarClick(View v, ChatMessage message) {
            ARouter.getInstance().build(AppRoute.USER_DETAIL)
                    .withString("userId", message.senderId)
                    .withString("roomId", message.channelType == Chat33Const.CHANNEL_ROOM ? message.receiveId : null)
                    .withInt("memberLevel", memberLevel)
                    .withBoolean("canAddFriend", canAddFriend)
                    .navigation(activity);
        }

        @Override
        public void onUserAvatarLongClick(View v, String id, String username) {
            if (!isSelectable()) {
                VibrateUtils.simple(activity, 50);
                aitManager.insertAitMember(id, username, chatInputView.getSelectionStart());
            }
        }

        @Override
        public void onAudioClick(IconView animView, final ChatMessage message, ChatRowAudio.Callback callback) {
            if (current == null) {
                current = callback;
            } else {
                previous = current;
                current = callback;
            }
            message.msg.isRead = true;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    ChatDatabase.getInstance().chatMessageDao().insert(message);
                }
            });
            notifyDataSetChanged();
            playOrPauseAudio(animView, message, data.indexOf(message));
        }

        @Override
        public void onMessageShow(View v, final ChatMessage message, int position) {
            if(messageCallBack !=null){
                messageCallBack.onSnapMsgClick(message, position);
            }
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    // 更新消息列表中的最新一条消息内容
                    int sticky = 2;
                    int disturb = 2;
                    String address = null;
                    long deadline = 0;
                    boolean isDeleted;
                    final String targetId;
                    ChatMessage newMsg;
                    if (message.channelType == Chat33Const.CHANNEL_ROOM) {
                        targetId = message.receiveId;
                    } else {
                        if (message.senderId.equals(AppConfig.MY_ID)) {
                            targetId = message.receiveId;
                        } else {
                            targetId = message.senderId;
                        }
                    }
                    List<ChatMessage> messages;
                    if (message.channelType == Chat33Const.CHANNEL_FRIEND) {
                        messages = ChatDatabase.getInstance().chatMessageDao()
                                .getPrivateNormalChatLogLocal(targetId, Long.MAX_VALUE, 1);
                        isDeleted = Chat33.loadFriendFromCache(targetId) == null;
                    } else {
                        messages = ChatDatabase.getInstance().chatMessageDao()
                                .getGroupNormalChatLogLocal(message.channelType, targetId, Long.MAX_VALUE, 1);
                        isDeleted = Chat33.loadRoomFromCache(targetId) == null;
                    }
                    if (messages.size() > 0) {
                        newMsg = messages.get(0);
                    } else {
                        newMsg = message;
                    }
                    if (!isDeleted) {
                        if (message.channelType == 2) {
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
                    RecentMessage oldMsg = ChatDatabase.getInstance().recentMessageDao().getRecentMsgById(message.channelType, targetId);
                    RecentMessage.PraiseNum praise = oldMsg == null ? new RecentMessage.PraiseNum() : oldMsg.getPraise();
                    RecentMessage recentMessage = new RecentMessage(targetId, address, deadline, 0, sticky, disturb, isDeleted, false,
                            praise, new RecentMessage.LastLogBean(newMsg));
                    ChatDatabase.getInstance().recentMessageDao().insert(recentMessage);
                }
            });
        }

        @Override
        public void onMessageCountDown(String key, CountDownTimer timer) {

        }

        @Override
        public void onMessageDestroy(View view, final ChatMessage message) {
            LogUtils.d(TAG, message.msg.content + " 被移除");
            data.remove(message);
            notifyDataSetChanged();
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    ChatDatabase.getInstance().chatMessageDao().deleteMessage(message.channelType, message.logId);
                }
            });
        }

        @Override
        public void onMessageSelected(ChatMessage message) {
            if (selectedMsg == null) {
                selectedMsg = new ArrayList<>();
            }
            if (message.isSelected) {
                if (!selectedMsg.contains(message)) {
                    selectedMsg.add(message);
                }
            } else {
                selectedMsg.remove(message);
            }
        }
    };

    public IconView playingViewAnim;
    private IconView lastPlayingViewAnim;
    private boolean isPlaying = false;
    private static String playingUrl;

    private ChatRowAudio.Callback current;
    private ChatRowAudio.Callback previous;

    public void onPause() {
        MediaManager.INSTANCE.stop();
        isPlaying = false;
        if (current != null) {
            current.shouldStartCount();
        }
        if (previous != null) {
            previous.shouldStartCount();
        }
        if (playingViewAnim != null) {
            playingViewAnim.stop();
        }
        if (lastPlayingViewAnim != null) {
            lastPlayingViewAnim.stop();
        }
    }

    private void playOrPauseAudio(IconView audioAnimView, ChatMessage message, int index) {
        //todo
//        MyLog.toastShort("播放或停止:" + chatFile.getName());
        if (activity == null || !com.fzm.chat33.utils.FileUtils.isGrantExternalRW(0x11, activity)) {
            return;
        }
        if(messageCallBack !=null){
            messageCallBack.onVoiceStateChanged(true);
        }

        // 播放动画
        if (playingViewAnim != null) {//让第二个播放的时候第一个停止播放
            lastPlayingViewAnim = playingViewAnim;
//            playingViewAnim.setTag(null);
            playingViewAnim.reset();
            playingViewAnim = null;
            MediaManager.INSTANCE.stop();
            if (previous != null && lastPlayingViewAnim != audioAnimView) {
                previous.shouldStartCount();
            }
        }
        playingViewAnim = audioAnimView;
        playingUrl = message.msg.getMediaUrl();
        if (lastPlayingViewAnim == playingViewAnim) {
            if (isPlaying) {
                isPlaying = false;
                if (current != null) {
                    current.shouldStartCount();
                }
                return;
            }
        }
        if (playingViewAnim != null) {
            playingViewAnim.play();
        }
        isPlaying = true;

//        playingViewAnim.setTag(chatFile.getNetUrl());
        // 播放音频
        MediaManager.INSTANCE.playSound(message, (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE),
                new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if(messageCallBack !=null){
                            messageCallBack.onVoiceStateChanged(false);
                        }
                        isPlaying = false;
//                        playingViewAnim.setTag(null);
                        playingUrl = null;
                        if (current != null) {
                            current.shouldStartCount();
                        }
                        if (playingViewAnim != null) {
                            playingViewAnim.reset();
                        }
                        for (int j = index + 1; j < data.size(); j++) {
                            ChatMessage item = data.get(j);
                            if (item.msgType == AUDIO && !item.msg.isRead
                                    && item.isSnap == 2 && !item.isSentType()) {
                                item.msg.isRead = true;
                                RoomUtils.run(new Runnable() {
                                    @Override
                                    public void run() {
                                        ChatDatabase.getInstance().chatMessageDao().insert(item);
                                    }
                                });
                                playOrPauseAudio(null, item, j);
                                notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                });
    }

    public interface MessageCallBack {
        void onRedBagClick(ChatMessage message);

        void onSnapMsgClick(ChatMessage message, int position);

        void onResendClick(ChatMessage message);

        void onVoiceStateChanged(boolean playing);

        void onHideKeyboard();
    }

}
