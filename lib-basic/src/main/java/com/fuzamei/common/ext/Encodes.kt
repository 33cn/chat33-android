package com.fuzamei.common.ext

import android.util.Base64
import com.fuzamei.common.utils.Base58
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * @author zhengjy
 * @since 2020/02/11
 * Description:
 */
/*
  Base64：（Base64编码算法不算是真正的加密算法。）
  Base64是一种基于64个可打印字符来表示二进制数据的表示方法。
  Base64常用于在通常处理文本数据的场合，表示、传输、存储一些二进制数据。
 */

/**
 * Base64编码
 * https://stackoverflow.com/questions/17912119/is-there-any-difference-between-apaches-base64-encodebase64-and-androids-base6
 *
 * Android 的Base64.DEFAULT 包括行终止符
 * 想要和 Java 统一需要使用Base64.NO_WRAP
 */
fun String.base64Encode2Str(charset: Charset = Charsets.US_ASCII, flags: Int = Base64.NO_WRAP): String = String(Base64.encode(this.toByteArray(), flags), charset)

fun String.base64Encode(flags: Int = Base64.NO_WRAP): ByteArray = Base64.encode(this.toByteArray(), flags)

/**
 * Base64编码
 *
 */
fun ByteArray.base64Encode2Str(charset: Charset = Charsets.US_ASCII, flags: Int = Base64.NO_WRAP): String = String(Base64.encode(this, flags), charset)

fun ByteArray.base64Encode(flags: Int = Base64.NO_WRAP): ByteArray = Base64.encode(this, flags)

/**
 * Base64解码
 *
 */
fun String.base64Decode2Str(charset: Charset = Charsets.US_ASCII, flags: Int = Base64.NO_WRAP): String = String(Base64.decode(this, flags), charset)

fun String.base64Decode(flags: Int = Base64.NO_WRAP): ByteArray = Base64.decode(this, flags)

fun ByteArray.base64Decode2Str(charset: Charset = Charsets.US_ASCII, flags: Int = Base64.NO_WRAP): String = String(Base64.decode(this, flags), charset)

fun ByteArray.base64Decode(flags: Int = Base64.NO_WRAP): ByteArray = Base64.decode(this, flags)

/**
 * Base58编码
 */
fun ByteArray.base58Encode(): ByteArray = Base58.encode(this).toByteArray()
fun ByteArray.base58EncodeStr(): String = Base58.encode(this)

/**
 * Base58解码
 */
fun String.base58Decode(): ByteArray = Base58.decode(this)
fun String.base58DecodeStr(): String = String(Base58.decode(this))

/*
  UrlEncode/Decode
 */

fun String.urlEncode(charset: String = "UTF-8"): String = URLEncoder.encode(this, charset)

fun String.urlDecode(charset: String = "UTF-8"): String = URLDecoder.decode(this, charset)