package com.fzm.chat33.utils

import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.utils.PinyinUtils

object HighlightUtils {

    class Highlight {
        var start: Int = -1
        var end: Int = -1

        fun isEffect(): Boolean {
            if(start in 0 until end) return true
            return false
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        print(isMatchKeyword("LuoGuanzhong", "luo"))
    }

    /**
     * 判断字段与搜索字段是否匹配
     */

    @JvmStatic
    fun isMatchKeyword(content: String?, keyword: String?): Boolean {
        if(TextUtils.isEmpty(content) || TextUtils.isEmpty(keyword)) return false
        //字串匹配
        var start = indexOf(content!!, keyword!!)
        if(start >= 0) {
            return true
        }
        if(PinyinUtils.isContainChinese(keyword)) return false
        if(!PinyinUtils.isContainChinese(content)) return false
        //拼音首字母匹配
        var firstSpell = PinyinUtils.getFirstSpell(content)
        start = indexOf(firstSpell, keyword)
        if(start >= 0) {
            return true
        }
        //拼音全拼匹配
        var index: Int = 0
        while (index < content.length) {
            var lastText = content.substring(index)
            var pinyin = PinyinUtils.getPingYin(lastText)
            //从第一个字段开始往后对比，查找出最先匹配位置
            var lastTextPrefix = pinyin.removePrefix(keyword)
            if(lastTextPrefix != pinyin) {
                return true
            }
            index ++
        }
        return false
    }

    /**
     * 找出匹配字段位置
     */
    @JvmStatic
    fun matchKeyWordHighlight(content: String?, keyword: String?): Highlight? {
        if(TextUtils.isEmpty(content) || TextUtils.isEmpty(keyword)) return null
        //字串匹配
        var start = indexOf(content!!, keyword!!)
        if(start >= 0) {
            var highlight = Highlight()
            highlight.start = start
            highlight.end = start + keyword.length
            return highlight
        }
        if(!PinyinUtils.isContainChinese(content)) return null
        //拼音首字母匹配
        var firstSpell = PinyinUtils.getFirstSpell(content)
        start = indexOf(firstSpell, keyword)
        if(start >= 0) {
            var highlight = Highlight()
            highlight.start = start
            highlight.end = start + keyword.length
            return highlight
        }
        //拼音全拼匹配
        var pinyin = PinyinUtils.getPingYin(content)
        start = indexOf(pinyin, keyword)
        if(start >= 0) {
            var highlight = Highlight()
            getMatchHighlight(content, keyword, highlight)
            return highlight
        }
        return null
    }

    private fun indexOf(content: String, keyword: String): Int {
        var content = content.toLowerCase()
        var keyword = keyword.toLowerCase()
        return content.indexOf(keyword, 0)
    }
    /**
     * 获取拼音全匹配字段位置
     */
    private fun getMatchHighlight(displayName: String, keyword: String, highlight: Highlight){
        var index: Int = 0
        last@ while (index < displayName.length) {
            var lastText = displayName.substring(index)
            var pinyin = PinyinUtils.getPingYin(lastText)
            //从第一个字段开始往后对比，查找出最先匹配位置
            if(pinyin.indexOf(keyword, 0) == 0) {
                highlight.start = index
                highlight.end = getMatchEnd(lastText, keyword) + 1 + index
                break@last
            }
            index ++
        }
    }

    /**
     * 根据初始位置后的字串来定位末尾匹配字串
     */
    private fun getMatchEnd(text: String, keyword: String): Int {
        var index: Int = 0
        var lastKeyword = keyword
        end@ while(index < text.length) {
            var charPinyin = PinyinUtils.getCharPingYin(text[index])
            keyword@ for(i in 0 until charPinyin.length) {
                var pinyin = charPinyin[i]
                var lastText = lastKeyword.removePrefix(pinyin.toString())
                if(lastText == lastKeyword) break@keyword
                lastKeyword = lastText
            }
            if(TextUtils.isEmpty(lastKeyword)) {
                return index
            }
            index ++
        }
        return -1
    }
}