package com.fzm.chat33.core.manager

import walletapi.Walletapi
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.bean.param.DecryptParams
import com.fzm.chat33.core.bean.param.EncryptParams
import com.fzm.chat33.core.global.Chat33Const
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*

/**
 * @author zhengjy
 * @since 2019/12/18
 * Description:
 */
object FileEncryption {

    /**
     * 用DH密钥对加密文件
     *
     * @param privateKey    自己的私钥
     * @param publicKey     对方的公钥
     * @param filePath      文件路径
     */
    @JvmStatic
    fun encryptFileDH(privateKey: String, publicKey: String, filePath: String): ByteArray? {
        var byteData: ByteArray? = null
        return try {
            byteData = File(filePath).toByteArray()
            Walletapi.encryptWithDHKeyPair(privateKey, publicKey, byteData)
        } catch (e: Exception) {
            byteData
        }
    }

    /**
     * 用DH密钥对解密文件
     *
     * @param privateKey    自己的私钥
     * @param publicKey     对方的公钥
     * @param byteData      文件byte数组
     */
    @JvmStatic
    fun decryptFileDH(privateKey: String, publicKey: String, byteData: ByteArray?): ByteArray? {
        return try {
            Walletapi.decryptWithDHKeyPair(privateKey, publicKey, byteData)
        } catch (e: Exception) {
            byteData
        }
    }

    /**
     * 用密钥直接对称加密文件
     *
     * @param key       对称密钥
     * @param filePath  文件路径
     */
    @JvmStatic
    fun encryptFile(key: String, filePath: String): ByteArray? {
        var byteData: ByteArray? = null
        return try {
            byteData = File(filePath).toByteArray()
            Walletapi.encryptSymmetric(key, byteData)
        } catch (e: Exception) {
            byteData
        }
    }

    /**
     * 用密钥直接对称解密文件
     *
     * @param key       对称密钥
     * @param byteData  文件byte数组
     */
    @JvmStatic
    fun decryptFile(key: String, byteData: ByteArray?): ByteArray? {
        return try {
            Walletapi.decryptSymmetric(key, byteData)
        } catch (e: Exception) {
            byteData
        }
    }

    @JvmStatic
    suspend fun encrypt(params: EncryptParams?, filePath: String): ByteArray? {
        if (params == null) {
            return null
        }
        return withContext(Dispatchers.IO) {
            if (params.channelType == Chat33Const.CHANNEL_ROOM) {
                encryptFile(params.groupKey, filePath)
            } else {
                if (params.friendKey.isNotEmpty() && CipherManager.getPrivateKey().isNotEmpty()) {
                    encryptFileDH(CipherManager.getPrivateKey(), params.friendKey, filePath)
                } else {
                    null
                }
            }
        }
    }

    @JvmStatic
    fun encryptSync(params: EncryptParams?, filePath: String): ByteArray? {
        if (params == null) {
            return null
        }
        return if (params.channelType == Chat33Const.CHANNEL_ROOM) {
            encryptFile(params.groupKey, filePath)
        } else {
            if (params.friendKey.isNotEmpty() && CipherManager.getPrivateKey().isNotEmpty()) {
                encryptFileDH(CipherManager.getPrivateKey(), params.friendKey, filePath)
            } else {
                null
            }
        }
    }

    @JvmStatic
    suspend fun decrypt(params: DecryptParams?, byteData: ByteArray?): ByteArray? {
        if (params == null) {
            return null
        }
        return withContext(Dispatchers.IO) {
            if (params.channelType == Chat33Const.CHANNEL_ROOM) {
                decryptFile(params.groupKey, byteData)
            } else {
                if (params.friendKey.isNotEmpty() && CipherManager.getPrivateKey().isNotEmpty()) {
                    decryptFileDH(CipherManager.getPrivateKey(), params.friendKey, byteData)
                } else {
                    byteData
                }
            }
        }
    }

    @JvmStatic
    fun decryptSync(params: DecryptParams?, byteData: ByteArray?): ByteArray? {
        if (params == null) {
            return null
        }
        return if (params.channelType == Chat33Const.CHANNEL_ROOM) {
            decryptFile(params.groupKey, byteData)
        } else {
            if (params.friendKey.isNotEmpty() && CipherManager.getPrivateKey().isNotEmpty()) {
                decryptFileDH(CipherManager.getPrivateKey(), params.friendKey, byteData)
            } else {
                byteData
            }
        }
    }

    /**
     * 清除App中临时生成的解密文件
     */
    @JvmStatic
    fun clearCache() {
        val path = File("${Chat33.getContext().filesDir}/temp")
        deleteAllFilesOfDir(path)
    }

    private fun deleteAllFilesOfDir(path: File?) {
        if (null != path) {
            if (!path.exists())
                return
            if (path.isFile) {
                var result = path.delete()
                var tryCount = 0
                while (!result && tryCount++ < 10) {
                    System.gc() // 回收资源
                    result = path.delete()
                }
            }
            val files = path.listFiles()
            if (null != files) {
                for (i in files.indices) {
                    deleteAllFilesOfDir(files[i])
                }
            }
            path.delete()
        }
    }
}

/**
 * 将文件转换为字节数组
 */
@Throws(Exception::class)
fun File.toByteArray(): ByteArray {
    return readBytes()
}

/**
 * 将字节数组转换为文件
 *
 * @param folder    文件目录
 * @param fileName  文件名
 */
fun ByteArray.toFile(folder: String, fileName: String?): File? {
    var bos: BufferedOutputStream? = null
    var fos: FileOutputStream? = null
    var file: File? = null
    try {
        val dir = File(folder)
        if (!dir.exists() && !dir.isDirectory) {
            dir.mkdirs()
        }
        if (fileName == null) {
            return null
        }
        file = File(folder, fileName)
        file.createNewFile()
        fos = FileOutputStream(file)
        bos = BufferedOutputStream(fos)
        bos.write(this)
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    } finally {
        try {
            bos?.close()
            fos?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun ByteArray.toCacheFile(path: String): File? {
    return toFile("${Chat33.getContext().filesDir.path}/temp", "${UUID.randomUUID()}${getExtension(path)}")
}

fun getExtension(pathOrUrl: String?): String {
    if (pathOrUrl == null) {
        return ""
    }
    val dotPos = pathOrUrl.lastIndexOf('.')
    return if (0 <= dotPos) {
        ".${pathOrUrl.substring(dotPos + 1).toLowerCase()}"
    } else {
        ""
    }
}