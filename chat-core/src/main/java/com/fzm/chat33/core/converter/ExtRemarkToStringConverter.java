package com.fzm.chat33.core.converter;

import androidx.room.TypeConverter;

import com.fzm.chat33.core.db.bean.ExtRemark;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author zhengjy
 * @since 2019/02/14
 * Description:
 */
public class ExtRemarkToStringConverter {

    private static Gson gson = new Gson();

    @TypeConverter
    public static ExtRemark revertList(String message) {
        return gson.fromJson(message, new TypeToken<ExtRemark>() {
        }.getType());
    }

    @TypeConverter
    public static String convertString(ExtRemark extRemark) {
        return gson.toJson(extRemark);
    }
}
