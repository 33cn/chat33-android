package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.fzm.chat33.core.global.UserInfo;

/**
 * @author zhengjy
 * @since 2019/08/20
 * Description:
 */
@Dao
public interface UserInfoDao {

    @Query("SELECT * FROM user_info WHERE uid=:uid")
    UserInfo getUserInfoByUid(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserInfo info);

    @Update
    void update(UserInfo info);

    @Query("UPDATE user_info SET username=:name WHERE id=:id")
    void updateName(String id, String name);

    @Query("UPDATE user_info SET avatar=:avatar WHERE id=:id")
    void updateAvatar(String id, String avatar);

    @Query("UPDATE user_info SET isSetPayPwd=:isSetPayPwd WHERE id=:id")
    void updateIsSetPwd(String id, int isSetPayPwd);

    @Query("UPDATE user_info SET verified=:verified WHERE id=:id")
    void updateVerified(String id, int verified);

    @Query("UPDATE user_info SET firstLogin=:firstLogin WHERE id=:id")
    void updateFirstLogin(String id, boolean firstLogin);

    @Query("UPDATE user_info SET identification=:identification WHERE id=:id")
    void updateIdentification(String id, int identification);

    @Query("UPDATE user_info SET identificationInfo=:identificationInfo WHERE id=:id")
    void updateIdentificationInfo(String id, String identificationInfo);
}
