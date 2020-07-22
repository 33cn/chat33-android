package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.DepositSMS
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.request.PayPasswordRequest
import com.fzm.chat33.core.repo.SettingRepository
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/08/15
 * Description:
 */
class PayPasswordViewModel @Inject constructor(
        private val repository: SettingRepository
) : LoadingViewModel(), LoginInfoDelegate by repository {

    private val _checkPassword by lazy { MutableLiveData<ApiException>() }
    val checkPassword: LiveData<ApiException>
        get() = _checkPassword

    private val _setPayPassword by lazy { MutableLiveData<ApiException>() }
    val setPayPassword: LiveData<ApiException>
        get() = _setPayPassword

    private val _depositSMS by lazy { MutableLiveData<DepositSMS>() }
    val depositSMS: LiveData<DepositSMS>
        get() = _depositSMS

    private val _depositVoiceSMS by lazy { MutableLiveData<DepositSMS>() }
    val depositVoiceSMS: LiveData<DepositSMS>
        get() = _depositVoiceSMS

    private val _verifyCode by lazy { MutableLiveData<Any>() }
    val verifyCode: LiveData<Any>
        get() = _verifyCode


    fun checkPayPassword(code: String) {
        start {
            loading()
        }.request {
            repository.checkPayPassword(code)
        }.result({
            _checkPassword.value = null
        }, {
            _checkPassword.value = it
        }, {
            dismiss()
        })
    }

    fun setPayPassword(request: PayPasswordRequest) {
        start {
            loading()
        }.request {
            repository.setPayPassword(request)
        }.result({
            _setPayPassword.value = null
        }, {
            _setPayPassword.value = it
        }, {
            dismiss()
        })
    }

    fun sendSMS4(phone: String, businessId: String, ticket: String) {
        request {
            repository.sendSMS(DepositSMS.AREA, phone, DepositSMS.CODETYPE_RESET_PAY_PASSWORD,
                    DepositSMS.SMS_PARAM4, "", businessId, ticket)
        }.result {
            _depositSMS.value = it
        }
    }

    fun sendVoiceCode(phone: String, businessId: String, ticket: String) {
        request {
            repository.sendVoiceCode(DepositSMS.AREA, phone, DepositSMS.CODETYPE_RESET_PAY_PASSWORD,
                    DepositSMS.SMS_PARAM4, businessId, ticket)
        }.result {
            _depositVoiceSMS.value = it
        }
    }

    fun verifyCode(phone: String, sendType: String, code: String) {
        request {
            repository.verifyCode(DepositSMS.AREA, phone, "",
                    DepositSMS.CODETYPE_RESET_PAY_PASSWORD, sendType, code)
        }.result {
            _verifyCode.value = null
        }
    }
}