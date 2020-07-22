package com.fuzamei.common.bus

import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

/**
 * @author zhengjy
 * @since 2019/09/30
 * Description:默认[observe]方法支持粘性事件，通过修改mLastVersion使其默认不支持
 */
class ChatEventData<T> : MediatorLiveData<T>() {

    companion object {
        private const val TAG = "ChatEventData"
    }

    override fun setValue(value: T) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            super.setValue(value)
        } else {
            postValue(value)
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
        try {
            hook(observer)
            Log.d(TAG, "hook succeed")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

   fun observeSticky(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
    }

    @Throws(Exception::class)
    private fun <T> hook(observer: Observer<T>) {
        //get wrapper's version
        val classLiveData = LiveData::class.java
        val fieldObservers = classLiveData.getDeclaredField("mObservers")
        fieldObservers.isAccessible = true
        val objectObservers = fieldObservers.get(this)
        val classObservers = objectObservers.javaClass
        val methodGet = classObservers.getDeclaredMethod("get", Any::class.java)
        methodGet.isAccessible = true
        val objectWrapperEntry = methodGet.invoke(objectObservers, observer)
        var objectWrapper: Any? = null
        if (objectWrapperEntry is Map.Entry<*, *>) {
            objectWrapper = objectWrapperEntry.value
        }
        if (objectWrapper == null) {
            throw NullPointerException("Wrapper can not be bull!")
        }
        val classObserverWrapper = objectWrapper.javaClass.superclass
        val fieldLastVersion = classObserverWrapper?.getDeclaredField("mLastVersion")
        fieldLastVersion?.isAccessible = true
        //get liveData's version
        val fieldVersion = classLiveData.getDeclaredField("mVersion")
        fieldVersion.isAccessible = true
        val objectVersion = fieldVersion.get(this)
        //set wrapper's version
        fieldLastVersion?.set(objectWrapper, objectVersion)
    }
}