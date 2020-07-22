package com.fzm.chat33.widget

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.fzm.chat33.R
import com.fzm.chat33.utils.HighlightUtils
import kotlin.math.max
import kotlin.math.min

open class HighlightTextView constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var customHighlightColor: Int
    protected var customWidth: Int = 0
    private var content: String? = null
    private var keyword: String? = null
    private var needHighlight = false
    private var ellipsis = "..."

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, android.R.attr.textViewStyle)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HighlightTextView, defStyleAttr, 0)
        customHighlightColor = typedArray.getColor(R.styleable.HighlightTextView_highlightColor, ContextCompat.getColor(context, R.color.chat_color_accent))
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        customWidth = MeasureSpec.getSize(widthMeasureSpec)
        if(needHighlight) highlightSearchText(this.content, this.keyword)
    }

    fun highlightSearchText(content: String?, keyword: String?) {
        this.needHighlight = true
        this.content = content
        this.keyword = keyword
        if(TextUtils.isEmpty(keyword) || TextUtils.isEmpty(content)) {
            this.text = content
            return
        }
        var highlight = HighlightUtils.matchKeyWordHighlight(content, keyword)
        if(highlight?.isEffect() == true) {
            var maxLengthStart = getCutStartByMaxLength(content!!, highlight)
            var maxWidthStart = getCutStartByWidth(maxWidth, content, highlight)
            var customWidthStart = getCutStartByWidth(customWidth, content, highlight)
            var start = max(max(maxLengthStart, maxWidthStart), customWidthStart)
            var cutContent: String = content
            if(start > 0) {
                cutContent = ellipsis+ content.substring(start + 1)
                highlight.start -= (start - 2)
                highlight.end -= (start - 2)
            }
            var spannable = SpannableStringBuilder(cutContent)
            if(highlight.end <= cutContent.length) {
                spannable.setSpan(ForegroundColorSpan(customHighlightColor), highlight.start, highlight.end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
            this.text = spannable
        } else {
            this.text = content
        }
    }

    private fun getCutStartByWidth(availableWidth: Int, content: String, highlight: HighlightUtils.Highlight): Int {
        if(availableWidth <= 0) return 0
        var boundsRect = Rect()
        this.paint.getTextBounds(content, 0, content.length, boundsRect)
        if(boundsRect.width() <= availableWidth) return 0

        this.paint.getTextBounds(ellipsis, 0, ellipsis.length, boundsRect)
        var availableEllipsisWidth = availableWidth - boundsRect.width()
        var start = highlight.start
        var end = highlight.end
        while (start > 0) {
            end = min(end, content.length)
            var startContent = content.substring(start, end)
            this.paint.getTextBounds(startContent, 0, startContent.length, boundsRect)
            var textWidth = boundsRect.width()
            if(textWidth > availableEllipsisWidth) {
                break
            }
            start --
            end ++
        }
        return start
    }

    private fun getCutStartByMaxLength(content: String, highlight: HighlightUtils.Highlight) : Int {
        var maxLength = -1
        for (filter in filters) {
            if (filter is InputFilter.LengthFilter) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    maxLength = filter.max
                }
                break
            }
        }
        if(maxLength <= 0 || content.length <= maxLength) {
            return 0
        }
        var start = highlight.start
        var end = highlight.end
        while (start > 0) {
            end = min(end, content.length)
            var startContent = content.substring(start, end)
            if(startContent.length > maxLength) {
                break
            }
            start --
            end ++
        }
        return start
    }
}