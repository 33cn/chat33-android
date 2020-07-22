package com.fzm.chat33.hepler.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.bean.param.toDecParams
import com.fzm.chat33.core.manager.DownloadManager
import com.fzm.chat33.core.manager.FileEncryption
import com.fzm.chat33.core.manager.toByteArray
import kotlinx.coroutines.*
import java.io.*
import java.lang.RuntimeException
import java.security.MessageDigest

/**
 * @author zhengjy
 * @since 2019/12/18
 * Description:
 */
data class SingleKeyEncrypt(
        val data: String,
        val key: String
) : Serializable

class ChatEncryptLoader2 : ModelLoader<SingleKeyEncrypt, InputStream> {

    override fun buildLoadData(model: SingleKeyEncrypt, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(ObjectKey(model), ChatDataFetcher(model))
    }

    override fun handles(s: SingleKeyEncrypt): Boolean {
        return true
    }

    /**
     * 文件唯一ID
     */
    class ObjectKey(internal var encrypt: SingleKeyEncrypt) : Key {

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(encrypt.data.toByteArray(Key.CHARSET))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ObjectKey
            if (encrypt != other.encrypt) return false
            return true
        }

        override fun hashCode(): Int {
            return encrypt.hashCode()
        }

    }

    class ChatDataFetcher(private val encrypt: SingleKeyEncrypt) : DataFetcher<InputStream> {
        private var isCanceled: Boolean = false
        var mInputStream: InputStream? = null
        private var job: Job? = null

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
            job = GlobalScope.launch(Dispatchers.Main) {
                try {
                    if (!isCanceled) {
                        val file = if (encrypt.data.startsWith("http")) {
                            withContext(Dispatchers.IO) {
                                DownloadManager.downloadTemp(encrypt.data)
                            }
                        } else {
                            File(encrypt.data)
                        }
                        if (file == null) {
                            callback.onLoadFailed(RuntimeException("picture download fail"))
                            return@launch
                        }
                        mInputStream = if (AppConfig.FILE_ENCRYPT && file.path.contains(AppConfig.ENC_PREFIX)) {
                            // 加密文件先解密
                            ByteArrayInputStream(FileEncryption.decrypt(encrypt.key.toDecParams(), File(file.path).toByteArray()))
                        } else {
                            FileInputStream(file.path)
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
            return DataSource.MEMORY_CACHE
        }
    }

    class LoaderFactory : ModelLoaderFactory<SingleKeyEncrypt, InputStream> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<SingleKeyEncrypt, InputStream> {
            return ChatEncryptLoader2()
        }

        override fun teardown() {

        }
    }
}
