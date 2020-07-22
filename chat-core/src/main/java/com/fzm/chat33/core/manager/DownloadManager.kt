package com.fzm.chat33.core.manager

import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.Chat33
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.kodein.di.conf.KodeinGlobalAware
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

/**
 * @author zhengjy
 * @since 2019/12/19
 * Description:
 */
object DownloadManager : KodeinGlobalAware {

    private val client: OkHttpClient by instance(tag = "downloadClient")
    private val TEMP_FILE_SUFFIX = ".temp"

    fun downloadTemp(url: String): File? {
        val folder = File("${Chat33.getContext().filesDir}/temp")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val request = Request.Builder().get().url(url).build()
        val response = client.newCall(request).execute()
        return if (response.isSuccessful) {
            saveFile(response, folder, "${AppConfig.ENC_PREFIX}${UUID.randomUUID()}${getExtension(url)}")
        } else {
            null
        }
    }

    private fun saveFile(response: Response, folder: File, fileName: String): File? {
        if (response.body() == null) {
            return null
        }
        val file = File(folder, fileName + TEMP_FILE_SUFFIX)
        var input: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            input = response.body()!!.byteStream()
            fos = FileOutputStream(file)
            val buf = ByteArray(2048)
            var len: Int
            while (input.read(buf).also { len = it } != -1) {
                fos.write(buf, 0, len)
            }
            val dest = File(folder, fileName)
            file.renameTo(dest)
            fos.flush()
            return dest
        } catch (e: Exception) {
            return null
        } finally {
            response.body()?.close()
            input?.close()
            fos?.close()
        }
    }
}