package com.fzm.chat33.core.converter;

import androidx.room.TypeConverter;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/08/19
 * Description:
 */
public class ListToStringConverter {

    @TypeConverter
    public static List<String> revertList(String message) {
        if (TextUtils.isEmpty(message)) {
            return null;
        }
        return Arrays.asList(message.split(","));
    }

    @TypeConverter
    public static String convertString(List<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            if (!TextUtils.isEmpty(item)) {
                sb.append(item).append(",");
            }
        }
        return sb.toString();
    }
}

