package com.fzm.chat33.main.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.BarUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.main.mvvm.ImportMnemonicViewModel
import com.fzm.chat33.widget.CheckEncryptPasswordDialog
import kotlinx.android.synthetic.main.activity_import_mnemonic_word.*
import javax.inject.Inject

@Route (path = AppRoute.IMPORT_MNEMONIC_WORD)
class ImportMnemonicActivity : DILoadableActivity(), View.OnClickListener {

    companion object {
        const val TYPE_NO_LOCAL_NO_SERVICE = 1
        const val TYPE_LOCAL_NO_SERVICE = 2
        const val TYPE_LOCAL_NO_SERVICE_DEFAULT_PASSWORD = 3
        const val TYPE_NO_LOCAL_SERVICE = 4
        const val TYPE_LOCAL_SERVICE = 5


        const val CODE_SET_ENCRYPT_PASSWORD = 11
        const val CODE_FORGET_ENCRYPT_PASSWORD = 12
    }

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: ImportMnemonicViewModel

    var type = TYPE_NO_LOCAL_NO_SERVICE
    @JvmField
    @Autowired
    var canBack: Boolean = false

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_transparent), 0)
        BarUtils.setStatusBarLightMode(this, true)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_import_mnemonic_word
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.saveMnemonicWord.observe(this, Observer {
            dismiss()
            ShowUtils.showToastNormal(instance, getString(R.string.chat_back_encrypt_pwd_success))
            setResult(RESULT_OK)
            finish()
        })
        viewModel.updateSecretKeyMnemonicWord.observe(this, Observer {
            dismiss()
            ShowUtils.showToastNormal(instance, R.string.chat_update_encrypt_pwd_success)
            setResult(RESULT_OK)
            finish()
        })
        iv_back.visibility = if (canBack) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initData()
    }

    override fun initData() {
        val localMnemonic = CipherManager.getEncMnemonicString()
        val serviceMnemonic = viewModel.currentUser.value?.privateKey
        if (TextUtils.isEmpty(localMnemonic) && TextUtils.isEmpty(serviceMnemonic)) {
            //本地与服务端均无助记词，则为刚注册用户，跳转到设置密聊密码
            type = TYPE_NO_LOCAL_NO_SERVICE
        } else if (TextUtils.isEmpty(localMnemonic)) {
            //本地无助记词，服务端有，则为用户换设备登录或卸载重装,跳转到找回密聊消息
            type = TYPE_NO_LOCAL_SERVICE
        } else if (TextUtils.isEmpty(serviceMnemonic) && CipherManager.hasDHKeyPair()) {
            // 本地有助记词，服务端无，则为本次版本升级情况
            // 如果不是默认密码跳转到找回密聊消息，是默认密码跳转到设置密聊密码
            type = if (TextUtils.isEmpty(CipherManager.getMnemonicString())) {
                //如果默认密码解不开助记词，则用户设置了密聊密码
                TYPE_LOCAL_NO_SERVICE
            } else {
                TYPE_LOCAL_NO_SERVICE_DEFAULT_PASSWORD
            }
        } else if (!TextUtils.equals(localMnemonic, serviceMnemonic)) {
            //本地服务器均有助记词，则判断是否一致，不一致则跳转到找回密聊密码，本地作废
            type = TYPE_LOCAL_SERVICE
        }
        viewModel.type = type
        when(type) {
            TYPE_NO_LOCAL_NO_SERVICE, TYPE_LOCAL_NO_SERVICE_DEFAULT_PASSWORD -> {
                action_encrypt_password.setText(R.string.chat_title_set_encrypt_password)
                action_forget_password.visibility = View.GONE
            }
            TYPE_NO_LOCAL_SERVICE, TYPE_LOCAL_NO_SERVICE, TYPE_LOCAL_SERVICE -> {
                action_encrypt_password.setText(R.string.chat_back_encrypt_message)
                action_forget_password.visibility = View.VISIBLE
            }
        }
    }

    override fun setEvent() {
        action_encrypt_password.setOnClickListener(this)
        action_forget_password.setOnClickListener(this)
        iv_back.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.iv_back -> finish()
            R.id.action_encrypt_password -> {
                when(type) {
                    TYPE_NO_LOCAL_NO_SERVICE -> {
                        //跳转到设置密聊密码
                        ARouter.getInstance().build(AppRoute.ENCRYPT_PWD)
                                .withBoolean("setMode", true)
                                .navigation(instance, CODE_SET_ENCRYPT_PASSWORD)
                    }
                    TYPE_LOCAL_NO_SERVICE_DEFAULT_PASSWORD -> {
                        //跳转到设置密聊密码，且直接复用本地助记词
                        ARouter.getInstance().build(AppRoute.ENCRYPT_PWD)
                                .withBoolean("setMode", true)
                                .navigation(instance, CODE_SET_ENCRYPT_PASSWORD)
                    }
                    TYPE_NO_LOCAL_SERVICE -> {
                        CheckEncryptPasswordDialog
                                .create(viewModel.currentUser.value?.privateKey)
                                .show(supportFragmentManager, "TYPE_NO_LOCAL_SERVICE")
                    }
                    TYPE_LOCAL_NO_SERVICE -> {
                        val localMnemonic = CipherManager.getEncMnemonicString()
                        CheckEncryptPasswordDialog
                                .create(localMnemonic ?: "")
                                .show(supportFragmentManager, "TYPE_LOCAL_NO_SERVICE")
                    }
                    TYPE_LOCAL_SERVICE -> {
                        CheckEncryptPasswordDialog
                                .create(viewModel.currentUser.value?.privateKey)
                                .show(supportFragmentManager, "TYPE_LOCAL_SERVICE")
                    }
                }
            }
            R.id.action_forget_password -> {
                ARouter.getInstance().build(AppRoute.ENCRYPT_PWD)
                        .withBoolean("setMode", true)
                        .withBoolean("forget", true)
                        .navigation(instance, CODE_FORGET_ENCRYPT_PASSWORD)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CODE_SET_ENCRYPT_PASSWORD || requestCode == CODE_FORGET_ENCRYPT_PASSWORD) {
            if(resultCode == Activity.RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (canBack) {
            super.onBackPressed()
        }
    }
}