package com.fzm.chat33.core.net;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.fuzamei.common.bus.LiveBus;
import com.fuzamei.common.net.rxjava.HttpResponseFunc;
import com.fuzamei.common.net.rxjava.HttpResult;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.componentservice.app.BusEvent;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.bean.AdInfoBean;
import com.fzm.chat33.core.bean.ApplyInfoBean;
import com.fzm.chat33.core.bean.ChatGroupBean;
import com.fzm.chat33.core.bean.ConditionReward;
import com.fzm.chat33.core.bean.DepositSMS;
import com.fzm.chat33.core.bean.GroupNotice;
import com.fzm.chat33.core.bean.ModuleState;
import com.fzm.chat33.core.bean.PromoteBriefInfo;
import com.fzm.chat33.core.bean.PromoteReward;
import com.fzm.chat33.core.bean.RecommendGroup;
import com.fzm.chat33.core.bean.RedPacketCoin;
import com.fzm.chat33.core.bean.RedPacketReceiveInfo;
import com.fzm.chat33.core.bean.RedPacketRecord;
import com.fzm.chat33.core.bean.ResultList;
import com.fzm.chat33.core.bean.RoomSessionKeys;
import com.fzm.chat33.core.bean.SettingInfoBean;
import com.fzm.chat33.core.bean.param.AddQuestionParam;
import com.fzm.chat33.core.bean.param.ChatGroupParam;
import com.fzm.chat33.core.bean.param.CreateGroupParam;
import com.fzm.chat33.core.bean.param.EditExtRemarkParam;
import com.fzm.chat33.core.bean.param.EditRoomUserParam;
import com.fzm.chat33.core.bean.param.JoinGroupParam;
import com.fzm.chat33.core.db.bean.InfoCacheBean;
import com.fzm.chat33.core.request.WithdrawRequest;
import com.fzm.chat33.core.response.SendRedPacketResponse;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.RoomInfoBean;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.request.ChatFileHistoryRequest;
import com.fzm.chat33.core.request.ChatLogHistoryRequest;
import com.fzm.chat33.core.request.PayPasswordRequest;
import com.fzm.chat33.core.request.ReceiveRedPacketRequest;
import com.fzm.chat33.core.request.RedPacketRecordRequest;
import com.fzm.chat33.core.request.SendRedPacketRequest;
import com.fzm.chat33.core.request.chat.ForwardRequest;
import com.fuzamei.common.net.rxjava.HttpResultFunc;
import com.fuzamei.common.net.rxjava.RxJavaUtil;
import com.fuzamei.common.net.subscribers.RxSubscriber;
import com.fzm.chat33.core.bean.RelationshipBean;
import com.fzm.chat33.core.bean.UidSearchBean;
import com.fzm.chat33.core.bean.UnreadNumber;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.response.BoolResponse;
import com.fzm.chat33.core.response.ReceiveRedPacketResponse;
import com.fzm.chat33.core.response.RedPacketInfoResponse;
import com.fzm.chat33.core.response.StateResponse;
import com.fzm.chat33.core.response.ChatListResponse;
import com.fzm.chat33.core.response.WithdrawResponse;
import com.fzm.chat33.core.source.impl.DatabaseLocalContactDataSource;
import com.fzm.chat33.core.utils.UserInfoPreference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:
 */
public enum RequestManager {

    /**
     * 单例
     */
    INS;

    static class Holder {
        private static ApiService apiService = AppRetrofitProvider.getRetrofit().create(ApiService.class);
    }

    public ApiService getAPIService() {
        return Holder.apiService;
    }

    public RxSubscriber<Object> setDeviceToken(String deviceToken, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        // 友盟推送token
        map.put("deviceToken", deviceToken);
        Observable<Object> observable = getAPIService().setDeviceToken(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> logout(RxSubscriber<Object> subscriber) {
        Observable<Object> observable = getAPIService().logout().map(new HttpResultFunc<Object>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<ChatGroupBean.Wrapper> getChatGroupList(ChatGroupParam param, RxSubscriber<ChatGroupBean.Wrapper> subscriber) {
        Observable<ChatGroupBean.Wrapper> observable = getAPIService().getChatGroupList(param).map(new HttpResultFunc<ChatGroupBean.Wrapper>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<ChatListResponse> getChatLogHistory(ChatLogHistoryRequest request, int channelType, RxSubscriber<ChatListResponse> subscriber) {
        String url = "";
        Map<String, Object> map = new HashMap<>();
        if (channelType == Chat33Const.CHANNEL_GROUP) {
            url = ApiService.getGroupChatLog;
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            url = ApiService.getRoomChatLog;
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            url = ApiService.getPrivateChatLog;
        }
        map.put("id", request.id);
        map.put("startId", request.startId);
        map.put("number", request.number);
        Observable<ChatListResponse> observable = getAPIService().getChatLogHistory(url, map).map(new HttpResultFunc<ChatListResponse>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public Observer<ChatListResponse> getChatFileHistory(ChatFileHistoryRequest request, int channelType, Observer<ChatListResponse> subscriber) {
        String url = "";
        Map<String, Object> map = new HashMap<>();
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            url = ApiService.getRoomChatFile;
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            url = ApiService.getPrivateChatFile;
        }
        map.put("id", request.id);
        map.put("startId", request.startId);
        map.put("number", request.number);
        if (request.owner != null) {
            map.put("owner", request.owner);
        }
        if (request.query != null) {
            map.put("query", request.query);
        }
        Observable<ChatListResponse> observable = getAPIService().getChatLogHistory(url, map).map(new HttpResultFunc<ChatListResponse>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public Observer<ChatListResponse> getChatMediaHistory(ChatFileHistoryRequest request, int channelType, Observer<ChatListResponse> subscriber) {
        String url = "";
        Map<String, Object> map = new HashMap<>();
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            url = ApiService.getRoomChatMedia;
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            url = ApiService.getPrivateChatMedia;
        }
        map.put("id", request.id);
        map.put("startId", request.startId);
        map.put("number", request.number);
        Observable<ChatListResponse> observable = getAPIService().getChatLogHistory(url, map).map(new HttpResultFunc<ChatListResponse>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> readSnapMessage(String logId, int type, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("logId", logId);
        map.put("type", type);
        Observable<Object> observable = getAPIService().readSnapMessage(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<GroupNotice.Wrapper> getGroupNoticeList(String roomId, String startId, int number, RxSubscriber<GroupNotice.Wrapper> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("startId", startId);
        map.put("number", number);
        Observable<GroupNotice.Wrapper> observable = getAPIService().getGroupNoticeList(map).map(new HttpResultFunc<GroupNotice.Wrapper>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    @SuppressWarnings("CheckResult")
    public RxSubscriber<RoomListBean.Wrapper> getRoomList(int type, RxSubscriber<RoomListBean.Wrapper> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        Observable<RoomListBean.Wrapper> observable = getAPIService().getRoomList(map).map(new HttpResultFunc<RoomListBean.Wrapper>());
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(new Consumer<RoomListBean.Wrapper>() {
                    @Override
                    public void accept(final RoomListBean.Wrapper wrapper) throws Exception {
                        // 将群列表信息存入本地数据库
                        RoomUtils.run(new Runnable() {
                            @Override
                            public void run() {
                                if (wrapper.roomList == null) {
                                    return;
                                }
                                List<RoomListBean> tmp = new ArrayList<>();
                                for (RoomListBean bean : wrapper.roomList) {
                                    if (!TextUtils.isEmpty(bean.getId())) {
                                        tmp.add(bean);
                                        ChatDatabase.getInstance().recentMessageDao()
                                                .changeDisableDeadline(bean.getId(), bean.getDisableDeadline());
                                    }
                                }
                                ChatDatabase.getInstance().roomsDao().insert(tmp);
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
        return subscriber;
    }

    public RxSubscriber<ApplyInfoBean.Wrapper> getFriendsApplyList(String id, int number, RxSubscriber<ApplyInfoBean.Wrapper> subscriber) {
        Map<String, Object> map = new HashMap<>();
        if (!TextUtils.isEmpty(id)) {
            map.put("id", id);
        }
        map.put("number", number);
        Observable<ApplyInfoBean.Wrapper> observable = getAPIService().getFriendsApplyList(map).map(new HttpResultFunc<ApplyInfoBean.Wrapper>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> dealFriendRequest(String id, int agree, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("agree", agree);
        Observable<Object> observable = getAPIService().dealFriendRequest(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public FriendBean getUserInfoSync(String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        try {
            HttpResult<FriendBean> result = getAPIService().getUserInfoSync(map).execute().body();
            if (result != null) {
                return result.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("CheckResult")
    public RxSubscriber<FriendBean> getUserInfo(String id, RxSubscriber<FriendBean> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        Observable<FriendBean> observable = getAPIService().getUserInfo(map).map(new HttpResultFunc<FriendBean>());
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(new Consumer<FriendBean>() {
                    @Override
                    public void accept(final FriendBean bean) throws Exception {
                        RoomUtils.run(new Runnable() {
                            @Override
                            public void run() {
                                if (DatabaseLocalContactDataSource.get().isLocalFriend(bean.getId())) {
                                    bean.setIsFriend(1);
                                } else {
                                    bean.setIsFriend(0);
                                }
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
        return subscriber;
    }

    public RxSubscriber<PromoteBriefInfo> getPromoteBriefInfo(RxSubscriber<PromoteBriefInfo> subscriber) {
        Observable<PromoteBriefInfo> observable = getAPIService().getPromoteBriefInfo().map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<PromoteReward.Wrapper> getPromoteRewardList(int page, RxSubscriber<PromoteReward.Wrapper> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("page", page);
        map.put("size", AppConfig.PAGE_SIZE);
        Observable<PromoteReward.Wrapper> observable = getAPIService().getPromoteRewardList(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<ConditionReward.Wrapper> getConditionRewardList(int page, RxSubscriber<ConditionReward.Wrapper> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("page", page);
        map.put("size", AppConfig.PAGE_SIZE);
        Observable<ConditionReward.Wrapper> observable = getAPIService().getConditionRewardList(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> stickyOnTop(String id, int channelType, int sticky, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        String url = "";
        if (channelType == Chat33Const.CHANNEL_FRIEND) {
            url = ApiService.friend_sticky_top;
            map.put("id", id);
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            url = ApiService.room_sticky_top;
            map.put("roomId", id);
        } else {
            return null;
        }
        map.put("stickyOnTop", sticky);
        Observable<Object> observable = getAPIService().stickyOnTop(url, map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setDND(String id, int channelType, int dnd, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        String url = "";
        if (channelType == Chat33Const.CHANNEL_FRIEND) {
            url = ApiService.friend_dnd;
            map.put("id", id);
            map.put("setNoDisturbing", dnd);
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            url = ApiService.room_dnd;
            map.put("roomId", id);
            map.put("setNoDisturbing", dnd);
        } else {
            return null;
        }
        Observable<Object> observable = getAPIService().setDND(url, map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    @Deprecated
    public RxSubscriber<Object> setFriendRemark(String id, String remark, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("remark", remark);
        Observable<Object> observable = getAPIService().setFriendRemark(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setFriendExtRemark(EditExtRemarkParam param, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", param.id);
        map.put("remark", param.remark);
        map.put("telephones", param.telephones);
        map.put("description", param.description);
        map.put("pictures", param.pictures);
        Observable<Object> observable = getAPIService().setFriendExtRemark(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> editName(int channelType, String id, String name, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        String url;
        if (channelType == Chat33Const.CHANNEL_GROUP) {
            map.put("groupId", id);
            map.put("groupName", name);
            url = ApiService.group_name;
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            map.put("roomId", id);
            map.put("name", name);
            url = ApiService.room_name;
        } else {
            map.put("nickname", name);
            url = ApiService.my_name;
        }
        Observable<Object> observable = getAPIService().editName(url, map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    @SuppressWarnings("CheckResult")
    public RxSubscriber<Object> editAvatar(final int channelType, final String id, final String avatar, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        String url;
        if (channelType == Chat33Const.CHANNEL_GROUP) {
            map.put("groupId", id);
            url = ApiService.group_avatar;
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            map.put("roomId", id);
            url = ApiService.room_avatar;
        } else {
            url = ApiService.my_avatar;
        }
        map.put("avatar", avatar);
        Observable<Object> observable = getAPIService().editAvatar(url, map).map(new HttpResultFunc<>());
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (channelType == Chat33Const.CHANNEL_ROOM) {
                            Chat33.loadRoomFromCache(id).setAvatar(avatar);
                        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
                            UserInfo.getInstance().setAvatar(avatar);
                            LiveBus.of(BusEvent.class).imageRefresh().setValue(avatar);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
        return subscriber;
    }

    public RxSubscriber<RoomInfoBean> createRoom(CreateGroupParam param, RxSubscriber<RoomInfoBean> subscriber) {
        Observable<RoomInfoBean> observable = getAPIService().createRoom(param).map(new HttpResultFunc<RoomInfoBean>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<UidSearchBean> searchByUid(String markId, RxSubscriber<UidSearchBean> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("markId", markId);
        Observable<UidSearchBean> observable = getAPIService().searchByUid(map).map(new HttpResultFunc<UidSearchBean>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    @SuppressWarnings("CheckResult")
    public RxSubscriber<RoomInfoBean> getRoomInfo(final String roomId, RxSubscriber<RoomInfoBean> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        Observable<RoomInfoBean> observable = getAPIService().getRoomInfo(map).map(new HttpResultFunc<RoomInfoBean>());
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(new Consumer<RoomInfoBean>() {
                    @Override
                    public void accept(final RoomInfoBean roomInfo) throws Exception {
                        // 将群列表信息存入本地数据库
                        final RoomListBean roomListBean = new RoomListBean(roomInfo);
                        RoomUtils.run(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(roomListBean.getId())) {
                                    ChatDatabase.getInstance().roomsDao().insert(roomListBean);
                                    ChatDatabase.getInstance().recentMessageDao().changeDisableDeadline(roomListBean.getId(), roomListBean.getDisableDeadline());
                                }
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
        return subscriber;
    }

    public RoomInfoBean getRoomInfoSync(String roomId) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        try {
            HttpResult<RoomInfoBean> result = getAPIService().getRoomInfoSync(map).execute().body();
            if (result != null) {
                RoomUtils.run(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(result.getData().getId())) {
                            ChatDatabase.getInstance().infoCacheDao().insert(new InfoCacheBean(result.getData()));
                        }
                    }
                });
                return result.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RxSubscriber<RoomInfoBean> getRoomInfoForCache(final String roomId, RxSubscriber<RoomInfoBean> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        Observable<RoomInfoBean> observable = getAPIService().getRoomInfo(map).map(new HttpResultFunc<RoomInfoBean>());
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(new Consumer<RoomInfoBean>() {
                    @Override
                    public void accept(final RoomInfoBean roomInfo) throws Exception {
                        // 将群列表信息存入本地数据库
                        RoomUtils.run(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(roomInfo.getId())) {
                                    ChatDatabase.getInstance().infoCacheDao().insert(new InfoCacheBean(roomInfo));
                                }
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
        return subscriber;
    }

    public RxSubscriber<UnreadNumber> getUnreadApplyNumber(RxSubscriber<UnreadNumber> subscriber) {
        Observable<UnreadNumber> observable = getAPIService().getUnreadApplyNumber().map(new HttpResultFunc<UnreadNumber>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<RecommendGroup.Wrapper> recommendGroups(int times, RxSubscriber<RecommendGroup.Wrapper> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("times", times);
        // 默认单次获取6个推荐群
        map.put("number", 6);
        Observable<RecommendGroup.Wrapper> observable = getAPIService().recommendGroups(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<ResultList> batchJoinRoomApply(List<String> roomIds, RxSubscriber<ResultList> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("rooms", roomIds.toArray());
        Observable<ResultList> observable = getAPIService().batchJoinRoomApply(map).map(new HttpResultFunc<ResultList>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> joinRoomApply(JoinGroupParam param, RxSubscriber<Object> subscriber) {
        Observable<Object> observable = getAPIService().joinRoomApply(param).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> dealJoinRoomApply(String userId, String roomId, int agree, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("roomId", roomId);
        map.put("agree", agree);
        Observable<Object> observable = getAPIService().dealJoinRoomApply(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> quitRoom(String roomId, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        Observable<Object> observable = getAPIService().quitRoom(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> deleteRoom(String roomId, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        Observable<Object> observable = getAPIService().deleteRoom(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<StateResponse> inviteUsers(EditRoomUserParam param, RxSubscriber<StateResponse> subscriber) {
        Observable<StateResponse> observable = getAPIService().inviteUsers(param).map(new HttpResultFunc<StateResponse>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> kickOutUsers(EditRoomUserParam param, RxSubscriber<Object> subscriber) {
        Observable<Object> observable = getAPIService().kickOutUsers(param).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setRoomUserLevel(String roomId, String userId, int level, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("userId", userId);
        map.put("level", level);
        Observable<Object> observable = getAPIService().setRoomUserLevel(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setPermission(String roomId, int canAddFriend, int joinPermission, int recordPermission, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        if (canAddFriend != 0) {
            map.put("canAddFriend", canAddFriend);
        }
        if (joinPermission != 0) {
            map.put("joinPermission", joinPermission);
        }
        if (recordPermission != 0) {
            map.put("recordPermission", recordPermission);
        }
        Observable<Object> observable = getAPIService().setPermission(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<RoomUserBean.Wrapper> getRoomUsers(String roomId, RxSubscriber<RoomUserBean.Wrapper> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        Observable<RoomUserBean.Wrapper> observable = getAPIService().getRoomUsers(map).map(new HttpResultFunc<RoomUserBean.Wrapper>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<RoomUserBean> getRoomUserInfo(String roomId, String userId, RxSubscriber<RoomUserBean> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("userId", userId);
        Observable<RoomUserBean> observable = getAPIService().getRoomUserInfo(map).map(new HttpResultFunc<RoomUserBean>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> revokeMessage(String logId, int type, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("logId", logId);
        map.put("type", type);
        Observable<Object> observable = getAPIService().revokeMessage(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public Observer<StateResponse> revokeFile(List<String> logs, int type, Observer<StateResponse> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("logs", logs.toArray());
        map.put("type", type);
        Observable<StateResponse> observable = getAPIService().revokeFile(map).map(new HttpResultFunc<StateResponse>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<StateResponse> forwardMessage(ForwardRequest request, RxSubscriber<StateResponse> subscriber) {
        Observable<StateResponse> observable = getAPIService().forwardMessage(request).map(new HttpResultFunc<StateResponse>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setMemberNickname(String roomId, String nickname, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("nickname", nickname);
        Observable<Object> observable = getAPIService().setMemberNickname(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> publishNotice(String roomId, String content, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("content", content);
        Observable<Object> observable = getAPIService().publishNotice(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setMutedList(String roomId, int listType, List<String> users, long deadline, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("listType", listType);
        if (users != null) {
            map.put("users", users.toArray());
        }
        if (deadline != -1) {
            map.put("deadline", deadline);
        }
        Observable<Object> observable = getAPIService().setMutedList(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setMutedSingle(String roomId, String userId, long deadline, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("userId", userId);
        map.put("deadline", deadline);
        Observable<Object> observable = getAPIService().setMutedSingle(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<RelationshipBean> hasRelationship(int channelType, String id, RxSubscriber<RelationshipBean> subscriber) {
        String url = "";
        Map<String, Object> map = new HashMap<>();
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            url = ApiService.in_group;
            map.put("roomId", id);
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            url = ApiService.is_friend;
            map.put("friendId", id);
        }
        Observable<RelationshipBean> observable = getAPIService().hasRelationship(url, map).map(new HttpResultFunc<RelationshipBean>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    /********************************************设置中心*******************************************/

    public RxSubscriber<SettingInfoBean> getSettingInfo(RxSubscriber<SettingInfoBean> subscriber) {
        Observable<SettingInfoBean> observable = getAPIService().getSettingInfo().map(new HttpResultFunc<SettingInfoBean>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<ModuleState.Wrapper> getModuleState(RxSubscriber<ModuleState.Wrapper> subscriber) {
        Observable<ModuleState.Wrapper> observable = getAPIService().getModuleState().map(new HttpResultFunc<ModuleState.Wrapper>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setAddVerify(int needVerify, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("tp", needVerify);
        Observable<Object> observable = getAPIService().setAddVerify(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setAddQuestion(AddQuestionParam param, RxSubscriber<Object> subscriber) {
        Observable<Object> observable = getAPIService().setAddQuestion(param).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> setInviteConfirm(int needConfirm, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("needConfirmInvite", needConfirm);
        Observable<Object> observable = getAPIService().setInviteConfirm(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<BoolResponse> checkAnswer(String friendId, String answer, RxSubscriber<BoolResponse> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("friendId", friendId);
        map.put("answer", answer);
        Observable<BoolResponse> observable = getAPIService().checkAnswer(map).map(new HttpResultFunc<BoolResponse>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> startStatistics(RxSubscriber<Object> subscriber) {
        Observable<Object> observable = getAPIService().startStatistics().map(new HttpResultFunc<Object>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public Observable<HttpResult<AdInfoBean>> getSplashAdInfo() {
        return getAPIService().getSplashAdInfo();
    }

    public void downloadSplashAd(String path, String name, String fileUrl) throws IOException {
        File file = new File(path, name);
        Request request = new Request.Builder().get().url(fileUrl).build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();
        Response response = client.newCall(request).execute();
        InputStream is = null;
        byte[] buf = new byte[4096];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public RxSubscriber<RoomSessionKeys> getRoomSessionKeys(long datetime, RxSubscriber<RoomSessionKeys> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("datetime", datetime);
        Observable<RoomSessionKeys> observable = getAPIService().getRoomSessionKeys(map).map(new HttpResultFunc<RoomSessionKeys>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    /********************************************钱包、支付密码、新版红包等相关接口*******************************************/

    public RxSubscriber<DepositSMS> sendSMS(String area, String mobile, String codetype, String param, String extend_param,
                                            String businessId, String ticket, RxSubscriber<DepositSMS> subscriber) {
        Observable<DepositSMS> observable = getAPIService().sendSMS(area, mobile, codetype, param,
                extend_param, businessId, ticket).map(new HttpResponseFunc<>(200));
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<DepositSMS> sendVoiceCode(String area, String mobile, String codetype, String param,
                                                  String businessId, String ticket, RxSubscriber<DepositSMS> subscriber) {
        Observable<DepositSMS> observable = getAPIService().sendVoiceCode(area, mobile, codetype, param,
                businessId, ticket).map(new HttpResponseFunc<>(200));
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> verifyCode(String area, String mobile, String email, String codetype,
                                           String type, String code, RxSubscriber<Object> subscriber) {
        Observable<Object> observable = getAPIService().verifyCode(area, mobile, email, codetype,
                type, code).map(new HttpResponseFunc<>(200));
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    @SuppressLint("CheckResult")
    public RxSubscriber<StateResponse> isSetPayPassword(RxSubscriber<StateResponse> subscriber) {
        Observable<StateResponse> observable = getAPIService().isSetPayPassword().map(new HttpResultFunc<>());
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<StateResponse>() {
                    @Override
                    public void accept(StateResponse stateResponse) throws Exception {
                        UserInfoPreference.getInstance().setBooleanPref(UserInfoPreference.SET_PAY_PASSWORD, stateResponse.state == 1);
                        UserInfo.getInstance().setIsSetPayPwd(stateResponse.state);
                    }
                })
                .subscribeWith(subscriber);
        return subscriber;
    }

    @SuppressLint("CheckResult")
    public RxSubscriber<Object> setPayPassword(PayPasswordRequest request, RxSubscriber<Object> subscriber) {
        Observable<Object> observable = getAPIService().setPayPassword(request).map(new HttpResultFunc<>());
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        UserInfo.getInstance().setIsSetPayPwd(1);
                    }
                })
                .subscribeWith(subscriber);
        return subscriber;
    }

    public RxSubscriber<Object> checkPayPassword(String payPassword, RxSubscriber<Object> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("payPassword", payPassword);
        Observable<Object> observable = getAPIService().checkPayPassword(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<RedPacketCoin.Wrapper> packetBalance(RxSubscriber<RedPacketCoin.Wrapper> subscriber) {
        Observable<RedPacketCoin.Wrapper> observable = getAPIService().packetBalance().map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public Call<HttpResult<RedPacketCoin.Wrapper>> packetBalanceSync() {
        return getAPIService().packetBalanceSync();
    }

    public RxSubscriber<SendRedPacketResponse> sendRedPacket(SendRedPacketRequest request, RxSubscriber<SendRedPacketResponse> subscriber) {
        Observable<SendRedPacketResponse> observable = getAPIService().sendRedPacket(request).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<ReceiveRedPacketResponse> receiveRedPacket(ReceiveRedPacketRequest request, RxSubscriber<ReceiveRedPacketResponse> subscriber) {
        Observable<ReceiveRedPacketResponse> observable = getAPIService().receiveRedPacket(request).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<RedPacketInfoResponse> redPacketInfo(String packetId, RxSubscriber<RedPacketInfoResponse> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("packetId", packetId);
        Observable<RedPacketInfoResponse> observable = getAPIService().redPacketInfo(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public RxSubscriber<RedPacketReceiveInfo.Wrapper> redPacketReceiveList(String packetId, RxSubscriber<RedPacketReceiveInfo.Wrapper> subscriber) {
        Map<String, Object> map = new HashMap<>();
        map.put("packetId", packetId);
        Observable<RedPacketReceiveInfo.Wrapper> observable = getAPIService().redPacketReceiveList(map).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public Observer<RedPacketRecord> redPacketRecord(RedPacketRecordRequest request, Observer<RedPacketRecord> subscriber) {
        Observable<RedPacketRecord> observable = getAPIService().redPacketRecord(request).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }

    public Observer<WithdrawResponse> withdraw(WithdrawRequest request, Observer<WithdrawResponse> subscriber) {
        Observable<WithdrawResponse> observable = getAPIService().withdraw(request).map(new HttpResultFunc<>());
        RxJavaUtil.toSubscribe(observable, subscriber);
        return subscriber;
    }
}
