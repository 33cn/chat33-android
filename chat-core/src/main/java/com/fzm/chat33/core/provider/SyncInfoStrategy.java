package com.fzm.chat33.core.provider;

import android.text.TextUtils;

import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.net.RequestManager;

/**
 * @author zhengjy
 * @since 2019/05/07
 * Description:用户信息同步获取策略
 */
public class SyncInfoStrategy extends AbstractProvideStrategy<FriendBean> {

    private ChatMessage message;
    private boolean useSenderInfo;

    public SyncInfoStrategy(ChatMessage message, String id, boolean useSenderInfo) {
        this.message = message;
        this.id = id;
        this.useSenderInfo = useSenderInfo;
    }

    @Override
    public FriendBean get() {
        FriendBean bean = null;
        if (Chat33.loadFriendFromCache(id) != null) {
            bean = Chat33.loadFriendFromCache(id);
        } else {
            if (message.channelType == Chat33Const.CHANNEL_ROOM) {
                // 先在缓存中查找群成员信息
                RoomUserBean roomUser = Chat33.loadRoomUserFromCache(message.receiveId, id);
                if (roomUser == null) {
                    // 缓存中没有则去数据库查找群成员信息
                    roomUser = ChatDatabase.getInstance().roomUserDao().getRoomUserSync(message.receiveId, id);
                }
                if (roomUser != null) {
                    bean = new FriendBean(roomUser.getId(), roomUser.getDisplayName(), roomUser.getAvatar());
                }
            }
            if (bean == null && useSenderInfo && message.senderInfo != null) {
                bean = new FriendBean(id, message.senderInfo.getDisplayName(), message.senderInfo.avatar);
            }
        }
        if (bean == null) {
            bean = RequestManager.INS.getUserInfoSync(id);
        }
        return bean;
    }

    @Override
    public void loadFromMemory(OnFindInfoListener<FriendBean> listener) {

    }

    @Override
    public void loadFromDatabase(final OnFindInfoListener<FriendBean> listener) {

    }

    @Override
    public void loadFromNetwork(final OnFindInfoListener<FriendBean> listener) {

    }
}
