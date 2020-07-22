package com.fzm.chat33.widget

import android.content.Context
import android.text.Html
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.util.AttributeSet
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.dp2px
import kotlin.math.max
import kotlin.math.min

/**
 * @author zhengjy
 * @since 2019/09/23
 * Description:全文搜索结果高亮TextView
 */
class FtsHighlightTextView constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
) : HighlightTextView(context, attrs, defStyleAttr) {

    private var originText: CharSequence? = ""

    companion object {
        const val START_MATCH = "<\u200b>"
        const val END_MATCH = "</\u200b>"
        const val ELLIPSES = "…"
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, android.R.attr.textViewStyle)

    private fun getLineNumber(text: CharSequence?, width: Int): Int {
        if (text.isNullOrEmpty()) {
            return 0
        }
        val maxWidth = if (width == 0) {
            // 布局文件固定，因此知道TextView的最大宽度
            resources.displayMetrics.widthPixels - dp2px(75f)
        } else {
            width
        }
        val staticLayout = StaticLayout(text, paint, maxWidth, Layout.Alignment.ALIGN_NORMAL
                , 1.0f, 0f, false)
        return staticLayout.getLineEnd(0)
    }

    private fun getKeywordsStart(text: CharSequence): Int {
        return text.indexOf(START_MATCH)
    }

    private fun getKeywordsEnd(text: CharSequence): Int {
        return text.indexOf(END_MATCH)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        originText = text
        super.setText(processText(text, customWidth), type)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        text = originText
    }

    private fun processText(text: CharSequence?, width: Int): CharSequence? {
        if (text.isNullOrEmpty()) {
            return text
        }
        var start = getKeywordsStart(text)
        var end = getKeywordsEnd(text) - START_MATCH.length
        if (start < 0 || end < 0) {
            return text
        }
        // 将字符串转为Spanned
        var result: SpannableStringBuilder = Html.fromHtml((text as String)
                .replace(START_MATCH, "<font color=\"" + AppConfig.APP_ACCENT_COLOR_STR + "\">")
                .replace(END_MATCH, "</font>")) as SpannableStringBuilder
        val maxNum = getLineNumber(result, width)
        if (maxNum >= result.length) {
            // 如果result长度不超过最大字数，则直接显示
            return result
        }
        out@ while (start > 0 || end < result.length) {
            start = max(0, start)
            end = min(end, result.length)
            while (end - start > maxNum) {
                when {
                    start == 0 -> end--
                    end == result.length -> start++
                    else -> {
                        start++
                        end--
                    }
                }
                break@out
            }
            start--
            end++
        }
        val startOver = start > 0
        val endOver = result.length > end
        if (startOver && endOver) {
            result = result.subSequence(start + ELLIPSES.length, end - ELLIPSES.length) as SpannableStringBuilder
            result.insert(0, ELLIPSES)
            result.append(ELLIPSES)
        } else if (startOver) {
            result = result.subSequence(start + ELLIPSES.length, end) as SpannableStringBuilder
            result.insert(0, ELLIPSES)
        } else if (endOver) {
            result = result.subSequence(0, end - ELLIPSES.length) as SpannableStringBuilder
            result.append(ELLIPSES)
        }
        return result
    }
}
