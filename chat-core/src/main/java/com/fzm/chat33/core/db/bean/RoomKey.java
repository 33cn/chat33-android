package com.fzm.chat33.core.db.bean;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fuzamei.common.utils.RoomUtils;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.manager.CipherManager;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/05/29
 * Description:
 */
@Entity(tableName = "room_key", primaryKeys = {"roomId", "kid"})
public class RoomKey implements Serializable {

    @NonNull
    private String roomId;

    @NonNull
    private String kid;

    @Nullable
    private String key;

    @Nullable
    private String originKey;

    @Nullable
    private String fromKey;

    public RoomKey() {
    }

    @Ignore
    public RoomKey(@NonNull String roomId, String kid, String key) {
        this.roomId = roomId;
        this.kid = kid;
        this.key = key;
    }

    @Ignore
    public RoomKey(@NonNull String roomId, String kid, String originKey, String fromKey) {
        this.roomId = roomId;
        this.kid = kid;
        this.originKey = originKey;
        this.fromKey = fromKey;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    public void setKey(@Nullable String key) {
        this.key = key;
    }

    @Nullable
    public String getOriginKey() {
        return originKey;
    }

    public void setOriginKey(@Nullable String originKey) {
        this.originKey = originKey;
    }

    @Nullable
    public String getFromKey() {
        return fromKey;
    }

    public void setFromKey(@Nullable String fromKey) {
        this.fromKey = fromKey;
    }

    public String getKeySafe() {
        if (key != null) {
            return key;
        } else if (originKey != null) {
            if (CipherManager.hasDHKeyPair()) {
                String result = CipherManager.decryptString(originKey, fromKey, CipherManager.getPrivateKey());
                if (originKey != null && !originKey.equals(result)) {
                    RoomUtils.run(new Runnable() {
                        @Override
                        public void run() {
                            ChatDatabase.getInstance().roomKeyDao().updateKey(roomId, kid, result);
                        }
                    });
                    this.key = result;
                    return result;
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
}
