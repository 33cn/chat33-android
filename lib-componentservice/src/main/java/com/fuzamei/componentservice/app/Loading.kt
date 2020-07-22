package com.fuzamei.componentservice.app

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/03/04
 * Description:loading框的显示逻辑
 */
data class Loading(
        /**
         * 是否正在显示
         */
        var loading: Boolean = true,
        /**
         * 加载框是否可取消
         */
        var cancelable: Boolean = true
): Serializable