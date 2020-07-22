package com.fzm.chat33.core.manager

import android.util.Log
import com.fuzamei.common.ext.bytes2Hex
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.net.socket.ChatSocket
import com.fzm.chat33.core.repo.ContactsRepository
import com.fzm.chat33.core.request.UpdateGroupKeyRequest
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.kodein.di.conf.KodeinGlobalAware
import org.kodein.di.generic.instance
import java.util.*

/**
 * @author zhengjy
 * @since 2019/05/29
 * Description:加密群密钥管理
 */
class GroupKeyManager {

    companion object : KodeinGlobalAware {

        private val gson: Gson by instance()
        private val repository: ContactsRepository by instance()
        private val socket: ChatSocket by instance()

        /**
         * 更新加密群聊的会话密钥
         *
         * @param roomId    群id
         * @param callback  完成更新消息发送后的回调，防止密钥还未更新，用户就发送消息
         */
        @JvmStatic
        fun notifyGroupEncryptKey(roomId: String, callback: () -> Unit) = GlobalScope.launch(Dispatchers.Main) {
            if (!AppConfig.APP_ENCRYPT) {
                callback()
                return@launch
            }
            // 获取当前群所有群成员
            val roomUserWrapper = withContext(Dispatchers.IO) {
                repository.getRoomUsers(roomId)
            }
            val roomUser = roomUserWrapper.dataOrNull()?.userList ?: return@launch

            val secrets = Collections.synchronizedList(mutableListOf<UpdateGroupKeyRequest.Secret>())
            val allKey = launch(Dispatchers.IO) {
                val key = CipherManager.generateAESKey(256).bytes2Hex()
                for (user in roomUser) {
                    if (user.publicKey.isNullOrEmpty()) {
                        continue
                    }
                    launch {
                        try {
                            // 用每一位群成员的公钥和自己的私钥对随机生成的群密钥进行加密
                            val tempKey: String = if (user.id == AppConfig.MY_ID) {
                                CipherManager.encryptString(key, CipherManager.getPublicKey(), CipherManager.getPrivateKey())
                            } else {
                                CipherManager.encryptString(key, user.publicKey, CipherManager.getPrivateKey())
                            }
                            secrets.add(UpdateGroupKeyRequest.Secret(user.id, tempKey))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            allKey.join()
            val request = UpdateGroupKeyRequest(roomId, CipherManager.getPublicKey(), secrets)
            val msg = gson.toJson(request)
            socket.send(msg)
            Log.d("GroupKeyManager", msg)
            callback()
        }

        /**
         * 更新加密群聊的会话密钥
         *
         * @param roomId    群id
         */
        @JvmStatic
        fun notifyGroupEncryptKey(roomId: String) {
            notifyGroupEncryptKey(roomId) {}
        }
    }
}