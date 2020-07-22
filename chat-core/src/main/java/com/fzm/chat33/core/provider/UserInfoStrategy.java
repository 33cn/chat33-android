package com.fzm.chat33.core.provider;

import com.fuzamei.common.net.subscribers.OnSubscribeListener;
import com.fuzamei.common.net.subscribers.RxSubscriber;
import com.fuzamei.common.utils.RoomUtils;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.InfoCacheBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.core.net.RequestManager;

import io.reactivex.functions.Consumer;

/**
 * @author zhengjy
 * @since 2018/12/21
 * Description:用户信息获取策略
 */
public class UserInfoStrategy extends AbstractProvideStrategy<FriendBean> {

    public UserInfoStrategy(String id) {
        this.id = id;
    }

    @Override
    public void loadFromMemory(OnFindInfoListener<FriendBean> listener) {
        if (id.equals(UserInfo.getInstance().id)) {
            listener.onFindInfo(new FriendBean(UserInfo.getInstance().id,
                    UserInfo.getInstance().username, UserInfo.getInstance().avatar), FROM_MEMO);
            return;
        }
        if (Chat33.loadFriendFromCache(id) != null) {
            FriendBean bean = Chat33.loadFriendFromCache(id);
            listener.onFindInfo(bean, ProvideStrategy.FROM_MEMO);
        } else if (Chat33.loadInfoFromCache(Chat33Const.CHANNEL_FRIEND, id) != null) {
            InfoCacheBean cache = Chat33.loadInfoFromCache(Chat33Const.CHANNEL_FRIEND, id);
            FriendBean bean = new FriendBean(cache.getId(), cache.getDisplayName(), cache.getAvatar());
            listener.onFindInfo(bean, ProvideStrategy.FROM_NETWORK_OR_TEMP_CACHE);
        } else {
            listener.onNotExist();
        }
    }

    @Override
    public void loadFromDatabase(final OnFindInfoListener<FriendBean> listener) {
        RoomUtils.subscribe(ChatDatabase.getInstance().friendsDao()
                .getOneFriendById(id), new Consumer<FriendBean>() {
            @Override
            public void accept(FriendBean friendBean) throws Exception {
                listener.onFindInfo(friendBean, ProvideStrategy.FROM_DATABASE);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                RoomUtils.subscribe(ChatDatabase.getInstance().infoCacheDao()
                        .getOneInfoCache(Chat33Const.CHANNEL_FRIEND, id), new Consumer<InfoCacheBean>() {
                    @Override
                    public void accept(InfoCacheBean cache) throws Exception {
                        FriendBean bean = new FriendBean(cache.getId(), cache.getDisplayName(), cache.getAvatar());
                        listener.onFindInfo(bean, ProvideStrategy.FROM_NETWORK_OR_TEMP_CACHE);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        listener.onNotExist();
                    }
                });
            }
        });
    }

    @Override
    public void loadFromNetwork(final OnFindInfoListener<FriendBean> listener) {
        RequestManager.INS.getUserInfo(id, new RxSubscriber<>(new OnSubscribeListener<FriendBean>() {
            @Override
            public void onSuccess(final FriendBean bean) {
                listener.onFindInfo(new FriendBean(bean.getId(),
                        bean.getName(), bean.getAvatar()), ProvideStrategy.FROM_NETWORK_OR_TEMP_CACHE);
                RoomUtils.run(new Runnable() {
                    @Override
                    public void run() {
                        ChatDatabase.getInstance().infoCacheDao().insert(new InfoCacheBean(bean));
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                listener.onNotExist();
            }
        }));
    }
}
