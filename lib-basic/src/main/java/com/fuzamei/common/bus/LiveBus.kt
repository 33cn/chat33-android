package com.fuzamei.common.bus

import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap


/**
 * @author zhengjy
 * @since 2019/09/30
 * Description:基于LiveData的事件总线
 */
class LiveBus {

    private val liveData by lazy { ConcurrentHashMap<String, ChatEventData<*>>() }

    private fun <T> bus(channel: String): ChatEventData<T> {
        return liveData.getOrPut(channel) {
            ChatEventData<T>()
        } as ChatEventData<T>
    }

    private fun <T> with(channel: String): ChatEventData<T> {
        return bus(channel)
    }

    fun <T> with(clazz: Class<T>): ChatEventData<T> {
        return bus(clazz.canonicalName ?: DEFAULT_KEY)
    }

    companion object {

        private const val DEFAULT_KEY = "DEFAULT_KEY"

        private val instance by lazy { LiveBus() }

        @JvmStatic
        fun get(): LiveBus {
            return instance
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <E> of(clazz: Class<E>): E {
            require(clazz.isInterface) {
                "API declarations must be interfaces."
            }
            require(clazz.interfaces.isEmpty()) {
                "API interfaces must not extend other interfaces."
            }
            return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)) { _, method, _ ->
                val returnType = method.genericReturnType
                require(returnType is ParameterizedType) {
                    "returnType must be ParameterizedType.eg(ChatEventData<String>)"
                }
                require(returnType.actualTypeArguments.size == 1) {
                    "returnType must have one parameterized type.eg(ChatEventData<String>)"
                }
                val type = TypeToken.get(returnType)
                type.rawType.cast(get().with<Any>("${clazz.canonicalName}_${method.name}"))
            } as E
        }
    }
}