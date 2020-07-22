package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.RoomContact;
import com.fzm.chat33.core.db.bean.RoomUserBean;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author zhengjy
 * @since 2018/12/05
 * Description:
 */
@Dao
public interface RoomUserDao {

    @Query("SELECT * FROM room_user")
    Flowable<List<RoomUserBean>> getAllRoomUsers();

    @Query("SELECT * FROM room_user WHERE roomId=:roomId")
    Flowable<List<RoomUserBean>> getRoomUsers(String roomId);

    @Query("SELECT * FROM room_user WHERE roomId=:roomId AND id=:id")
    Flowable<RoomUserBean> getRoomUser(String roomId, String id);

    @Query("SELECT * FROM room_user WHERE roomId=:roomId AND id=:id")
    RoomUserBean getRoomUserSync(String roomId, String id);

    @Query("SELECT * FROM room_user WHERE roomId=:roomId AND id=:id")
    Single<RoomUserBean> mayGetRoomUser(String roomId, String id);

    @Query("UPDATE room_user SET roomNickname=:roomNickname WHERE roomId=:roomId AND id=:id")
    void updateNickname(String roomId, String id, String roomNickname);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(RoomUserBean bean);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<RoomUserBean> beans);

    @Query("SELECT room_user.roomId, room_user.id,room_user.nickname,room_user.roomNickname,room_user.avatar,room_user.memberLevel,room_user.roomMutedType,room_user.mutedType,room_user.deadline,room_user.identification,room_user.identificationInfo,room_user.searchKey,friends.remark AS friendRemark,friends.searchKey AS friendSearchKey FROM room_user LEFT JOIN friends ON room_user.id == friends.id WHERE room_user.roomId=:roomId")
    List<RoomContact> getRoomContacts(String roomId);

    @Query("SELECT room_user.roomId, room_user.id,room_user.nickname,room_user.roomNickname,room_user.avatar,room_user.memberLevel,room_user.roomMutedType,room_user.mutedType,room_user.deadline,room_user.identification,room_user.identificationInfo,room_user.searchKey,friends.remark AS friendRemark,friends.searchKey AS friendSearchKey FROM room_user LEFT JOIN friends ON room_user.id == friends.id WHERE room_user.roomId=:roomId AND (room_user.nickname LIKE '%'||:keywords||'%' OR room_user.roomNickname LIKE '%'||:keywords||'%' OR room_user.searchKey LIKE '% '||:keywords||'%' OR friends.searchKey LIKE '% '||:keywords||'%' ESCAPE '/')")
    List<RoomContact> searchRoomContacts(String roomId, String keywords);

    @Query("SELECT room_user.roomId, room_user.id,room_user.nickname,room_user.roomNickname,room_user.avatar,room_user.memberLevel,room_user.roomMutedType,room_user.mutedType,room_user.deadline,room_user.identification,room_user.identificationInfo,room_user.searchKey,friends.remark AS friendRemark,friends.searchKey AS friendSearchKey FROM room_user LEFT JOIN friends ON room_user.id == friends.id WHERE room_user.roomId=:roomId AND room_user.memberLevel<=:memberLevel")
    List<RoomContact> getRoomContactsByLevel(String roomId, int memberLevel);

    @Query("SELECT room_user.roomId, room_user.id,room_user.nickname,room_user.roomNickname,room_user.avatar,room_user.memberLevel,room_user.roomMutedType,room_user.mutedType,room_user.deadline,room_user.identification,room_user.identificationInfo,room_user.searchKey,friends.remark AS friendRemark,friends.searchKey AS friendSearchKey FROM room_user LEFT JOIN friends ON room_user.id == friends.id WHERE room_user.roomId=:roomId AND room_user.memberLevel<=:memberLevel AND (room_user.nickname LIKE '%'||:keywords||'%' OR room_user.roomNickname LIKE '%'||:keywords||'%' OR room_user.searchKey LIKE '% '||:keywords||'%' OR friends.searchKey LIKE '% '||:keywords||'%' ESCAPE '/')")
    List<RoomContact> searchRoomContactsByLevel(String roomId, int memberLevel, String keywords);

    @Query("DELETE FROM room_user WHERE roomId=:roomId")
    void deleteRoomUsers(String roomId);
}
