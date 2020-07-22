package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.db.bean.RoomView;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author zhengjy
 * @since 2018/10/31
 * Description:
 */
@Dao
public interface RoomsDao {

    @Query("SELECT * FROM room_list")
    Flowable<List<RoomListBean>> getAllRooms();

    @Query("SELECT * FROM room_list")
    List<RoomListBean> getAllRoomsOnce();

    @Query("SELECT * FROM room_list WHERE id=:id")
    Flowable<RoomListBean> getRoomById(String id);

    @Deprecated
    @Query("SELECT * FROM room_list WHERE id=:id")
    RoomListBean getRoomByIdSync(String id);

    @Query("SELECT * FROM RoomView WHERE id=:id")
    RoomView getRoomFromView(String id);

    @Query("SELECT * FROM room_list WHERE id=:id")
    Maybe<RoomListBean> mayGetRoomById(String id);

    @Deprecated
    @Query("SELECT * FROM room_list WHERE id=:id")
    Single<RoomListBean> getOneRoomById(String id);

    @Query("DELETE FROM room_list WHERE id=:id")
    void delete(String id);

    @Query("UPDATE room_list SET onTop=:sticky WHERE id=:id AND onTop!=:sticky")
    void changeSticky(String id, int sticky);

    @Query("UPDATE room_list SET noDisturbing=:dnd WHERE id=:id AND noDisturbing!=:dnd")
    void changeDnd(String id, int dnd);

    @Query("UPDATE room_list SET disableDeadline=:deadline WHERE id=:id")
    void changeDisableDeadline(String id, long deadline);

    @Query("UPDATE room_list SET name=:name WHERE id=:id")
    void updateName(String id, String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<RoomListBean> roomsInfo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(RoomListBean roomsInfo);

    @Query("SELECT * FROM room_list WHERE name LIKE '%'||:keywords||'%' OR searchKey LIKE '% '||:keywords||'%' ESCAPE '/'")
    List<RoomListBean> searchGroups(String keywords);
}
