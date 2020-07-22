package com.fzm.chat33.core.converter;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import com.fzm.chat33.core.db.bean.RewardDetail;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author zhengjy
 * @since 2019/02/14
 * Description:
 */
public class RewardToStringConverter {

    private static Gson gson = new Gson();

    @TypeConverter
    public static RewardDetail revertReward(String message) {
        if (TextUtils.isEmpty(message)) {
            return new RewardDetail();
        } else {
            return gson.fromJson(message, new TypeToken<RewardDetail>() {
            }.getType());
        }
    }

    @TypeConverter
    public static String convertString(RewardDetail reward) {
        if (reward == null) {
            return "";
        } else {
            return gson.toJson(reward);
        }
    }
}
