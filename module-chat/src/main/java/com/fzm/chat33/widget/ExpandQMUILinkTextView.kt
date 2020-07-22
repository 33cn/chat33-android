package com.fzm.chat33.widget

import android.content.Context
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.style.ImageSpan
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.fzm.chat33.R
import com.qmuiteam.qmui.widget.textview.QMUILinkTextView

/**
 * 创建日期：2019/11/21
 * 描述:
 * 作者:yll
 */
class ExpandQMUILinkTextView(context: Context, val attrs: AttributeSet?) : QMUILinkTextView(context, attrs){

    /** 最多展示的行数 */
    private var maxLineCount = 3
    /** 省略文字 */
    private var ellipsizeText = "…"
    private var mText : String? = null


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int) : this(context, attributes)

    private var mesured = false

    private var isExpand = false
    var isNeedExpandFun = false

    fun expandText() {
        isExpand = !isExpand
        text = mText
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!mesured) {
            mesured = true
            text = mText
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        mText = text?.toString()
        if(!isNeedExpandFun || text.isNullOrEmpty() || measuredWidth <= 0) {
            super.setText(text, type)
            return
        }
        //StaticLayout对象
        val sl = StaticLayout(text, paint, measuredWidth - paddingLeft - paddingRight, Layout.Alignment.ALIGN_CENTER, 1f, 0f, true)
        // 总计行数
        val lineCount = sl.lineCount
        if(lineCount > maxLineCount) {
            if(isExpand) {
                // 收起文案和源文字组成的新的文字
                val newEndLineText = "$text"
                //收起文案和源文字组成的新的文字
                val spannableString = SpannableString("$newEndLineText ")
                val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_text_expanded)
                drawable!!.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                val span = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                spannableString.setSpan(span, newEndLineText.length, newEndLineText.length + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                super.setText(spannableString, type)
            } else {
                // 省略文字和展开文案的宽度
                val dotWidth = paint.measureText(ellipsizeText) + 30
                // 找出显示最后一行的文字
                val start = sl.getLineStart(maxLineCount - 1)
                val end = sl.getLineEnd(maxLineCount - 1)
                val lineText = text.substring(start, end)
                // 将第最后一行最后的文字替换为 ellipsizeText和expandText
                var endIndex = 0
                for (i in lineText.length - 1 downTo 0) {
                    val str = lineText.substring(i, lineText.length)
                    // 找出文字宽度大于 ellipsizeText 的字符
                    if (paint.measureText(str) >= dotWidth) {
                        endIndex = i
                        break
                    }
                }
                // 新的文字
                val newEndLineText = text.substring(0, start) + lineText.substring(0, endIndex) + ellipsizeText
                //全部文字
                val spannableString = SpannableString("$newEndLineText ")
                val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_text_expand)
                drawable!!.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                val span = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                spannableString.setSpan(span, newEndLineText.length, newEndLineText.length + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                super.setText(spannableString, type)
            }
        }
    }
}