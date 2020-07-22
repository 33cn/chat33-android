package com.fzm.chat33.core.decentration.contract

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @author zhengjy
 * @since 2020/02/06
 * Description:
 */
data class UserAddress(
        var mainAddress: String,
        @SerializedName(value = "friendAddress", alternate = ["targetAddress"])
        var friendAddress: String,
        var createTime: Long
) : Serializable {

    data class Wrapper(
            @SerializedName(value = "friends", alternate = ["list"])
            var friends: List<UserAddress>
    ) : Serializable
}