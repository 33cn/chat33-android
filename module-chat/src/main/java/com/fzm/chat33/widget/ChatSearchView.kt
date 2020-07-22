package com.fzm.chat33.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.transition.ChangeBounds
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.fuzamei.componentservice.ext.dp2px
import com.fzm.chat33.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.chat_search_view.view.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:通用搜索View
 */
class ChatSearchView : RelativeLayout {

    private var search: OnTextChangeListener? = null
    private var cancel: OnSearchCancelListener? = null
    private var hintText: String? = null
    private var maxWords: Int = MAX_LENGTH
    private var searchDelay: Long = DEFAULT_DELAY
    private var enable: Boolean = true
    private var isExpand: Boolean = false

    private val keywords: PublishSubject<String> = PublishSubject.create()

    companion object {
        const val MAX_LENGTH = 30

        const val DEFAULT_DELAY = 200L
    }

    constructor(context: Context) : super(context, null) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ChatSearchView)
        hintText = ta.getString(R.styleable.ChatSearchView_hint)
        maxWords = ta.getInteger(R.styleable.ChatSearchView_maxWords, MAX_LENGTH)
        searchDelay = ta.getInteger(R.styleable.ChatSearchView_searchDelay, DEFAULT_DELAY.toInt()).toLong()
        ta.recycle()

        initView()
    }

    @SuppressLint("CheckResult")
    private fun initView() {
        inflate(context, R.layout.chat_search_view, this)
        if (!hintText.isNullOrEmpty()) {
            et_search.hint = hintText
        }
        tv_cancel.setOnClickListener {
            if (et_search.text.toString().trim().isNotEmpty()) {
                et_search.setText("")
            } else {
                cancel?.onSearchCancel()
            }
        }
        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (enable) {
                    keywords.onNext(s.toString().trim())
                }
            }
        })
        // 搜索延迟需要在布局文件中设置
        keywords.debounce(searchDelay, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    search?.onTextChange(it)
                }, {
                    it.printStackTrace()
                })
    }

    /**
     * 设置伸展状态时的布局
     */
    fun expand() {
        beginDelayedTransition(this)
        val params = layoutParams
        params.width = LayoutParams.MATCH_PARENT
        layoutParams = params
        isExpand = true
    }

    /**
     * 设置收缩状态时的布局
     */
    fun reduce() {
        beginDelayedTransition(this)
        val params = layoutParams
        params.width = dp2px(0f)
        layoutParams = params
        isExpand = false
    }

    private fun beginDelayedTransition(view: ViewGroup) {
        val set = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(Slide(Gravity.END))
            duration = 200
        }
        TransitionManager.beginDelayedTransition(view, set)
    }

    private fun enableTextWatcher(enable: Boolean) {
        this.enable = enable
    }

    /**
     * 用于将EditText暴露给外部来获取焦点
     */
    fun getFocusView(): View {
        return et_search
    }

    fun getText(): String {
        return et_search.text.toString().trim()
    }

    fun setText(text: CharSequence?) {
        et_search.setText(text?.subSequence(0, min(text.length, maxWords)))
        et_search.setSelection(text?.length ?: 0)
    }

    fun setTextWithoutWatcher(text: CharSequence?) {
        enableTextWatcher(false)
        setText(text)
        enableTextWatcher(true)
    }

    fun setHint(text: String?) {
        et_search.hint = text
    }

    fun isExpand(): Boolean {
        return isExpand
    }

    fun onBackPressed(): Boolean {
        if (isExpand()) {
            cancel()
            return true
        }
        return false
    }

    fun cancel() {
        tv_cancel.performClick()
    }

    fun setOnTextChangeListener(listener: OnTextChangeListener) {
        this.search = listener
    }

    fun setOnSearchCancelListener(listener: OnSearchCancelListener) {
        this.cancel = listener
    }

    interface OnTextChangeListener {
        fun onTextChange(s: String)
    }

    interface OnSearchCancelListener {
        fun onSearchCancel()
    }
}
