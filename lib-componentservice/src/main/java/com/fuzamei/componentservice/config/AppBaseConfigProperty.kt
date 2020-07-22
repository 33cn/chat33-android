package com.fuzamei.componentservice.config

import android.annotation.SuppressLint
import android.content.Context
import com.fuzamei.common.utils.AbstractConfigProperty
import com.fuzamei.componentservice.BuildConfig
import com.fuzamei.common.FzmFramework

/**
 * @author zhengjy
 * @since 2019/05/31
 * Description:App的基本信息配置
 */
class AppBaseConfigProperty(val context: Context) : AbstractConfigProperty(context) {

    private object ConfigPropertyHolder {
        @SuppressLint("StaticFieldLeak")
        val appConfigProperty = AppBaseConfigProperty(FzmFramework.context)
    }

    override fun getFileName(): String {
        return "${BuildConfig.FLAVOR_product}-base.properties"
    }

    companion object {
        @JvmStatic
        fun getInstance(): AppBaseConfigProperty{
            return ConfigPropertyHolder.appConfigProperty
        }
    }
}