package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.RoomInfoBean;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * @author zhengjy
 * @since 2018/12/05
 * Description:
 */
@Deprecated
@Dao
public interface RoomInfoDao {

    @Query("SELECT * FROM room_info")
    Flowable<List<RoomInfoBean>> getAllRoomsInfo();

    @Query("SELECT * FROM room_info WHERE id=:id")
    Flowable<RoomInfoBean> getRoomInfo(String id);

    @Query("SELECT * FROM room_info WHERE markId=:id")
    Maybe<RoomInfoBean> getRoomInfoByUid(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(RoomInfoBean bean);

    @Query("UPDATE room_info SET disableDeadline=:deadline WHERE id=:id")
    void changeDisableDeadline(String id, long deadline);
}
