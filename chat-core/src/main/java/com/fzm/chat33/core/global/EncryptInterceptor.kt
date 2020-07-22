package com.fzm.chat33.core.global

import android.util.Log
import com.fuzamei.common.net.rxjava.HttpResult
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ExtRemark
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.repo.SettingRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.instance

/**
 * @author zhengjy
 * @since 2019/12/24
 * Description:对指定接口返回数据进行解密的拦截器
 */
class EncryptInterceptor : Interceptor {

    companion object {
        const val TAG = "EncryptInterceptor"
    }

    private val paths = arrayOf(
            "/chat/user/userInfo",
            "chat/user/usersInfo",
            "/chat/friend/list",
            "/chat/friend/blocked-list",
            "/chat/room/list",
            "/chat/room/info"
    )

    private val gson: Gson by Kodein.global.instance()
    private val setting: SettingRepository by Kodein.global.instance()

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!AppConfig.FILE_ENCRYPT) {
            return chain.proceed(chain.request())
        }
        val request = chain.request()
        val index = paths.matches(request.url().encodedPath())
        return if (index != -1) {
            val response = chain.proceed(request)
            val body = response.body()?.string() ?: ""
            val decBody = when (paths[index]) {
                "/chat/user/userInfo" -> {
                    try {
                        if (!body.contains("privateKey")) {
                            val temp = gson.fromJson<HttpResult<FriendBean>>(body, object : TypeToken<HttpResult<FriendBean>>() {}.type)
                            if (!temp.data.extRemark.encrypt.isNullOrEmpty()) {
                                val decExt = CipherManager.decryptString(temp.data.extRemark.encrypt, CipherManager.getPublicKey(), CipherManager.getPrivateKey())
                                temp.data.extRemark = gson.fromJson(decExt, ExtRemark::class.java)
                            }
                            if (!temp.data.remark.isNullOrEmpty()) {
                                val decRemark = CipherManager.decryptString(temp.data.remark, CipherManager.getPublicKey(), CipherManager.getPrivateKey())
                                temp.data.remark = decRemark
                            }
                            gson.toJson(temp)
                        } else {
                            body
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "${paths[index]}解密出错，${e.message}")
                        body
                    }
                }
                "chat/user/usersInfo",
                "/chat/friend/list",
                "/chat/friend/blocked-list" -> {
                    try {
                        val temp = gson.fromJson<HttpResult<FriendBean.Wrapper>>(body, object : TypeToken<HttpResult<FriendBean.Wrapper>>() {}.type)
                        for (friend in temp.data.userList) {
                            try {
                                if (!friend.extRemark.encrypt.isNullOrEmpty()) {
                                    val decExt = CipherManager.decryptString(friend.extRemark.encrypt, CipherManager.getPublicKey(), CipherManager.getPrivateKey())
                                    friend.extRemark = gson.fromJson(decExt, ExtRemark::class.java)
                                }
                                if (!friend.remark.isNullOrEmpty()) {
                                    val decRemark = CipherManager.decryptString(friend.remark, CipherManager.getPublicKey(), CipherManager.getPrivateKey())
                                    friend.remark = decRemark
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "用户id ${friend.id}解密出错，${e.message}")
                            }
                        }
                        gson.toJson(temp)
                    } catch (e: Exception) {
                        Log.e(TAG, "${paths[index]}解密出错，${e.message}")
                        body
                    }
                }
                "/chat/room/list" -> {
                    try {
                        val temp = gson.fromJson<HttpResult<RoomListBean.Wrapper>>(body, object : TypeToken<HttpResult<RoomListBean.Wrapper>>() {}.type)
                        for (room in temp.data.roomList) {
                            try {
                                val pair = room.name.split(AppConfig.ENC_INFIX)
                                if (pair.size != 3) {
                                    continue
                                } else {
                                    val encName = pair[0]
                                    val kid = pair[1]
                                    val roomKey = ChatDatabase.getInstance().roomKeyDao().getRoomKeyById(room.id, kid)
                                    room.name = CipherManager.decryptSymmetric(encName, roomKey.keySafe)
                                    updateRoomName(room.id, room.name, kid)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "群id ${room.id}解密出错，${e.message}")
                            }
                        }
                        gson.toJson(temp)
                    } catch (e: Exception) {
                        Log.e(TAG, "${paths[index]}解密出错，${e.message}")
                        body
                    }
                }
                "/chat/room/info" -> {
                    try {
                        val temp = gson.fromJson<HttpResult<RoomInfoBean>>(body, object : TypeToken<HttpResult<RoomInfoBean>>() {}.type)
                        val pair = temp.data.name.split(AppConfig.ENC_INFIX)
                        if (pair.size != 3) {
                            body
                        } else {
                            val encName = pair[0]
                            val kid = pair[1]
                            val roomKey = ChatDatabase.getInstance().roomKeyDao().getRoomKeyById(temp.data.id, kid)
                            temp.data.name = CipherManager.decryptSymmetric(encName, roomKey.keySafe)
                            updateRoomName(temp.data.id, temp.data.name, kid)
                            gson.toJson(temp)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "${paths[index]}解密出错，${e.message}")
                        body
                    }
                }
                else -> body
            }
            val mediaType = response.body()?.contentType()
            response.newBuilder().body(ResponseBody.create(mediaType, decBody)).build()
        } else {
            chain.proceed(request)
        }
    }

    /**
     * 根据需要确定是否重新加密群名
     *
     * @param roomId    群id
     * @param roomName  群名（未加密）
     * @param kid       加密当前群名的kid
     */
    private fun updateRoomName(roomId: String, roomName: String, kid: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val roomKey = ChatDatabase.getInstance().roomKeyDao().getLatestKey(roomId)
            if (roomKey == null || roomKey.kid.toLong() <= kid.toLong()) {
                // 如果当前最新密钥不比加密群名的密钥更新，则不操作
                return@launch
            }
            try {
                val prefix = CipherManager.encryptSymmetric(roomName, roomKey.keySafe)
                val name = "${prefix}${AppConfig.ENC_INFIX}${roomKey.kid}${AppConfig.ENC_INFIX}${roomId}"
                setting.editName(Chat33Const.CHANNEL_ROOM, roomId, name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun Array<String>.matches(str: String?): Int {
        if (str == null) {
            return -1
        }
        for (index in this.indices) {
            if (str.contains(get(index))) {
                return index
            }
        }
        return -1
    }
}