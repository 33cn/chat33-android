package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.RoomKey;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * @author zhengjy
 * @since 2019/05/29
 * Description:
 */
@Dao
public interface RoomKeyDao {

    @Query("SELECT * FROM room_key WHERE roomId=:roomId AND kid=:kid")
    RoomKey getRoomKeyById(String roomId, String kid);

    @Query("SELECT * FROM room_key WHERE roomId=:roomId ORDER BY kid DESC LIMIT 1")
    RoomKey getLatestKey(String roomId);

    @Query("SELECT * FROM room_key WHERE roomId=:roomId AND kid=:kid")
    Maybe<RoomKey> loadRoomKeyById(String roomId, String kid);

    @Query("SELECT * FROM room_key WHERE roomId=:roomId ORDER BY kid DESC LIMIT 1")
    Flowable<RoomKey> loadLatestKey(String roomId);

    @Query("UPDATE room_key SET `key`=:key WHERE roomId=:roomId AND kid=:kid")
    void updateKey(String roomId, String kid, String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomKey roomKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<RoomKey> roomKeys);
}
