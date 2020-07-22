package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import walletapi.Walletapi
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.utils.ByteUtils
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.main.activity.ImportMnemonicActivity.Companion.TYPE_LOCAL_NO_SERVICE
import com.fzm.chat33.main.activity.ImportMnemonicActivity.Companion.TYPE_LOCAL_SERVICE
import com.fzm.chat33.main.activity.ImportMnemonicActivity.Companion.TYPE_NO_LOCAL_SERVICE
import com.fzm.chat33.core.repo.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class ImportMnemonicViewModel @Inject constructor(
        private val repository: SettingRepository,
        private val loginInfoDelegate: LoginInfoDelegate
) : LoadingViewModel(), LoginInfoDelegate by loginInfoDelegate {

    private val _saveMnemonicWord by lazy { MutableLiveData<Any>() }
    val saveMnemonicWord: LiveData<Any>
        get() = _saveMnemonicWord

    private val _updateSecretKeyMnemonicWord by lazy { MutableLiveData<Any>() }
    val updateSecretKeyMnemonicWord: LiveData<Any>
        get() = _updateSecretKeyMnemonicWord

    private val _checkError by lazy { MutableLiveData<Int>() }
    val checkError: LiveData<Int>
        get() = _checkError

    var type = 0

    /**
     * 加密后的助记词，公钥上传至服务端
     *
     * @param password            密聊密码
     * @param mnemonic            助记词
     */
    fun saveMnemonicWord(password: String, mnemonic: String) {
        loading()
        launch (Dispatchers.Main){
            withContext(Dispatchers.IO) {
                val hdWallet = CipherManager.getHDWallet(Walletapi.TypeBtyString, mnemonic)
                CipherManager.saveDHKeyPair(ByteUtils.bytes2Hex(hdWallet!!.newKeyPub(0)), ByteUtils.bytes2Hex(hdWallet.newKeyPriv(0)))
                CipherManager.saveMnemonicString(mnemonic, password)
            }
            _saveMnemonicWord.value = true
        }

    }

    /**
     * 加密后的助记词，公钥上传至服务端
     *
     * @param password            密聊密码
     * @param mnemonic            助记词
     */
    fun updateSecretKeyMnemonicWord(publicKey: String, localMnemonic: String) {
        start {
            loading()
        }.request {
            repository.uploadSecretKey(publicKey, localMnemonic)
        }.result (onSuccess = {
            updateInfo {
                privateKey = CipherManager.getEncMnemonicString()
            }
            _updateSecretKeyMnemonicWord.value = it
        }, onComplete = {
            dismiss()
        })
    }

    fun checkPassword(password: String, encMnemonic: String) {
        loading()
        launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    CipherManager.getMnemonicStringByPassword(password, encMnemonic)
                }
                if (result.isNullOrEmpty()) {
                    _checkError.value = R.string.chat_error_encrypt_password
                    dismiss()
                } else {
                    _checkError.value = 0
                    when(type) {
                        TYPE_NO_LOCAL_SERVICE->{
                            saveMnemonicWord(password, result)
                        }
                        TYPE_LOCAL_NO_SERVICE->{
                            //本地已设置公私钥，只需上传助记词到服务器
                            val publicKey = CipherManager.getPublicKey()
                            updateSecretKeyMnemonicWord(publicKey, encMnemonic)
                        }
                        TYPE_LOCAL_SERVICE->{
                            saveMnemonicWord(password, result)
                        }
                        else->{
                            throw RuntimeException("Invalid mnemonic type")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _checkError.value = R.string.chat_error_encrypt_password
                dismiss()
            }
        }
    }
}