package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.FriendView;

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
public interface FriendsDao {

    @Query("SELECT * FROM friends WHERE isBlocked != 1 ")
    Flowable<List<FriendBean>> getAllFriends();

    @Query("SELECT * FROM friends")
    Flowable<List<FriendBean>> getAllFriendsWithBlocked();

    @Query("SELECT * FROM friends WHERE id=:id")
    Flowable<FriendBean> getFriendById(String id);

    @Deprecated
    @Query("SELECT * FROM friends WHERE id=:id")
    FriendBean getFriendByIdSync(String id);

    @Query("SELECT * FROM FriendView WHERE id=:id")
    FriendView getFriendFromView(String id);

    @Query("SELECT * FROM friends WHERE id=:id")
    Maybe<FriendBean> mayGetFriendById(String id);

    @Deprecated
    @Query("SELECT * FROM friends WHERE id=:id")
    Single<FriendBean> getOneFriendById(String id);

    @Query("DELETE FROM friends WHERE id=:id")
    void delete(String id);

    @Query("DELETE FROM friends WHERE isBlocked!=1")
    void deleteFriends();

    @Query("DELETE FROM friends WHERE isBlocked=1")
    void deleteBlocked();

    @Query("UPDATE friends SET onTop=:sticky WHERE id=:id AND onTop!=:sticky")
    void changeSticky(String id, int sticky);

    @Query("UPDATE friends SET noDisturbing=:dnd WHERE id=:id AND noDisturbing!=:dnd")
    void changeDnd(String id, int dnd);

    @Query("UPDATE friends SET encrypt=:encrypt WHERE id=:id AND encrypt!=:encrypt")
    void changeEncrypt(String id, int encrypt);

    @Query("UPDATE friends SET encrypt=:encrypt")
    void changeEncryptAll(int encrypt);

    @Query("UPDATE friends SET publicKey=:publicKey WHERE id=:userId")
    void changePublicKey(String userId, String publicKey);

    @Query("UPDATE friends SET remark=:remark WHERE id=:id")
    void updateRemark(String id, String remark);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(FriendBean friend);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<FriendBean> friends);

    @Query("SELECT * FROM friends WHERE isBlocked != 1 AND (name LIKE '%'||:keywords||'%' OR remark LIKE '%'||:keywords||'%' OR searchKey LIKE '% '||:keywords||'%' ESCAPE '/')")
    List<FriendBean> searchFriends(String keywords);

    @Query("SELECT * FROM friends WHERE (name LIKE '%'||:keywords||'%' OR remark LIKE '%'||:keywords||'%' OR searchKey LIKE '% '||:keywords||'%' ESCAPE '/')")
    List<FriendBean> searchFriendsWithBlocked(String keywords);

    @Query("UPDATE friends SET isBlocked=:isBlocked WHERE id=:id")
    void updateBlocked(String id, int isBlocked);
}
