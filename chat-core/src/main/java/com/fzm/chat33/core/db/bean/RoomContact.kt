package com.fzm.chat33.core.db.bean

import androidx.room.Ignore
import com.fzm.chat33.core.utils.PinyinUtils
import java.io.Serializable

data class RoomContact (
        //朋友
        var roomId: String,
        var id: String,
        var nickname: String?,
        var roomNickname: String?,
        var avatar: String?,
        var memberLevel: Int,
        var roomMutedType : Int, //1 全员发言 2黑名单 3 白名单 4 全员禁言
        var mutedType: Int, //1 不采用 2 黑名单 3 白名单
        var deadline: Long,
        var identification: Int,
        var identificationInfo: String?,
        var searchKey: String?,
        var friendRemark: String?,
        var friendSearchKey: String?

) : Serializable, Sortable {

    @Ignore
    private var letter: String? = null
    fun getDisplayName(): String? {
        return if (friendRemark.isNullOrEmpty()) (if (roomNickname.isNullOrEmpty()) nickname else roomNickname) else friendRemark
    }

    fun isIdentified(): Boolean {
        return identification == 1
    }

    override fun priority(): Int {
        return memberLevel
    }

    override fun getFirstChar(): String {
        return if (!friendRemark.isNullOrEmpty()) {
            friendRemark!!.substring(0, 1)
        } else if (!roomNickname.isNullOrEmpty()) {
            roomNickname!!.substring(0, 1)
        } else if (!nickname.isNullOrEmpty()) {
            nickname!!.substring(0, 1)
        } else {
            "#"
        }
    }

    override fun getFirstLetter(): String {
        return letters.substring(0, 1)
    }


    override fun getLetters(): String {
        if (!letter.isNullOrEmpty()) {
            return letter!!
        }
        //汉字转换成拼音
        val pinyin = if (!friendRemark.isNullOrEmpty()){
            PinyinUtils.getPingYin(friendRemark)
        } else if (!roomNickname.isNullOrEmpty()) {
            PinyinUtils.getPingYin(roomNickname)
        } else if (!nickname.isNullOrEmpty()) {
            PinyinUtils.getPingYin(nickname)
        } else {
            "#"
        }
        val sortString = pinyin.substring(0, 1).toUpperCase()
        return if (sortString.matches("[A-Z]".toRegex())) {
            letter = pinyin.toUpperCase()
            letter!!
        } else {
            letter = "#"
            letter!!
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomContact

        if (roomId != other.roomId) return false
        if (id != other.id) return false
        if (nickname != other.nickname) return false
        if (roomNickname != other.roomNickname) return false
        if (avatar != other.avatar) return false
        if (memberLevel != other.memberLevel) return false
        if (roomMutedType != other.roomMutedType) return false
        if (mutedType != other.mutedType) return false
        if (deadline != other.deadline) return false
        if (identification != other.identification) return false
        if (identificationInfo != other.identificationInfo) return false
        if (searchKey != other.searchKey) return false
        if (friendRemark != other.friendRemark) return false
        if (friendSearchKey != other.friendSearchKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roomId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (nickname?.hashCode() ?: 0)
        result = 31 * result + (roomNickname?.hashCode() ?: 0)
        result = 31 * result + (avatar?.hashCode() ?: 0)
        result = 31 * result + memberLevel
        result = 31 * result + roomMutedType
        result = 31 * result + mutedType
        result = 31 * result + deadline.hashCode()
        result = 31 * result + (identification)
        result = 31 * result + (identificationInfo?.hashCode() ?: 0)
        result = 31 * result + (searchKey?.hashCode() ?: 0)
        result = 31 * result + (friendSearchKey?.hashCode() ?: 0)
        return result
    }
}