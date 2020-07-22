package com.fzm.chat33.core.bean

import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author zhengjy
 * @since 2019/09/09
 * Description:多端登录信息
 */
class MultipleLogin(
        time: Long,
        device: String,
        way: Int
) : ServerTips(time, device, way) {

    // 1 ：短信验证码；2 密码； 3 邮箱验证码； 4 邮箱密码
    private fun getWay(): String {
        return when (way) {
            1 -> Chat33.getContext().getString(R.string.core_login_by_code)
            2 -> Chat33.getContext().getString(R.string.core_login_by_password)
            3 -> Chat33.getContext().getString(R.string.core_login_by_mail_code)
            4 -> Chat33.getContext().getString(R.string.core_login_by_mail_password)
            else -> Chat33.getContext().getString(R.string.core_login_by_other)
        }
    }

    override fun toString(): String {
        return Chat33.getContext().getString(R.string.core_login_way, SimpleDateFormat(PATTERN, Locale.CHINESE).format(time), device, getWay())
    }
}