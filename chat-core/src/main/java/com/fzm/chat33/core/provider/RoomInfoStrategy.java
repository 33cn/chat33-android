package com.fzm.chat33.core.provider;

import android.text.TextUtils;

import com.fuzamei.common.net.subscribers.OnSubscribeListener;
import com.fuzamei.common.net.subscribers.RxSubscriber;
import com.fuzamei.common.utils.RoomUtils;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.InfoCacheBean;
import com.fzm.chat33.core.db.bean.RoomInfoBean;
import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.net.RequestManager;

import io.reactivex.functions.Consumer;

/**
 * @author zhengjy
 * @since 2018/12/21
 * Description:群信息获取策略
 */
public class RoomInfoStrategy extends AbstractProvideStrategy<RoomListBean> {

    public RoomInfoStrategy(String id) {
        this.id = id;
    }

    @Override
    public void loadFromMemory(OnFindInfoListener<RoomListBean> listener) {
        if (Chat33.loadRoomFromCache(id) != null) {
            RoomListBean bean = Chat33.loadRoomFromCache(id);
            listener.onFindInfo(bean, FROM_MEMO);
        } else if (Chat33.loadInfoFromCache(Chat33Const.CHANNEL_ROOM, id) != null) {
            InfoCacheBean cache = Chat33.loadInfoFromCache(Chat33Const.CHANNEL_ROOM, id);
            RoomListBean bean = new RoomListBean(cache.getId(), cache.getDisplayName(),
                    cache.getAvatar(), cache.getIdentification(), cache.getIdentificationInfo());
            listener.onFindInfo(bean, FROM_NETWORK_OR_TEMP_CACHE);
        } else {
            listener.onNotExist();
        }
    }

    @Override
    public void loadFromDatabase(final OnFindInfoListener<RoomListBean> listener) {
        RoomUtils.subscribe(ChatDatabase.getInstance().roomsDao()
                .getOneRoomById(id), new Consumer<RoomListBean>() {
            @Override
            public void accept(RoomListBean roomListBean) throws Exception {
                listener.onFindInfo(roomListBean, FROM_DATABASE);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                RoomUtils.subscribe(ChatDatabase.getInstance().infoCacheDao()
                        .getOneInfoCache(Chat33Const.CHANNEL_ROOM, id), new Consumer<InfoCacheBean>() {
                    @Override
                    public void accept(InfoCacheBean cache) throws Exception {
                        RoomListBean bean = new RoomListBean(cache.getId(), cache.getDisplayName(),
                                cache.getAvatar(), cache.getIdentification(), cache.getIdentificationInfo());
                        listener.onFindInfo(bean, FROM_NETWORK_OR_TEMP_CACHE);
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
    public void loadFromNetwork(final OnFindInfoListener<RoomListBean> listener) {
        RequestManager.INS.getRoomInfo(id, new RxSubscriber<>(new OnSubscribeListener<RoomInfoBean>() {
            @Override
            public void onSuccess(final RoomInfoBean bean) {
                listener.onFindInfo(new RoomListBean(bean), FROM_NETWORK_OR_TEMP_CACHE);
                RoomUtils.run(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(bean.getId())) {
                            ChatDatabase.getInstance().infoCacheDao().insert(
                                    new InfoCacheBean(bean));
                        }
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
