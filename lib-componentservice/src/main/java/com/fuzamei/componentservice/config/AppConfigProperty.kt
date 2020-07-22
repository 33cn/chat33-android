package com.fuzamei.componentservice.config

import android.annotation.SuppressLint
import android.content.Context
import com.fuzamei.common.FzmFramework
import com.fuzamei.common.utils.AbstractConfigProperty
import com.fuzamei.componentservice.BuildConfig

/**
 * @author zhengjy
 * @since 2019/05/31
 * Description:App根据正式和测试环境的不同而不同的配置在这里设置
 */
class AppConfigProperty(val context: Context) : AbstractConfigProperty(context) {

    private object ConfigPropertyHolder {
        @SuppressLint("StaticFieldLeak")
        val appConfigProperty = AppConfigProperty(FzmFramework.context)
    }

    override fun getFileName(): String {
        return if (AppConfig.DEVELOP) {
            "${BuildConfig.FLAVOR_product}-dev.properties"
        } else {
            "${BuildConfig.FLAVOR_product}-pro.properties"
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): AppConfigProperty{
            return ConfigPropertyHolder.appConfigProperty
        }
    }

}