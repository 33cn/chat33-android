package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.InfoCacheBean;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author zhengjy
 * @since 2018/10/29
 * Description:
 */
@Dao
public interface InfoCacheDao {

    @Query("SELECT * FROM info_cache")
    Flowable<List<InfoCacheBean>> getAllInfoCache();

    @Query("SELECT * FROM info_cache WHERE channelType=:channelType AND id=:id")
    Flowable<InfoCacheBean> getInfoCache(int channelType, String id);

    @Query("SELECT * FROM info_cache WHERE channelType=:channelType AND id=:id")
    Single<InfoCacheBean> getOneInfoCache(int channelType, String id);

    @Query("SELECT * FROM info_cache WHERE channelType=:channelType AND id=:id")
    InfoCacheBean getOneInfoCacheSync(int channelType, String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(InfoCacheBean bean);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<InfoCacheBean> bean);

    @Query("UPDATE info_cache SET remark=:remark WHERE channelType=:channelType AND id=:id")
    void updateRemark(String remark, int channelType, String id);

    @Query("UPDATE info_cache SET nickname=:nickname WHERE channelType=:channelType AND id=:id")
    void updateName(String nickname, int channelType, String id);

    @Query("UPDATE info_cache SET avatar=:avatar WHERE channelType=:channelType AND id=:id")
    void updateAvatar(String avatar, int channelType, String id);
}
