package com.fuzamei.common.ext

import android.annotation.SuppressLint
import com.fuzamei.common.FzmFramework
import com.fuzamei.commonlib.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author zhengjy
 * @since 2019/09/29
 * Description:
 */
@SuppressLint("SimpleDateFormat")
fun Long.parseTime(): String {
    val now = Calendar.getInstance()
    now.time = Date(System.currentTimeMillis())
    val that = Calendar.getInstance()
    that.time = Date(this)
    return when {
        now.isToday(that) -> {
            SimpleDateFormat("HH:mm").format(this)
        }
        now.isYestYesterday(that) -> {
            val sdf = SimpleDateFormat("HH:mm")
            FzmFramework.getString(R.string.basic_yesterday_time, sdf.format(this))
        }
        now.lessThanAWeek(that) -> {
            val sdf = SimpleDateFormat("HH:mm")
            val weekDays = FzmFramework.context.resources.getStringArray(R.array.basic_week_days)
            val dayOfWeek = when (that.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> weekDays[6]
                Calendar.MONDAY -> weekDays[0]
                Calendar.TUESDAY -> weekDays[1]
                Calendar.WEDNESDAY -> weekDays[2]
                Calendar.THURSDAY -> weekDays[3]
                Calendar.FRIDAY -> weekDays[4]
                Calendar.SATURDAY -> weekDays[5]
                else -> ""
            }
            "$dayOfWeek ${sdf.format(this)}"
        }
        now.isThisYear(that) -> {
            SimpleDateFormat(FzmFramework.getString(R.string.basic_this_year_time)).format(this)
        }
        else -> {
            SimpleDateFormat(FzmFramework.getString(R.string.basic_year_time)).format(this)
        }
    }
}

fun Calendar.isToday(date: Calendar): Boolean {
    return isThisMonth(date) && this.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
}

fun Calendar.isYestYesterday(date: Calendar): Boolean {
    return isThisMonth(date) && this.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH) + 1
}

fun Calendar.lessThanAWeek(date: Calendar): Boolean {
    return isThisYear(date) && this.get(Calendar.DAY_OF_YEAR) < date.get(Calendar.DAY_OF_YEAR) + 7
}

fun Calendar.isThisWeek(date: Calendar): Boolean {
    return isThisYear(date) && this.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR)
}

fun Calendar.isThisMonth(date: Calendar): Boolean {
    return isThisYear(date) && this.get(Calendar.MONTH) == date.get(Calendar.MONTH)
}

fun Calendar.isThisYear(date: Calendar): Boolean {
    return this.get(Calendar.YEAR) == date.get(Calendar.YEAR)
}

/**
 * 判断当前时间是否在某个时间段内，时间段不能超过24小时
 *
 * @param beginHour 开始小时数
 * @param beginMin  开始分钟数
 * @param endHour   结束小时数
 * @param endMin    结束分钟数
 */
fun isCurrentInTimeScope(beginHour: Int, beginMin: Int, endHour: Int, endMin: Int): Boolean {
    val currentTimeMillis = System.currentTimeMillis()
    val now = Calendar.getInstance().apply {
        time = Date(currentTimeMillis)
    }
    val startTime = Calendar.getInstance().apply {
        time = Date(currentTimeMillis)
        set(Calendar.HOUR_OF_DAY, beginHour)
        set(Calendar.MINUTE, beginMin)
    }
    val endTime = Calendar.getInstance().apply {
        time = Date(currentTimeMillis)
        set(Calendar.HOUR_OF_DAY, endHour)
        set(Calendar.MINUTE, endMin)
    }
    return if (!startTime.before(endTime)) {
        // 跨天的特殊情况（比如22:00-8:00）
        val trueEndTime = Calendar.getInstance().apply {
            time = Date(endTime.timeInMillis + 1000 * 60 * 60 * 24.toLong())
        }
        now.after(startTime) && now.before(trueEndTime)
    } else {
        // 普通情况(比如 8:00 - 14:00)
        now.after(startTime) && now.before(endTime)
    }
}

@SuppressLint("SimpleDateFormat")
fun Long.format(format: String): String {
    return SimpleDateFormat(format).format(this)
}