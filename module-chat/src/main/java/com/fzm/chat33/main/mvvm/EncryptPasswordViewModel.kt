package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.bytes2Hex
import walletapi.Walletapi
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.utils.ByteUtils
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.R
import com.fuzamei.componentservice.consts.AppError
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.exception.AppException
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.manager.GroupKeyManager
import com.fzm.chat33.core.repo.SettingRepository
import com.fzm.chat33.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/24
 * Description:
 */
class EncryptPasswordViewModel @Inject constructor(
        private val repository: SettingRepository,
        private val loginInfoDelegate: LoginInfoDelegate
) : LoadingViewModel(), LoginInfoDelegate by loginInfoDelegate {

    /*-----------------------------------设置密聊密码-----------------------------------*/
    private val _firstError by lazy { MutableLiveData<Int>() }
    val firstError: LiveData<Int>
        get() = _firstError

    private val _secondError by lazy { MutableLiveData<Int>() }
    val secondError: LiveData<Int>
        get() = _secondError

    private val _mnemonicResult by lazy { MutableLiveData<Any>() }
    val mnemonicResult: LiveData<Any>
        get() = _mnemonicResult

    /*-----------------------------------更新密聊密码-----------------------------------*/
    private val _oldError by lazy { MutableLiveData<Int>() }
    val oldError: LiveData<Int>
        get() = _oldError

    private val _newError by lazy { MutableLiveData<Int>() }
    val newError: LiveData<Int>
        get() = _newError

    private val _newSecondError by lazy { MutableLiveData<Int>() }
    val newSecondError: LiveData<Int>
        get() = _newSecondError

    private val _changeResult by lazy { MutableLiveData<Any>() }
    val changeResult: LiveData<Any>
        get() = _changeResult

    private var setWords = ""

    private var updateWords = ""

    /**
     * 加密后的助记词，公钥上传至服务端
     *
     * @param first             第一次输入的密码
     * @param second            第二次确认密码
     * @param forgetPassword    是否是忘记密码
     */
    fun setMnemonicWord(first: String, second: String, forgetPassword: Boolean) {
        if (!setCheck(first, second)) {
            return
        }
        start {
            loading()
        }.request {
            setWords = when {
                forgetPassword -> {
                    // 忘记密码，则自动生成中文助记词
                    CipherManager.createMnemonicString(1, 160)
                            ?: throw AppException(AppError.CREATE_WORDS_ERROR)
                }
                CipherManager.hasDHKeyPair() -> {
                    // 本地有公私钥对，则尝试解密助记词
                    CipherManager.getMnemonicString()
                            ?: throw AppException(AppError.DECRYPT_ERROR)
                }
                else -> {
                    // 其他情况，则按刚进入App创建新的助记词处理
                    CipherManager.createMnemonicString(1, 160)
                            ?: throw AppException(AppError.CREATE_WORDS_ERROR)
                }
            }
            val encString = CipherManager.encryptMnemonicString(setWords, first)
                    ?: throw AppException(AppError.ENCRYPT_ERROR)
            val hdWallet = CipherManager.getHDWallet(Walletapi.TypeBtyString, setWords)
            repository.uploadSecretKey(ByteUtils.bytes2Hex(hdWallet!!.newKeyPub(0)), encString)
        }.result(onSuccess = {
            GlobalScope.launch(Dispatchers.IO) {
                // 是否创建了新的助记词
                val createMnemonic = !CipherManager.hasDHKeyPair() || forgetPassword
                updateInfo {
                    val hdWallet = CipherManager.getHDWallet(Walletapi.TypeBtyString, setWords)
                    CipherManager.saveDHKeyPair(hdWallet!!.newKeyPub(0).bytes2Hex(),
                            hdWallet.newKeyPriv(0).bytes2Hex())
                    CipherManager.saveMnemonicString(setWords, first)
                    privateKey = CipherManager.encryptMnemonicString(setWords, first)
                }
                if (createMnemonic) {
                    // 如果本地原先没有公私钥对，则更新所有群密钥
                    ChatDatabase.getInstance().roomsDao().allRoomsOnce?.forEach { room ->
                        if (room.encrypt == 1) {
                            GroupKeyManager.notifyGroupEncryptKey(room.id)
                        }
                    }
                }
            }
            _mnemonicResult.value = it
        }, onComplete = {
            dismiss()
        })
    }

    /**
     * 检查第一次输入密码是否符合要求
     */
    fun checkFirst(password: String): Boolean {
        return if (password.length !in 8..16) {
            _firstError.value = R.string.chat_tips_update_encrypt_pwd3
            false
        } else if (!StringUtils.isEncryptPassword(password)) {
            _firstError.value = R.string.chat_tips_update_encrypt_pwd4
            false
        } else {
            _firstError.value = 0
            true
        }
    }

    private fun setCheck(first: String, second: String): Boolean {
        val checkFirst = checkFirst(first)
        val checkSecond = if (first != second) {
            _secondError.value = R.string.chat_tips_update_encrypt_pwd6
            false
        } else {
            _secondError.value = 0
            true
        }
        return checkFirst && checkSecond
    }

    /**
     * 外部调用，检查输入的旧密码是否正确
     */
    fun checkOldPassword(password: String) {
        launch {
            checkOld(password)
        }
    }

    /**
     * 修改密聊密码，重新上传加密后的助记词和公钥
     */
    fun changePassword(oldPassword: String, password: String, passwordAgain: String) {
        start {
            loading()
        }.request {
            if (updateCheck(oldPassword, password, passwordAgain)) {
                updateWords = CipherManager.getMnemonicString(oldPassword)
                        ?: throw AppException(AppError.DECRYPT_ERROR)
                val encString = CipherManager.encryptMnemonicString(updateWords, password)
                        ?: throw AppException(AppError.ENCRYPT_ERROR)
                val hdWallet = CipherManager.getHDWallet(Walletapi.TypeBtyString, updateWords)
                repository.uploadSecretKey(ByteUtils.bytes2Hex(hdWallet!!.newKeyPub(0)), encString)
            } else {
                Result.Error(ApiException(AppError.IGNORE_ERROR))
            }
        }.result(onSuccess = {
            GlobalScope.launch(Dispatchers.IO) {
                updateInfo {
                    val hdWallet = CipherManager.getHDWallet(Walletapi.TypeBtyString, updateWords)
                    CipherManager.saveDHKeyPair(hdWallet!!.newKeyPub(0).bytes2Hex(),
                            hdWallet.newKeyPriv(0).bytes2Hex())
                    CipherManager.saveMnemonicString(updateWords, password)
                    privateKey = CipherManager.encryptMnemonicString(updateWords, password)
                }
            }
            _changeResult.value = it
        }, onComplete = {
            dismiss()
        })
    }

    /**
     * 检查输入的旧密码是否正确
     */
    private suspend fun checkOld(password: String): Boolean {
        val check = withContext(Dispatchers.IO) {
            CipherManager.checkPassword(password)
        }
        if (!check) {
            _oldError.postValue(R.string.chat_tips_update_encrypt_pwd2)
        } else {
            _oldError.postValue(0)
        }
        return check
    }

    /**
     * 检查输入的新密码是否正确
     */
    fun checkNew(password: String): Boolean {
        return if (password.length !in 8..16) {
            _newError.postValue(R.string.chat_tips_update_encrypt_pwd3)
            false
        } else if (!StringUtils.isEncryptPassword(password)) {
            _newError.postValue(R.string.chat_tips_update_encrypt_pwd4)
            false
        } else {
            _newError.postValue(0)
            true
        }
    }

    private suspend fun updateCheck(oldPassword: String, password: String, passwordAgain: String): Boolean {
        val check = checkOld(oldPassword) && checkNew(password)
        if (password != passwordAgain) {
            _newSecondError.postValue(R.string.chat_tips_update_encrypt_pwd6)
        } else {
            _newSecondError.postValue(0)
        }
        return check && password == passwordAgain
    }

    fun resetSecondError() {
        _secondError.value = 0
        _newSecondError.value = 0
    }
}