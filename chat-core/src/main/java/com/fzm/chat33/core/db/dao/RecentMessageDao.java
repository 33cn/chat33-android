package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.RecentContact;
import com.fzm.chat33.core.db.bean.RecentMessage;
import com.fzm.chat33.core.db.bean.RecentMessageBean;
import com.fzm.chat33.core.db.bean.RewardDetail;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * @author zhengjy
 * @since 2018/10/29
 * Description:
 */
@Dao
public interface RecentMessageDao {

    @Query("SELECT * FROM recent_message WHERE channelType=:channelType")
    Maybe<List<RecentMessage>> mayGetRecentMsgList(int channelType);

    @Query("SELECT * FROM recent_message WHERE channelType=:channelType")
    List<RecentMessage> getRecentMsgListSync(int channelType);

    @Query("SELECT * FROM recent_message WHERE channelType=:channelType AND id=:id")
    RecentMessage getRecentMsgById(int channelType, String id);

    @Query("SELECT * FROM recent_message WHERE channelType=:channelType AND id=:id")
    Flowable<RecentMessage> observeSingleMsg(int channelType, String id);

    @Query("SELECT recent_message.id, recent_message.isDeleted, recent_message.stickyTop, recent_message.number, recent_message.channelType, RoomView.name, RoomView.remark, RoomView.avatar, recent_message.depositAddress, recent_message.noDisturb, RoomView.identification, recent_message.datetime, recent_message.msgType, recent_message.content, recent_message.name AS fileName, recent_message.isSnap, recent_message.fromId, recent_message.redBagRemark AS redBagRemark, recent_message.inviterId, recent_message.beAit, recent_message.recent_like, recent_message.recent_reward, recent_message.targetId, recent_message.nickname, recent_message.disableDeadline, RoomView.onTop, RoomView.noDisturbing " +
            "FROM recent_message " +
            "LEFT JOIN RoomView ON recent_message.id = RoomView.id " +
            "WHERE recent_message.channelType = 2 " +
            "GROUP BY RoomView.id")
    Flowable<List<RecentMessageBean>> getRoomRecentMsgList();

    @Query("SELECT recent_message.id, recent_message.isDeleted, recent_message.stickyTop, recent_message.number, recent_message.channelType, FriendView.name, FriendView.remark, FriendView.avatar, recent_message.depositAddress, recent_message.noDisturb, FriendView.identification, recent_message.datetime, recent_message.msgType, recent_message.content, recent_message.name AS fileName, recent_message.isSnap, recent_message.fromId, recent_message.redBagRemark AS redBagRemark, recent_message.inviterId, recent_message.beAit, recent_message.recent_like, recent_message.recent_reward, recent_message.targetId, recent_message.nickname, recent_message.disableDeadline, FriendView.onTop, FriendView.noDisturbing " +
            "FROM recent_message " +
            "LEFT JOIN FriendView ON recent_message.id = FriendView.id " +
            "WHERE recent_message.channelType = 3 " +
            "GROUP BY FriendView.id")
    Flowable<List<RecentMessageBean>> getFriendRecentMsgList();

    @Query("SELECT * FROM recent_message ORDER BY datetime DESC")
    Maybe<List<RecentMessage>> getAllRecentMsgs();

    @Query("SELECT number FROM recent_message WHERE channelType=:channelType AND id=:id")
    Integer getUnreadMsgCount(int channelType, String id);

    @Query("UPDATE recent_message SET stickyTop=:sticky WHERE id=:id AND stickyTop!=:sticky")
    void changeSticky(String id, int sticky);

    @Query("UPDATE recent_message SET noDisturb=:noDisturb WHERE id=:id AND noDisturb!=:noDisturb")
    void changeDisturb(String id, int noDisturb);

    @Query("UPDATE recent_message SET encrypt=:encrypt WHERE id=:id AND encrypt!=:encrypt")
    void changeEncrypt(String id, int encrypt);

    @Query("UPDATE recent_message SET encrypt=:encrypt WHERE channelType=3")
    void changeEncryptAll(int encrypt);

    @Query("UPDATE recent_message SET disableDeadline=:deadline WHERE id=:id")
    void changeDisableDeadline(String id, long deadline);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(RecentMessage message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<RecentMessage> messages);

    @Query("SELECT sum(number) FROM recent_message WHERE channelType=:channelType AND noDisturb!=1")
    Flowable<Integer> getMsgCountByChannel(int channelType);

    @Query("SELECT sum(number) FROM recent_message WHERE channelType=:channelType AND id=:id")
    Maybe<Integer> getMsgCount(int channelType, String id);

    @Query("SELECT sum(number) FROM recent_message WHERE noDisturb!=1")
    Flowable<Integer> getAllMsgCount();

    @Query("SELECT sum(number) FROM recent_message WHERE id!=:targetId AND noDisturb!=1")
    Flowable<Integer> getMsgCountOut(String targetId);

    @Query("UPDATE recent_message SET number=0 WHERE channelType=:channelType AND id=:id")
    void clearUnreadMsg(int channelType, String id);

    @Query("UPDATE recent_message SET beAit=:beAit WHERE channelType=:channelType AND id=:id")
    void clearAitMsg(int channelType, String id, boolean beAit);

    @Query("DELETE FROM recent_message WHERE channelType=:channelType AND id=:id")
    void deleteMessage(int channelType, String id);

    @Query("UPDATE recent_message SET isDeleted=:isDelete WHERE channelType=:channelType AND id=:id AND isDeleted!=:isDelete")
    void markDelete(boolean isDelete, int channelType, String id);

    @Query("SELECT recent_message.id, recent_message.channelType, friends.name, friends.remark, friends.avatar, recent_message.datetime, recent_message.depositAddress, recent_message.noDisturb, recent_message.stickyTop, friends.identificationInfo " +
            "FROM recent_message " +
            "INNER JOIN friends ON recent_message.id = friends.id " +
            "WHERE recent_message.channelType = 3")
    Flowable<List<RecentContact>> getRecentFriend();

    @Query("SELECT recent_message.id, recent_message.channelType, room_list.name, room_list.name AS remark, room_list.avatar, recent_message.datetime, recent_message.depositAddress, recent_message.noDisturb, recent_message.stickyTop, room_list.identificationInfo " +
            "FROM recent_message " +
            "INNER JOIN room_list ON recent_message.id = room_list.id " +
            "WHERE recent_message.channelType = 2")
    Flowable<List<RecentContact>> getRecentRoom();

    @Query("UPDATE recent_message SET recent_like = 0, recent_reward = 0 WHERE id=:id AND channelType=:channelType")
    void clearPraise(int channelType, String id);
}
