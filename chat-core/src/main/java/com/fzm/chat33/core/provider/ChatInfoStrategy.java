package com.fzm.chat33.core.provider;

import android.text.TextUtils;

import com.fuzamei.common.net.subscribers.OnSubscribeListener;
import com.fuzamei.common.net.subscribers.RxSubscriber;
import com.fuzamei.common.utils.RoomUtils;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.InfoCacheBean;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.core.net.RequestManager;

import io.reactivex.functions.Consumer;

/**
 * @author zhengjy
 * @since 2018/12/21
 * Description:聊天记录列表信息获取策略
 */
public class ChatInfoStrategy extends AbstractProvideStrategy<InfoCacheBean> {

    private ChatMessage message;
    private int channelType;
    private String receiveId;
    private String senderId;

    public ChatInfoStrategy(ChatMessage message) {
        this.message = message;
        this.channelType = message.channelType;
        this.receiveId = message.receiveId;
        this.senderId = message.senderId;
    }

    public ChatInfoStrategy(ChatMessage message, String userId) {
        this.message = message;
        this.channelType = message.channelType;
        this.receiveId = message.receiveId;
        this.senderId = userId;
    }

    @Override
    public void loadFromMemory(OnFindInfoListener<InfoCacheBean> listener) {
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            if (message.isSentType()) {
                if (Chat33.loadRoomUserFromCache(receiveId, senderId) != null) {
                    RoomUserBean bean = Chat33.loadRoomUserFromCache(receiveId, senderId);
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_MEMO);
                } else {
                    listener.onNotExist();
                }
            } else {
                if (Chat33.loadFriendFromCache(senderId) != null
                        && !TextUtils.isEmpty(Chat33.loadFriendFromCache(senderId).getRemark())) {
                    FriendBean bean = Chat33.loadFriendFromCache(senderId);
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_MEMO);
                } else if (Chat33.loadRoomUserFromCache(receiveId, senderId) != null) {
                    RoomUserBean bean = Chat33.loadRoomUserFromCache(receiveId, senderId);
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_MEMO);
                } else {
                    listener.onNotExist();
                }
            }
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            if (message.isSentType()) {
                listener.onFindInfo(new InfoCacheBean(UserInfo.getInstance()), FROM_MEMO);
            } else {
                if (Chat33.loadFriendFromCache(senderId) != null) {
                    FriendBean bean = Chat33.loadFriendFromCache(senderId);
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_MEMO);
                } else {
                    listener.onNotExist();
                }
            }
        } else {
            listener.onNotExist();
        }
    }

    @Override
    public void loadFromDatabase(OnFindInfoListener<InfoCacheBean> listener) {
        if (message.channelType == Chat33Const.CHANNEL_ROOM) {
            RoomUtils.subscribe(ChatDatabase.getInstance().roomUserDao()
                    .mayGetRoomUser(message.receiveId, message.senderId), new Consumer<RoomUserBean>() {
                @Override
                public void accept(RoomUserBean bean) throws Exception {
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_DATABASE);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    listener.onNotExist();
                }
            });
        } else {
            listener.onNotExist();
        }
    }

    @Override
    public void loadFromNetwork(OnFindInfoListener<InfoCacheBean> listener) {
        if (message.channelType == Chat33Const.CHANNEL_ROOM) {
            if (message.senderInfo != null) {
                listener.onFindInfo(new InfoCacheBean(message.channelType, message.senderId,
                                message.senderInfo.nickname, message.senderInfo.avatar, 0),
                        FROM_NETWORK_OR_TEMP_CACHE);
            } else {
                RequestManager.INS.getRoomUserInfo(message.receiveId, message.senderId, new RxSubscriber<>(new OnSubscribeListener<RoomUserBean>() {
                    @Override
                    public void onSuccess(RoomUserBean roomUserBean) {
                        listener.onFindInfo(new InfoCacheBean(roomUserBean), FROM_NETWORK_OR_TEMP_CACHE);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (message.senderInfo != null) {
                            listener.onFindInfo(new InfoCacheBean(message.senderInfo), FROM_NETWORK_OR_TEMP_CACHE);
                        } else {
                            listener.onNotExist();
                        }
                    }
                }));
            }
        } else {
            if (message.senderInfo != null) {
                listener.onFindInfo(new InfoCacheBean(message.channelType, message.senderId,
                                message.senderInfo.nickname, message.senderInfo.avatar, 0),
                        FROM_NETWORK_OR_TEMP_CACHE);
            } else {
                RequestManager.INS.getUserInfo(message.senderId, new RxSubscriber<>(new OnSubscribeListener<FriendBean>() {
                    @Override
                    public void onSuccess(FriendBean bean) {
                        listener.onFindInfo(new InfoCacheBean(bean), FROM_NETWORK_OR_TEMP_CACHE);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (message.senderInfo != null) {
                            listener.onFindInfo(new InfoCacheBean(message.senderInfo), FROM_NETWORK_OR_TEMP_CACHE);
                        } else {
                            listener.onNotExist();
                        }
                    }
                }));
            }
        }
    }
}
