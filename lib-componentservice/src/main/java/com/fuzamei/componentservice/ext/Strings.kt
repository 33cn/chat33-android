package com.fuzamei.componentservice.ext

/**
 * @author zhengjy
 * @since 2019/12/13
 * Description:
 */

/**
 * 将多行文字合并为一行，行之间用一个空格隔开
 */
fun String?.flatMapLines(): String {
    val sb = StringBuilder()
    this?.lines()?.forEachIndexed { index, s ->
        sb.append(s)
        if (index != lastIndex) {
            sb.append(" ")
        }
    }.toString()
    return sb.toString()
}