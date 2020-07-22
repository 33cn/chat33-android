package com.fzm.chat33.hepler.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DataSource.MEMORY_CACHE
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.bean.param.toDecParams
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.manager.FileEncryption
import com.fzm.chat33.core.manager.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import java.security.MessageDigest

/**
 * @author zhengjy
 * @since 2019/12/18
 * Description:
 */
class ChatEncryptLoader : ModelLoader<ChatMessage, InputStream> {

    override fun buildLoadData(model: ChatMessage, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(ObjectKey(model), ChatDataFetcher(model))
    }

    override fun handles(s: ChatMessage): Boolean {
        return true
    }

    /**
     * 文件唯一ID
     */
    class ObjectKey(internal var message: ChatMessage?) : Key {

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(message!!.logId.toByteArray(Key.CHARSET))
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false

            val myKey = o as ObjectKey?
            return message == myKey!!.message
        }

        override fun hashCode(): Int {
            return if (message != null) message!!.hashCode() else 0
        }
    }

    class ChatDataFetcher(private val message: ChatMessage) : DataFetcher<InputStream> {
        private var isCanceled: Boolean = false
        var mInputStream: InputStream? = null
        private var job: Job? = null

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
            job = GlobalScope.launch(Dispatchers.Main) {
                try {
                    if (!isCanceled) {
                        mInputStream = if (message.briefPos == 0) {
                            if (AppConfig.FILE_ENCRYPT && message.msg.localPath.contains(AppConfig.ENC_PREFIX)) {
                                // 加密文件先解密
                                ByteArrayInputStream(FileEncryption.decrypt(message.toDecParams(), File(message.msg.localPath).toByteArray()))
                            } else {
                                FileInputStream(message.msg.localPath)
                            }
                        } else {
                            val chatLog = message.msg.sourceLog[message.briefPos - 1]
                            if (AppConfig.FILE_ENCRYPT && chatLog.msg.localPath.contains(AppConfig.ENC_PREFIX)) {
                                // 加密文件先解密
                                ByteArrayInputStream(FileEncryption.decrypt(message.toDecParams(), File(chatLog.msg.localPath).toByteArray()))
                            } else {
                                FileInputStream(chatLog.msg.localPath)
                            }
                        }
                    }
                    if (isCanceled) {
                        return@launch
                    }
                    callback.onDataReady(mInputStream)
                } catch (e: Exception) {
                    callback.onLoadFailed(e)
                }
            }
        }

        override fun cleanup() {
            try {
                mInputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun cancel() {
            isCanceled = true
            job?.cancel()
        }

        override fun getDataClass(): Class<InputStream> {
            return InputStream::class.java
        }

        override fun getDataSource(): DataSource {
            return MEMORY_CACHE
        }
    }

    class LoaderFactory : ModelLoaderFactory<ChatMessage, InputStream> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ChatMessage, InputStream> {
            return ChatEncryptLoader()
        }

        override fun teardown() {

        }
    }
}
