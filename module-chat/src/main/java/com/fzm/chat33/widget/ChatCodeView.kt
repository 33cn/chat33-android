package com.fzm.chat33.widget

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.InputFilter
import android.view.KeyEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.common.utils.ScreenUtils
import com.fzm.chat33.R
import com.fzm.chat33.utils.SimpleTextWatcher
import java.lang.StringBuilder

/**
 * @author zhengjy
 * @since 2019/03/12
 * Description:
 */
class ChatCodeView(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    val NUMBER = 1
    val PASSWORD = 2

    private var inputCount: Int
    @ColorInt
    private var textColor: Int
    private var textSize: Float
    private var itemSize: Float
    private var inputType: Int

    private var editText: EditText? = null
    private val textViewList = mutableListOf<TextView>()

    private var listener: OnCodeCompleteListener? = null
    private var disable: Boolean = false

    private var content: StringBuilder = StringBuilder()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatCodeView, defStyleAttr, 0)
        inputCount = typedArray.getInt(R.styleable.ChatCodeView_input_count, 4)
        textColor = typedArray.getColor(R.styleable.ChatCodeView_text_color, ContextCompat.getColor(context, R.color.chat_text_grey_dark))
        textSize = typedArray.getDimension(R.styleable.ChatCodeView_text_size, 18f)
        itemSize = typedArray.getDimension(R.styleable.ChatCodeView_item_size, 50f)
        inputType = typedArray.getInt(R.styleable.ChatCodeView_input_type, NUMBER)
        typedArray.recycle()
        initView()
    }

    private fun initView() {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        for (i in 0 until inputCount) {
            // 创建一个输入框
            val relativeLayout = RelativeLayout(context)
            val relativeLayoutParams = ViewGroup.LayoutParams(ScreenUtils.dp2px(context, itemSize), ScreenUtils.dp2px(context, itemSize))
            relativeLayout.gravity = Gravity.CENTER
            relativeLayout.background = ContextCompat.getDrawable(context, R.mipmap.bg_code_box)
            relativeLayout.layoutParams = relativeLayoutParams
            val textView = TextView(context)
            textView.setTextColor(textColor)
            textView.textSize = textSize
            textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            textView.gravity = Gravity.CENTER
            // 加入TextView动态数组
            textViewList.add(textView)

            val layoutParamsTextView = LinearLayout.LayoutParams(ScreenUtils.dp2px(context, itemSize), ScreenUtils.dp2px(context, itemSize))
            relativeLayout.addView(textView, layoutParamsTextView)

            val layoutParamsLinearLayout = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (i != 0) {
                layoutParamsLinearLayout.leftMargin = ScreenUtils.dp2px(context, -19f)
            }
            linearLayout.addView(relativeLayout, layoutParamsLinearLayout)
        }
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        this.addView(linearLayout, params)
        editText = EditText(context)
        editText?.includeFontPadding = false
        editText?.background = null
        editText?.isCursorVisible = false
        editText?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
        editText?.isFocusable = true
        editText?.isFocusableInTouchMode = true
        editText?.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (disable) {
                    return
                }
                if (content.length < inputCount && s?.length == 1) {
                    content.append(s)
                    if (PASSWORD == inputType) {
                        textViewList[content.length - 1].text = "●"
                    } else {
                        textViewList[content.length - 1].text = s
                    }
                    if (isCompleteText()) {
                        listener?.onCodeComplete(editText, content.toString())
                    }
                }
                disable = true
                editText?.setText("")
                disable = false
            }
        })
        editText?.setOnKeyListener(OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
                if (content.isEmpty()) {
                    return@OnKeyListener true
                }
                textViewList[content.length - 1].text = ""
                content.delete(content.length - 1, content.length)
                return@OnKeyListener true
            }
            false
        })
        editText?.inputType = InputType.TYPE_CLASS_NUMBER
        this.addView(editText, params)
        setOnClickListener {
            KeyboardUtils.showKeyboard(editText)
        }
    }

    fun clear() {
        content.clear()
        editText?.setText("")
        for (tv in textViewList) {
            tv.text = ""
        }
    }

    fun isCompleteText(): Boolean {
        return content.length == inputCount
    }

    fun focus(): View {
        return editText!!
    }

    fun setOnCodeCompleteListener(listener: OnCodeCompleteListener) {
        this.listener = listener
    }

    interface OnCodeCompleteListener {
        fun onCodeComplete(view: View?, code: String)
    }
}