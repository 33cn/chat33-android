package com.fzm.chat33.core.provider;

import android.text.TextUtils;

import com.fuzamei.common.net.subscribers.OnSubscribeListener;
import com.fuzamei.common.net.subscribers.RxSubscriber;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.bean.PraiseBean;
import com.fzm.chat33.core.db.ChatDatabase;
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
public class ChatPraiseInfoStrategy extends AbstractProvideStrategy<InfoCacheBean> {

    private PraiseBean bean;
    private int channelType;
    private String userId;
    private String targetId;

    public ChatPraiseInfoStrategy(PraiseBean bean, String targetId) {
        this.bean = bean;
        this.channelType = bean.getChannelType();
        this.userId = bean.getUser().getId();
        this.targetId = targetId;
    }

    public boolean isSentType() {
        if (TextUtils.isEmpty(userId)) {
            return false;
        }
        return userId.equals(AppConfig.MY_ID);
    }

    @Override
    public void loadFromMemory(OnFindInfoListener<InfoCacheBean> listener) {
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            if (isSentType()) {
                if (Chat33.loadRoomUserFromCache(targetId, userId) != null) {
                    RoomUserBean bean = Chat33.loadRoomUserFromCache(targetId, userId);
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_MEMO);
                } else {
                    listener.onNotExist();
                }
            } else {
                if (Chat33.loadFriendFromCache(userId) != null
                        && !TextUtils.isEmpty(Chat33.loadFriendFromCache(userId).getRemark())) {
                    FriendBean bean = Chat33.loadFriendFromCache(userId);
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_MEMO);
                } else if (Chat33.loadRoomUserFromCache(targetId, userId) != null) {
                    RoomUserBean bean = Chat33.loadRoomUserFromCache(targetId, userId);
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_MEMO);
                } else {
                    listener.onNotExist();
                }
            }
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            if (isSentType()) {
                listener.onFindInfo(new InfoCacheBean(UserInfo.getInstance()), FROM_MEMO);
            } else {
                if (Chat33.loadFriendFromCache(userId) != null) {
                    FriendBean bean = Chat33.loadFriendFromCache(userId);
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
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            RoomUtils.subscribe(ChatDatabase.getInstance().roomUserDao()
                    .mayGetRoomUser(targetId, userId), new Consumer<RoomUserBean>() {
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
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            RequestManager.INS.getRoomUserInfo(targetId, userId, new RxSubscriber<>(new OnSubscribeListener<RoomUserBean>() {
                @Override
                public void onSuccess(RoomUserBean roomUserBean) {
                    listener.onFindInfo(new InfoCacheBean(roomUserBean), FROM_NETWORK_OR_TEMP_CACHE);
                }

                @Override
                public void onError(Throwable t) {
                    if (bean.getUser() != null) {
                        listener.onFindInfo(new InfoCacheBean(bean.getChannelType(), bean.getUser().getId(), bean.getUser().getName(), bean.getUser().getAvatar(), -1), FROM_NETWORK_OR_TEMP_CACHE);
                    } else {
                        listener.onNotExist();
                    }
                }
            }));
        } else {
            RequestManager.INS.getUserInfo(userId, new RxSubscriber<>(new OnSubscribeListener<FriendBean>() {
                @Override
                public void onSuccess(FriendBean bean) {
                    listener.onFindInfo(new InfoCacheBean(bean), FROM_NETWORK_OR_TEMP_CACHE);
                }

                @Override
                public void onError(Throwable t) {
                    if (bean.getUser() != null) {
                        listener.onFindInfo(new InfoCacheBean(bean.getChannelType(), bean.getUser().getId(), bean.getUser().getName(), bean.getUser().getAvatar(), -1), FROM_NETWORK_OR_TEMP_CACHE);
                    } else {
                        listener.onNotExist();
                    }
                }
            }));
        }
    }
}
