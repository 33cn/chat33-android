package com.fzm.chat33.core.converter;

import androidx.room.TypeConverter;

import com.fzm.chat33.core.db.bean.BriefChatLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * @author zhengjy
 * @since 2018/12/25
 * Description:
 */
public class ChatLogToStringConverter {

    private static Gson gson = new Gson();

    @TypeConverter
    public static List<BriefChatLog> revertList(String message) {
        return gson.fromJson(message, new TypeToken<List<BriefChatLog>>() {
        }.getType());
    }

    @TypeConverter
    public static String convertString(List<BriefChatLog> list) {
        return gson.toJson(list);
    }
}
