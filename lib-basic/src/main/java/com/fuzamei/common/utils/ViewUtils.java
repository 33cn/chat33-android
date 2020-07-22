package com.fuzamei.common.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.widget.EditText;

/**
 * @author Mark
 * @since 2018/9/25
 * Description:
 */
public class ViewUtils {

    public static void setEditTextHintSize(EditText editText, String hintText, int size) {
        SpannableString ss = new SpannableString(hintText);//定义hint的值
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(size, true);//设置字体大小 true表示单位是sp
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(new SpannedString(ss));
    }

    public static void setEditTextHintSize(EditText editText, int size) {
        setEditTextHintSize(editText, editText.getHint().toString(), size);
    }
}
