package com.fzm.chat33.utils

import android.text.Editable
import android.text.TextWatcher

/**
 * @author zhengjy
 * @since 2019/03/12
 * Description:
 */
abstract class SimpleTextWatcher : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable?) {

    }
}