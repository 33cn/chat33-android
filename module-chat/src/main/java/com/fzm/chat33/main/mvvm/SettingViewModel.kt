package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.bean.param.AddQuestionParam
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.repo.SettingRepository
import com.fzm.chat33.core.response.StateResponse
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class SettingViewModel @Inject constructor(
        private val repository: SettingRepository
) : LoadingViewModel() {

    private val _setAddVerify by lazy { MutableLiveData<EnableResult>() }
    val setAddVerify: LiveData<EnableResult>
        get() = _setAddVerify

    private val _setAddQuestion by lazy { MutableLiveData<EnableResult>() }
    val setAddQuestion: LiveData<EnableResult>
        get() = _setAddQuestion

    private val _setInviteConfirm by lazy { MutableLiveData<EnableResult>() }
    val setInviteConfirm: LiveData<EnableResult>
        get() = _setInviteConfirm

    private val _editAvatar by lazy { MutableLiveData<EditAvatarResult>() }
    val editAvatar: LiveData<EditAvatarResult>
        get() = _editAvatar

    private val _setAddVerifyQuestion by lazy { MutableLiveData<AddQuestionParam>() }
    val setAddVerifyQuestion: LiveData<AddQuestionParam>
        get() = _setAddVerifyQuestion

    private val _editName by lazy { MutableLiveData<String>() }
    val editName: LiveData<String>
        get() = _editName

    private val _isSetPayPassword by lazy { MutableLiveData<StateResponse>() }
    val isSetPayPassword: LiveData<StateResponse>
        get() = _isSetPayPassword

    fun setAddVerify(enable: Int) {
        request {
            repository.setAddVerify(enable)
        }.result(onSuccess = {
            _setAddVerify.value = EnableResult(it, enable)
        }, onError = {
            _setAddVerify.value = EnableResult(null, enable)
        })
    }

    fun setAddQuestion(param: AddQuestionParam, enable: Int) {
        request {
            repository.setAddQuestion(param)
        }.result(onSuccess = {
            _setAddQuestion.value = EnableResult(it, enable)
        }, onError = {
            _setAddQuestion.value = EnableResult(null, enable)
        })
    }

    fun setInviteConfirm(enable: Int) {
        request {
            repository.setInviteConfirm(enable)
        }.result(onSuccess = {
            _setInviteConfirm.value = EnableResult(it, enable)
        }, onError = {
            _setInviteConfirm.value = EnableResult(null, enable)
        })
    }

    fun editAvatar(channelType: Int?, id: String?, avatar: String) {
        request {
            repository.editAvatar(channelType, id, avatar)
        }.result(onSuccess = {
            _editAvatar.value = EditAvatarResult(it, null)
        }, onError = {
            _editAvatar.value = EditAvatarResult(null, it)
        })
    }

    fun setAddVerifyQuestion(param: AddQuestionParam) {
        start {
            loading()
        }.request {
            repository.setAddQuestion(param)
        }.result(onSuccess = {
            _setAddVerifyQuestion.value = param
        }, onComplete = {
            dismiss()
        })
    }

    fun editName(channelType: Int?, id: String?, newName: String) {
        start {
            loading()
        }.request {
            val name = if (AppConfig.FILE_ENCRYPT && channelType == Chat33Const.CHANNEL_ROOM) {
                try {
                    val roomKey = ChatDatabase.getInstance().roomKeyDao().getLatestKey(id)
                    val prefix = CipherManager.encryptSymmetric(newName, roomKey.keySafe)
                    "${prefix}${AppConfig.ENC_INFIX}${roomKey.kid}${AppConfig.ENC_INFIX}${id}"
                } catch (e: Exception) {
                    e.printStackTrace()
                    newName
                }
            } else {
                newName
            }
            repository.editName(channelType, id, name)
        }.result(onSuccess = {
            _editName.value = newName
        }, onComplete = {
            dismiss()
        })
    }

    fun isSetPayPassword() {
        start {
            loading()
        }.request {
            repository.isSetPayPassword()
        }.result(onSuccess = {
            _isSetPayPassword.value = it
        }, onComplete = {
            dismiss()
        })
    }
}

data class EnableResult(
        val result: Any?,
        val enable: Int
)

data class EditAvatarResult(
        val result: Any?,
        val exception: ApiException?
)

