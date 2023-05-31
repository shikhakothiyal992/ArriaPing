package com.arria.ping.ui.generalview

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.arria.ping.R
import com.arria.ping.log.awsCognitoAccessProvider
import com.arria.ping.model.login.LoginRequest
import com.arria.ping.model.responsehandlers.Status
import com.arria.ping.util.*
import com.arria.ping.util.IpConstants.KEY_NAME
import com.arria.ping.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login_activty.*
import kotlinx.android.synthetic.main.activity_welcome_screen.*

class WelcomeActivity : BaseActivity() {

    private val welcomeViewModel by viewModels<LoginViewModel>()
    private var dialog: Dialog? = null
    private lateinit var biometricPrompt: BiometricPrompt

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)
        showBiometricPrompt()
        welcome_btn.setOnClickListener {
            navigateToLoginScreen()
        }
        welcome_fingerprint_btn.setOnClickListener {
            showBiometricPrompt()
        }
        setObserver()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setObserver() {
        welcomeViewModel.loginMutableLiveData.observe(this, {
            when (it.status) {
                Status.LOADING -> showProgressDialog()

                Status.SUCCESS -> {
                    dismissProgressDialog()
                    if (it.data!!.authenticationResult !=null) {
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
                    } else if (it.code == IpConstants.ERROR_CODE_400) {
                        dialog = DialogUtil.getErrorDialogAccessDialog(
                                this,
                                getString(R.string.login_fail_text),
                                getString(R.string.login_fail_description_text),
                                getString(R.string.ok_text),
                                {
                                    dialog?.dismiss()
                                    dialog = null

                                },
                                null,
                                null
                        )
                        dialog?.show()
                    } else{
                        dialog = DialogUtil.getErrorDialogAccessDialog(
                                this@WelcomeActivity,
                                getString(R.string.login_api_failed_text),
                                getString(R.string.unexpected_error_text),
                                getString(R.string.retry_text),
                                {
                                    dialog?.dismiss()
                                    dialog = null
                                    welcomeViewModel.getLogin(
                                            LoginRequest(
                                                    StorePrefData.email,
                                                    EncryptionAndDecryptionUtil.decrypt(StorePrefData.encryptedPassword)
                                            )
                                    )
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

    fun navigateToLoginScreen(){
        StorePrefData.isFromWelcomeScreen = true
        startActivity(Intent(this, LoginActivity::class.java))
    }

    fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
                                          object : BiometricPrompt.AuthenticationCallback() {
                                              override fun onAuthenticationError(
                                                      errorCode: Int,
                                                      errString: CharSequence,
                                              ) {
                                                  if(errorCode == 13){
                                                      navigateToLoginScreen()
                                                  }
                                                  super.onAuthenticationError(errorCode, errString)
                                                  Log.e("onAuthenticationError", "$errString")
                                              }

                                              override fun onAuthenticationSucceeded(
                                                      result: BiometricPrompt.AuthenticationResult,
                                              ) {
                                                  super.onAuthenticationSucceeded(result)
                                                  welcomeViewModel.getLogin(
                                                          LoginRequest(
                                                                  StorePrefData.email,
                                                                  EncryptionAndDecryptionUtil.decrypt(StorePrefData.encryptedPassword)
                                                          )
                                                  )
                                              }

                                              override fun onAuthenticationFailed() {
                                                  super.onAuthenticationFailed()
                                                  Log.e("onAuthenticationFailed", "onAuthenticationFailed")
                                              }
                                          })

        val cipher = CryptoManagerUtil.getInitializedCipherForEncryption(KEY_NAME)
        val promptInfo = DialogUtil.createPromptInfo(StorePrefData.firstName)
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))

    }

}