package com.fuzamei.componentservice.base

import android.content.Context
import androidx.fragment.app.DialogFragment
import org.kodein.di.AnyKodeinContext
import org.kodein.di.Kodein
import org.kodein.di.KodeinContext
import org.kodein.di.KodeinTrigger
import org.kodein.di.conf.KodeinGlobalAware
import org.kodein.di.conf.global
import org.kodein.di.jxinject.jx

/**
 * @author zhengjy
 * @since 2019/12/09
 * Description:提供注入容器的[DialogFragment]
 */
abstract class DIDialogFragment : DialogFragment(), KodeinGlobalAware {

    override val kodein: Kodein get() = Kodein.global

    override val kodeinContext: KodeinContext<*> get() = AnyKodeinContext

    override val kodeinTrigger: KodeinTrigger? get() = null

    override fun onAttach(context: Context?) {
        kodein.jx.inject(this)
        super.onAttach(context)
    }
}