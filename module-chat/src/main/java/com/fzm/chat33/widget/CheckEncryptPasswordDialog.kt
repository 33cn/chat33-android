package com.fzm.chat33.widget

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.base.DIDialogFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.main.mvvm.ImportMnemonicViewModel
import javax.inject.Inject

class CheckEncryptPasswordDialog : DIDialogFragment() {

    private lateinit var etPassword: EditText
    private lateinit var encMnemonic: String

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: ImportMnemonicViewModel

    companion object {
        @JvmStatic
        fun create(encMnemonic: String?): CheckEncryptPasswordDialog {
            return CheckEncryptPasswordDialog().apply {
                arguments = Bundle().apply {
                    putString("encMnemonic", encMnemonic)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encMnemonic = arguments?.getString("encMnemonic") ?: ""
        viewModel = findViewModel(provider)

        viewModel.checkError.observe(this, Observer {
            if (it == 0) {
                dismiss()
            } else {
                ShowUtils.showToastNormal(context, it)
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(R.color.chat_transparent)
        val lp = dialog.window?.attributes
        lp?.gravity = Gravity.CENTER
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = lp
        val container = requireActivity().layoutInflater.inflate(R.layout.dialog_verify_encrypt_password, null)
        container.setBackgroundResource(R.drawable.bg_dialog)
        dialog.window?.setContentView(container)
        etPassword = container.findViewById(R.id.et_password)
        container.findViewById<View>(R.id.confirm).setOnClickListener {
            val password = etPassword.text.toString().trim()
            if (password.length < 8 || password.length > 16) {
                ShowUtils.showToastNormal(context, R.string.chat_error_encrypt_password_length)
                return@setOnClickListener
            }
            viewModel.checkPassword(password, encMnemonic)
        }
        container.findViewById<View>(R.id.iv_close).setOnClickListener {
            dismiss()
        }
        dialog.setOnShowListener {
            etPassword.postDelayed({
                KeyboardUtils.showKeyboard(etPassword)
            }, 100)
        }
        return dialog
    }
}