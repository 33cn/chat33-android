package com.fzm.chat33.core.net

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.annotation.IntDef

import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider
import com.alibaba.sdk.android.oss.common.utils.OSSUtils
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.config.AppPreference
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.bean.param.EncryptParams
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.manager.FileEncryption
import kotlinx.coroutines.*

import java.text.SimpleDateFormat

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:阿里云Oss上传
 */
class OssModel {

    private val oss: OSS?
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var folder = "chatList/picture/"

    init {
        val provider = object : OSSCustomSignerCredentialProvider() {
            override fun signContent(s: String): String {
                return OSSUtils.sign(AppConfig.ALIYUN_OSS_ACCESS_KEY, AppConfig.ALIYUN_OSS_SECRET_KEY, s)
            }
        }
        val conf = ClientConfiguration()
        conf.connectionTimeout = 15_000     // 连接超时，默认15秒
        conf.socketTimeout = 15_000         // socket超时，默认15秒
        conf.maxConcurrentRequest = 5       // 最大并发请求书，默认5个
        conf.maxErrorRetry = 2              // 失败后最大重试次数，默认2次
        oss = OSSClient(Chat33.getContext(), AppConfig.ALIYUN_OSS_END_POINT, provider, conf)
    }

    fun uploadMedia(params: EncryptParams?, path: String, @MediaType type: Int, uploadCallback: UpLoadCallBack?) = GlobalScope.launch(Dispatchers.Main) {
        if (TextUtils.isEmpty(path) || oss == null || path.contains(AppConfig.ENC_PREFIX)) {
            uploadCallback?.onFailure(path)
            return@launch
        }
        folder = when (type) {
            PICTURE -> "chatList/picture/"
            VOICE -> "chatList/voice/"
            FILE -> "chatList/file/"
            VIDEO -> "chatList/video/"
            else -> "chatList/picture/"
        }
        val put = if (!AppConfig.APP_ENCRYPT || params == null) {
            PutObjectRequest(AppConfig.ALIYUN_OSS_BUCKET, getObjectKey(path), path)
        } else {
            val byteData = FileEncryption.encrypt(params, path)
            if (byteData != null) {
                PutObjectRequest(AppConfig.ALIYUN_OSS_BUCKET, getObjectKey(path), byteData)
            } else {
                PutObjectRequest(AppConfig.ALIYUN_OSS_BUCKET, getObjectKey(path), path)
            }
        }
        put.progressCallback = OSSProgressCallback { _, currentSize, totalSize ->
            handler.post { uploadCallback?.onProgress(currentSize, totalSize) }
        }
        oss.asyncPutObject(put, object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
            override fun onSuccess(request: PutObjectRequest, result: PutObjectResult) {
                val objectKey = request.objectKey
                val mOssImgUrl = oss.presignPublicObjectURL(AppConfig.ALIYUN_OSS_BUCKET, objectKey)
                handler.post { uploadCallback?.onSuccess(mOssImgUrl) }
            }

            override fun onFailure(request: PutObjectRequest, clientExcepion: ClientException?, serviceException: ServiceException?) {
                handler.post { uploadCallback?.onFailure(path) }
            }
        })
    }

    suspend fun uploadMedia(params: EncryptParams, path: String, @MediaType type: Int):String? {
        if (TextUtils.isEmpty(path) || oss == null || path.contains(AppConfig.ENC_PREFIX)) {
            return null
        }
        folder = when (type) {
            PICTURE -> "chatList/picture/"
            VOICE -> "chatList/voice/"
            FILE -> "chatList/file/"
            VIDEO -> "chatList/video/"
            else -> "chatList/picture/"
        }
        val objectKey = getObjectKey(path)
        val put = if (!AppConfig.APP_ENCRYPT) {
            PutObjectRequest(AppConfig.ALIYUN_OSS_BUCKET, objectKey, path)
        } else {
            val byteData = FileEncryption.encrypt(params, path)
            if (byteData != null) {
                PutObjectRequest(AppConfig.ALIYUN_OSS_BUCKET, objectKey, byteData)
            } else {
                PutObjectRequest(AppConfig.ALIYUN_OSS_BUCKET, objectKey, path)
            }
        }
        oss.putObject(put)
        return oss.presignPublicObjectURL(AppConfig.ALIYUN_OSS_BUCKET, objectKey)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getObjectKey(path: String): String {
        val timeL = System.currentTimeMillis()
        val format = SimpleDateFormat("yyyyMMdd")
        val time = format.format(timeL)
        val format1 = SimpleDateFormat("HHmm")
        val time1 = format1.format(timeL)
        val format2 = SimpleDateFormat("ssSSS")
        val time2 = format2.format(timeL)
        val objectKey = StringBuilder("")
        try {
            objectKey.append(folder)
            objectKey.append(time)
            objectKey.append("/")
            if (AppConfig.FILE_ENCRYPT) {
                objectKey.append(AppConfig.ENC_PREFIX)
            }
            objectKey.append(time)
            objectKey.append(time1)
            objectKey.append(time2)
            objectKey.append("_")
            objectKey.append(AppPreference.USER_ID)
            val suffix = path.substring(path.lastIndexOf(".") + 1)
            objectKey.append(".")
            objectKey.append(suffix)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return objectKey.toString()
    }

    interface UpLoadCallBack {

        fun onSuccess(url: String)

        fun onProgress(currentSize: Long, totalSize: Long)

        fun onFailure(path: String)
    }

    @IntDef(PICTURE, VOICE, FILE, VIDEO)
    @Retention(AnnotationRetention.SOURCE)
    annotation class MediaType

    companion object {

        const val PICTURE = ChatMessage.Type.IMAGE

        const val VOICE = ChatMessage.Type.AUDIO

        const val FILE = ChatMessage.Type.FILE

        const val VIDEO = ChatMessage.Type.VIDEO

        @JvmStatic
        fun getInstance(): OssModel {
            return OssModelHolder.OSS_MODEL
        }
    }

    private object OssModelHolder {
        val OSS_MODEL: OssModel = OssModel()
    }
}
