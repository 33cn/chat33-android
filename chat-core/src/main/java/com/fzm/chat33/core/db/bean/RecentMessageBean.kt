package com.fzm.chat33.core.db.bean

import com.fuzamei.componentservice.config.AppConfig
import java.io.Serializable

data class RecentMessageBean (
        //朋友或者群聊id
        var id: String,
        var isDeleted: Boolean,
        var stickyTop: Int,
        var number: Int,
        var channelType: Int,
        var name: String?,
        var remark: String?,
        var avatar: String?,
        var depositAddress: String?,
        var noDisturb: Int,
        var identification: Int?,
        var datetime: Long,
        var msgType: Int,
        var content: String?,
        var fileName: String?,
        var isSnap: Int,
        var fromId: String?,
        var redBagRemark: String?,
        var inviterId: String?,
        // 1:有@消息  2:无@消息
        var beAit: Boolean,
        var recent_like: Int,
        var recent_reward: Int,
        //群组id
        var targetId: String?,
        var nickname: String?,
        // 封群截至时间
        var disableDeadline: Long,
        // 是否置顶
        var onTop: Int,
        // 仅好友可看
        var noDisturbing: Int
) : Serializable {

    fun getDisplayName(): String? {
        return if (remark.isNullOrEmpty()) name else remark
    }

    fun isIdentified(): Boolean {
        return identification == 1
    }

    fun isSentType(): Boolean {
        return if (fromId.isNullOrEmpty()) {
            false
        } else {
            fromId == AppConfig.MY_ID
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecentMessageBean

        if (id != other.id) return false
        if (isDeleted != other.isDeleted) return false
        if (stickyTop != other.stickyTop) return false
        if (number != other.number) return false
        if (channelType != other.channelType) return false
        if (name != other.name) return false
        if (remark != other.remark) return false
        if (avatar != other.avatar) return false
        if (depositAddress != other.depositAddress) return false
        if (noDisturb != other.noDisturb) return false
        if (identification != other.identification) return false
        if (datetime != other.datetime) return false
        if (msgType != other.msgType) return false
        if (content != other.content) return false
        if (fileName != other.fileName) return false
        if (isSnap != other.isSnap) return false
        if (fromId != other.fromId) return false
        if (redBagRemark != other.redBagRemark) return false
        if (inviterId != other.inviterId) return false
        if (beAit != other.beAit) return false
        if (recent_like != other.recent_like) return false
        if (recent_reward != other.recent_reward) return false
        if (targetId != other.targetId) return false
        if (nickname != other.nickname) return false
        if (disableDeadline != other.disableDeadline) return false
        if (onTop != other.onTop) return false
        if (noDisturbing != other.noDisturbing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + isDeleted.hashCode()
        result = 31 * result + stickyTop
        result = 31 * result + number
        result = 31 * result + channelType
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (remark?.hashCode() ?: 0)
        result = 31 * result + (avatar?.hashCode() ?: 0)
        result = 31 * result + (depositAddress?.hashCode() ?: 0)
        result = 31 * result + noDisturb
        result = 31 * result + (identification ?: 0)
        result = 31 * result + datetime.hashCode()
        result = 31 * result + msgType
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + isSnap
        result = 31 * result + (fromId?.hashCode() ?: 0)
        result = 31 * result + (redBagRemark?.hashCode() ?: 0)
        result = 31 * result + (inviterId?.hashCode() ?: 0)
        result = 31 * result + beAit.hashCode()
        result = 31 * result + recent_like
        result = 31 * result + recent_reward
        result = 31 * result + (targetId?.hashCode() ?: 0)
        result = 31 * result + (nickname?.hashCode() ?: 0)
        result = 31 * result + disableDeadline.hashCode()
        result = 31 * result + onTop
        result = 31 * result + noDisturbing
        return result
    }
}