package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fuzamei.componentservice.consts.AppError
import com.fzm.chat33.core.bean.param.EditExtRemarkParam
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.repo.ContactsRepository
import com.google.gson.Gson
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.instance
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class EditUserRemarkViewModel @Inject constructor(
        private val repository: ContactsRepository
): LoadingViewModel() {

    private val gson: Gson by Kodein.global.instance()

    private val _setFriendExtRemark by lazy { MutableLiveData<EditExtRemarkParam>() }
    val setFriendExtRemark : LiveData<EditExtRemarkParam>
        get() = _setFriendExtRemark

    fun setFriendExtRemark(param: EditExtRemarkParam) {
        start {
            loading()
        }.request{
            try {
                val temp = EditExtRemarkParam().apply {
                    telephones = param.telephones
                    description = param.description
                    pictures = param.pictures
                }
                val enc = CipherManager.encryptString(gson.toJson(temp), CipherManager.getPublicKey(), CipherManager.getPrivateKey())
                val encRemark = if (!param.remark.isNullOrEmpty()) {
                    CipherManager.encryptString(param.remark, CipherManager.getPublicKey(), CipherManager.getPrivateKey())
                } else {
                    param.remark
                }
                val real = EditExtRemarkParam().apply {
                    this.id = param.id
                    this.remark = encRemark
                    this.encrypt = enc
                }
                repository.setFriendExtRemark(real)
            } catch (e: Exception) {
                Result.Error(ApiException(AppError.ENCRYPT_ERROR))
            }
        }.result(onSuccess = {
            _setFriendExtRemark.value = param
        }, onComplete = {
            dismiss()
        })
    }
}

