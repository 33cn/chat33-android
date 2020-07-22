package com.fzm.chat33.core.bean

import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author zhengjy
 * @since 2019/11/08
 * Description:
 */
class UpdateWords(
        time: Long,
        device: String,
        way: Int
) : ServerTips(time, device, way) {

    override fun toString(): String {
        return Chat33.getContext().getString(R.string.core_update_words_other, SimpleDateFormat(PATTERN, Locale.CHINESE).format(time), device)
    }
}