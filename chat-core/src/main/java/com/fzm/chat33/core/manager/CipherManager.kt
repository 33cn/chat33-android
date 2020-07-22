package com.fzm.chat33.core.manager

import com.fuzamei.common.ext.bytes2Hex
import com.fuzamei.common.ext.hex2Bytes
import walletapi.Walletapi
import walletapi.HDWallet
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.core.utils.UserInfoPreference.*
import javax.crypto.KeyGenerator

/**
 * @author zhengjy
 * @since 2019/05/20
 * Description:
 */
class CipherManager {

    companion object {

        /**
         * 默认密码：chat33Cipher-{md5("chat33Cipher")}
         */
        const val DEFAULT_PASSWORD = "chat33Cipher-7F67A95639B92CDD52C224DB7202DB07"

        /**
         * 创建助记词
         * 中文:     lang:1 bitSize:160
         * English:  lang:0 bitSize:128
         *
         * 0：英文   1：中文
         * bitSize=128 返回12个单词或者汉子，bitSize+32=160  返回15个单词或者汉子，bitSize=256 返回24个单词或者汉子
         */
        @JvmStatic
        fun createMnemonicString(lang: Long, bitSize: Long): String? {
            try {
                return Walletapi.newMnemonicString(lang, bitSize)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * 通过主链类型和助记词获取HDWallet对象
         */
        @JvmStatic
        fun getHDWallet(coinType: String, mnem: String): HDWallet? {
            return try {
                Walletapi.newWalletFromMnemonic_v2(coinType, mnem)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun encryptMnemonicString(mnem: String, password: String): String? {
            val encPassword = encryptPassword(password)
            return seedEncKey(encPassword, mnem)
        }

        @JvmStatic
        fun saveMnemonicString(mnem: String, password: String): String? {
            val encPassword = encryptPassword(password)
            val enemString = seedEncKey(encPassword, mnem)
            UserInfoPreference.getInstance().setBooleanPref(UserInfoPreference.USER_HAS_CHAT_KEY_PWD, DEFAULT_PASSWORD != password)
            UserInfoPreference.getInstance().setStringPref(UserInfoPreference.USER_MNEMONIC_WORDS, enemString)
            UserInfoPreference.getInstance().setStringPref(UserInfoPreference.USER_CHAT_KEY_PWD, passwordHash(encPassword))
            return enemString
        }

        @JvmStatic
        fun saveMnemonicString(mnem: String): String? {
            return saveMnemonicString(mnem, DEFAULT_PASSWORD)
        }

        @JvmStatic
        fun getMnemonicString(password: String): String? {
            return if (checkPassword(password)) {
                val encPassword = encryptPassword(password)
                val encMnem = UserInfoPreference.getInstance().getStringPref(UserInfoPreference.USER_MNEMONIC_WORDS, "")
                seedDecKey(encPassword, encMnem)
            } else {
                null
            }
        }

        @JvmStatic
        fun getMnemonicStringByPassword(password: String, encMnemonic: String): String? {
            val encPassword = encryptPassword(password)
            return seedDecKey(encPassword, encMnemonic)
        }

        @JvmStatic
        fun getMnemonicString(): String? {
            return getMnemonicString(DEFAULT_PASSWORD)
        }

        /**
         * 获取用户加密的助记词密
         */
        @JvmStatic
        fun getEncMnemonicString(): String? {
            return UserInfoPreference.getInstance().getStringPref(UserInfoPreference.USER_MNEMONIC_WORDS, "")
        }

        /**
         * 解密外部传入的助记词
         */
        @JvmStatic
        fun decryptMnemonicString(encMnem: String, password: String): String? {
            return if (checkPassword(password)) {
                val encPassword = encryptPassword(password)
                seedDecKey(encPassword, encMnem)
            } else {
                null
            }
        }

        @JvmStatic
        fun decryptMnemonicString(encMnem: String): String? {
            return decryptMnemonicString(encMnem, DEFAULT_PASSWORD)
        }

        /**
         * 用户是否已设置助记词密码
         */
        @JvmStatic
        fun hasChatPassword(): Boolean {
            return UserInfoPreference.getInstance().getBooleanPref(UserInfoPreference.USER_HAS_CHAT_KEY_PWD, false)
        }

        /**
         * 用户是否已创建公私钥对
         */
        @JvmStatic
        fun hasDHKeyPair(): Boolean {
            val pub = UserInfoPreference.getInstance().getStringPref(USER_PUBLIC_KEY, "")
            val pri = UserInfoPreference.getInstance().getStringPref(USER_PRIVATE_KEY, "")
            return pub.isNotEmpty() && pri.isNotEmpty()
        }

        /**
         * 保存用户公私钥对
         */
        @JvmStatic
        fun saveDHKeyPair(publicKey: String?, privateKey: String?) {
            UserInfoPreference.getInstance().setStringPref(USER_PUBLIC_KEY, publicKey)
            UserInfoPreference.getInstance().setStringPref(USER_PRIVATE_KEY, privateKey)
            try {
                val address = Walletapi.publicKeyToAddress(getPublicKey().hex2Bytes())
                UserInfoPreference.getInstance().setStringPref(USER_ADDRESS, address)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        /**
         * 获取用户公钥
         */
        @JvmStatic
        fun getPublicKey(): String {
            return UserInfoPreference.getInstance().getStringPref(USER_PUBLIC_KEY, "")
        }

        /**
         * 获取用户私钥
         */
        @JvmStatic
        fun getPrivateKey(): String {
            return UserInfoPreference.getInstance().getStringPref(USER_PRIVATE_KEY, "")
        }

        @JvmStatic
        fun getAddress(): String {
            var address = UserInfoPreference.getInstance().getStringPref(USER_ADDRESS, "")
            if (address.isEmpty()) {
                address = Walletapi.publicKeyToAddress(getPublicKey().hex2Bytes())
                UserInfoPreference.getInstance().setStringPref(USER_ADDRESS, address)
                return address
            }
            return address
        }

        /**
         * 加密
         *
         * @param data          待加密数据
         * @param publicKey     对方公钥
         * @param privateKey    己方私钥
         * @return
         * @throws Exception
         */
        @JvmStatic
        fun encryptString(data: String, publicKey: String?, privateKey: String?): String {
            return Walletapi.encryptWithDHKeyPair(privateKey, publicKey, data.toByteArray()).bytes2Hex()
        }

        /**
         * 解密
         *
         * @param data          待解密数据
         * @param publicKey     对方公钥
         * @param privateKey    己方私钥
         * @return
         * @throws Exception
         */
        @JvmStatic
        fun decryptString(data: String, publicKey: String?, privateKey: String?): String {
            return try {
                String(Walletapi.decryptWithDHKeyPair(privateKey, publicKey, data.hex2Bytes()))
            } catch (e: Exception) {
                data
            }
        }

        /**
         * 对称加密，用于群聊加密
         */
        @JvmStatic
        fun encryptSymmetric(data: String, key: String): String {
            return Walletapi.encryptSymmetric(key, data.toByteArray()).bytes2Hex()
        }

        /**
         * 对称解密，用于群聊解密
         */
        @JvmStatic
        fun decryptSymmetric(data: String, key: String): String {
            return try {
                return String(Walletapi.decryptSymmetric(key, data.hex2Bytes()))
            } catch (e: Exception) {
                data
            }
        }

        @JvmStatic
        fun generateAESKey(length: Int): ByteArray {
            return try {
                val gen = KeyGenerator.getInstance("AES")
                gen.init(length)
                val key = gen.generateKey()
                return key.encoded
            } catch (e: Exception) {
                e.printStackTrace()
                ByteArray(0)
            }
        }

        /**
         * 数字签名接口
         */
        @JvmStatic
        fun sign(data: String, privateKey: String): ByteArray {
            return if (privateKey.length == 64) {
                try {
                    return Walletapi.chatSign(data.toByteArray(), privateKey.hex2Bytes())
                } catch (e: Exception) {
                    e.printStackTrace()
                    ByteArray(0)
                }
            } else {
                ByteArray(0)
            }
        }

        /**
         * 加密密聊密码
         */
        @JvmStatic
        fun encryptPassword(password: String): ByteArray? {
            return try {
                Walletapi.encPasswd(password)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * 密聊密码Hash
         */
        private fun passwordHash(password: ByteArray?): String? {
            return try {
                Walletapi.passwdHash(password)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * 校验密聊密码
         */
        @JvmStatic
        fun checkPassword(password: String): Boolean {
            return checkPassword(password, UserInfoPreference.getInstance().getStringPref(UserInfoPreference.USER_CHAT_KEY_PWD, ""))
        }

        @JvmStatic
        fun checkPassword(password: String, passwordHash: String): Boolean {
            var checked = false
            try {
                checked = Walletapi.checkPasswd(password, passwordHash)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return checked
        }

        /**
         * 对助记词进行加密
         */
        private fun seedEncKey(encPassword: ByteArray?, seed: String): String? {
            return try {
                val bSeed = Walletapi.stringTobyte(seed)
                val seedEncKey = Walletapi.seedEncKey(encPassword, bSeed)
                Walletapi.byteTohex(seedEncKey)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * 对助记词进行解密
         */
        @JvmStatic
        fun seedDecKey(encPassword: ByteArray?, seed: String): String? {
            return try {
                val bSeed = Walletapi.hexTobyte(seed)
                val seedDecKey = Walletapi.seedDecKey(encPassword, bSeed)
                Walletapi.byteTostring(seedDecKey)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}

class CipherException(
        id: String?,
        publicKey: String?,
        decryptKey: String?,
        cause: Throwable?
) : Exception("id:${id},publicKey:${publicKey},decryptKey${decryptKey}", cause)
