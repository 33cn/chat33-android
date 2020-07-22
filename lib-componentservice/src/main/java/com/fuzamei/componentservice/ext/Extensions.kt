package com.fuzamei.componentservice.ext

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*

/**
 * @author zhengjy
 * @since 2019/09/12
 * Description:
 */
/**
 * 通过[ViewModelProvider.Factory]构建[ViewModel]
 */
inline fun <reified VM : ViewModel> FragmentActivity.findViewModel(
        provider: ViewModelProvider.Factory
) = ViewModelProviders.of(this, provider).get(VM::class.java)

inline fun <reified VM : ViewModel> FragmentActivity.findViewModel()
        = ViewModelProviders.of(this).get(VM::class.java)

inline fun <reified VM : ViewModel> Fragment.findViewModel(
        provider: ViewModelProvider.Factory
) = requireActivity().findViewModel<VM>(provider)

inline fun <reified VM : ViewModel> Fragment.findViewModel()
        = requireActivity().findViewModel<VM>()

/**
 * 防止相同数据重复更新
 */
fun <T> LiveData<T>.getDistinct(): LiveData<T> {
    val distinctLiveData = object : MediatorLiveData<T>() {
        override fun getValue(): T? {
            // getValue获取的是数据源source的value
            return this@getDistinct.value
        }
    }
    distinctLiveData.addSource(this, object : Observer<T> {
        private var initialized = false
        private var lastObj: T? = null
        override fun onChanged(obj: T?) {
            if (!initialized) {
                initialized = true
                lastObj = obj
                distinctLiveData.value = lastObj
            } else if (obj?.equals(lastObj) != true) {
                lastObj = obj
                distinctLiveData.value = lastObj
            }
        }
    })
    return distinctLiveData
}

fun Context.dp2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun View.dp2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun Context.sp2px(spValue: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}

fun View.sp2px(spValue: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}