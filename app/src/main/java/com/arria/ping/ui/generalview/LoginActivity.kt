package com.arria.ping.ui.generalview

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import com.arria.ping.R
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.database.*
import com.arria.ping.log.awsCognitoAccessProvider
import com.arria.ping.util.NetworkHelper
import com.arria.ping.model.login.LoginRequest
import com.arria.ping.model.responsehandlers.Status
import com.arria.ping.util.*
import com.arria.ping.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_change_paswword_after_otp_verify.*
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_login_activty.*
import kotlinx.android.synthetic.main.activity_welcome_screen.*
import kotlinx.android.synthetic.main.dialog_layout_with_single_button.*
import kotlinx.android.synthetic.main.two_button_dialog_layout.*
import java.security.*
import javax.crypto.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    @Inject
    lateinit var apiInterface: ApiInterface

    @Inject
    lateinit var networkHelper: NetworkHelper
    private val loginViewModel by viewModels<LoginViewModel>()
    private var dialog: Dialog? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_activty)
        enableDisableButton()
        setObserver()
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)) {

            BiometricManager.BIOMETRIC_SUCCESS -> {
                StorePrefData.isDeviceHasBiometricFeatures = true
            }
        }

        if(StorePrefData.isFromWelcomeScreen && StorePrefData.isTouchIDEnabled){
            back_button.visibility = View.VISIBLE
            StorePrefData.isFromWelcomeScreen = false
        }else{
            back_button.visibility = View.GONE
        }

        btn_login.setOnClickListener {
            hideKeyBoard()
            if (checkValidation()) {
                if (networkHelper.isNetworkConnected()) {
                    StorePrefData.email = edit_email.text.toString()
                    loginViewModel.getLogin(LoginRequest(edit_email.text.toString(), edit_password.text.toString()))
                } else {
                    dialog = DialogUtil.getErrorDialogAccessDialog(
                            this,
                            getString(R.string.network_error_title),
                            getString(R.string.network_error_description),
                            getString(R.string.ok_text),
                            {
                                dialog?.dismiss()
                                dialog = null

                            },
                            null,
                            null
                    )
                    dialog?.show()
                }
            }
        }

        edit_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // Not yet implemented

            }

            override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
            ) {
                // Not yet implemented

            }

            override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int,
            ) {
                if (s.isNotEmpty()) {
                    enableDisableButton()
                } else {
                    enableDisableButton()
                }

            }
        })

        edit_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

                if (checkValidation()) {
                    edit_email.setBackgroundResource(R.drawable.edit_text_border)
                    login_error_layout.visibility = View.GONE
                }

            }

            override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
            ) {
                // Not yet implemented

            }

            override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int,
            ) {
                if (s.isNotEmpty()) {
                    enableDisableButton()
                } else {
                    enableDisableButton()
                }

            }
        })

        txt_forget_password.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        edit_email.setText(StorePrefData.email)

        back_button.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

    }

    private fun checkValidation(): Boolean {
        var flag = true
        if (edit_email.text!!.isNotEmpty() && !Validation().isEmailValid(edit_email.text.toString())) {
            edit_email.requestFocus()
            edit_email.setBackgroundResource(R.drawable.edit_text_red_border)
            login_error_layout.visibility = View.VISIBLE
            flag = false
        }
        return flag
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setObserver() {
        loginViewModel.loginMutableLiveData.observe(this, {
            when (it.status) {
                Status.LOADING -> showProgressDialog()
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    if (it.data!!.challengeName == IpConstants.NEW_PASSWORD_REQUIRED) {
                        navigateToPasswordScreen(
                                it.data.session.toString(), it.data.challengeParameters.userIdForSRP
                                .toString()
                        )
                    } else {
                        StorePrefData.encryptedPassword = EncryptionAndDecryptionUtil.encrypt(edit_password.text.toString())
                                .toString()

                        StorePrefData.token = it.data.authenticationResult?.accessToken.toString()
                        StorePrefData.refreshToken = it.data.authenticationResult?.refreshToken.toString()
                        StorePrefData.iDToken = it.data.authenticationResult?.idToken.toString()
                        awsCognitoAccessProvider(this,StorePrefData.iDToken)
                        getUserProfile()
                    }
                }
                Status.ERROR -> {
                    dismissProgressDialog()
                    if (it.code == IpConstants.OFFLINE_ERROR_CODE) {
                        dialog = DialogUtil.getErrorDialogAccessDialog(
                                this@LoginActivity,
                                getString(R.string.network_error_title),
                                getString(R.string.network_error_description),
                                getString(R.string.ok_text),
                                {
                                    dialog?.dismiss()
                                    dialog = null

                                },
                                null,
                                null
                        )
                        dialog?.show()
                    } else if(it.code == IpConstants.ERROR_CODE_400){
                        dialog = DialogUtil.getErrorDialogAccessDialog(
                                this@LoginActivity,
                                getString(R.string.login_fail_text),
                                it.message,
                                getString(R.string.ok_text),
                                {
                                    dialog?.dismiss()
                                    dialog = null

                                },
                                null,
                                null
                        )
                        dialog?.show()
                    }
                    else {
                        dialog = DialogUtil.getErrorDialogAccessDialog(
                                this@LoginActivity,
                                getString(R.string.login_api_failed_text),
                                getString(R.string.unexpected_error_text),
                                getString(R.string.retry_text),
                                {
                                    dialog?.dismiss()
                                    dialog = null
                                    loginViewModel.getLogin(LoginRequest(edit_email.text.toString(), edit_password.text.toString()))
                                },
                                getString(R.string.cancel_text),
                                {
                                    dialog?.dismiss()
                                    dialog = null

                                }
                        )
                        dialog?.show()
                    }

                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        edit_email.setBackgroundResource(R.drawable.edit_text_border)
        login_error_layout.visibility = View.GONE
    }

    fun navigateToPasswordScreen(
            session: String,
            userIdForSRP: String
    ) {
        dismissProgressDialog()
        StorePrefData.Session = session
        StorePrefData.USER_ID_FOR_SRP = userIdForSRP
        startActivity(Intent(this, NewPasswordActivity::class.java))
    }

    fun enableDisableButton() {
        if (edit_email.text!!.isNotEmpty() && edit_password.text!!.isNotEmpty()) {
            btn_login.isEnabled = true
            btn_login.setBackgroundResource(R.drawable.button_border_blue)
            btn_login.setTextColor(getColor(R.color.header_color))
        } else {
            btn_login.isEnabled = false
            btn_login.setBackgroundResource(R.drawable.button_border)
            btn_login.setTextColor(getColor(R.color.dark_black_button_opacity_13))
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideKeyBoard()
        }
        return super.dispatchTouchEvent(ev)
    }
}