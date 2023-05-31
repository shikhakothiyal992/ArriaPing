package com.arria.ping.ui.generalview

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.arria.ping.R
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.log.awsCognitoAccessProvider
import com.arria.ping.model.ChallengeParamFail
import com.arria.ping.model.login.LoginRequest
import com.arria.ping.model.responsehandlers.Status
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.util.*
import com.arria.ping.viewmodel.LoginViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_new_change_password_after_otp_verify.*
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class NewPasswordActivity : BaseActivity() {

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var passwordApiInterface: ApiInterface
    private val loginViewModel by viewModels<LoginViewModel>()
    private var newPasswordDialog: Dialog? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_change_password_after_otp_verify)
        val upperCaseRegex = ".*[A-Z].*"
        val numberRegex = ".*[0-9].*"
        val lengthRegex = ".{8,}"

        val upperCasePatter: Pattern = Pattern.compile(upperCaseRegex)
        val numberPatter: Pattern = Pattern.compile(numberRegex)
        val lengthPatter: Pattern = Pattern.compile(lengthRegex)
        setObserver()

        confirm_verify_code_heading.visibility = View.INVISIBLE

        edit_verify_new_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

                val upperCaseResult = upperCasePatter.matcher(s.toString())
                val numberResult = numberPatter.matcher(s.toString())
                val lengthResult = lengthPatter.matcher(s.toString())

                if (s.isNotEmpty()) {
                    if (upperCaseResult.matches()) {
                        at_least_one_uppercase_text.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_green_check_circle,
                                0,
                                0,
                                0
                        )
                    } else {
                        at_least_one_uppercase_text.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_gray_check_circle,
                                0,
                                0,
                                0
                        )
                    }
                    if (numberResult.matches()) {
                        at_least_one_number_text.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_green_check_circle,
                                0,
                                0,
                                0
                        )
                    } else {
                        at_least_one_number_text.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_gray_check_circle,
                                0,
                                0,
                                0
                        )
                    }
                    if (lengthResult.matches()) {
                        at_least_eight_characters_text.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_green_check_circle,
                                0,
                                0,
                                0
                        )
                    } else {
                        at_least_eight_characters_text.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_gray_check_circle,
                                0,
                                0,
                                0
                        )
                    }

                    if (edit_verify_confirm_password.text!!.isNotEmpty()) {
                        verifyPasswordFields()
                    }

                } else {
                    at_least_eight_characters_text.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_gray_check_circle,
                            0,
                            0,
                            0
                    )

                    at_least_one_uppercase_text.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_gray_check_circle,
                            0,
                            0,
                            0
                    )
                    at_least_one_number_text.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_gray_check_circle,
                            0,
                            0,
                            0
                    )
                }
            }

            override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int,
            ) {
                // Not yet implemented

            }

            override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int,
            ) {


            }
        })
        edit_verify_confirm_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                verifyPasswordFields()
            }

            override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int,
            ) {
                // Not yet implemented

            }

            override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int,
            ) {


            }
        })


        btn_verify_confirm_new_password.setOnClickListener{
            hideKeyBoard()
            if (checkValidation()) {
                if (networkHelper.isNetworkConnected()) {
                    callChallengeApi()
                } else {
                    showNewPasswordDialog(getString(R.string.network_error_title),
                                          getString(R.string.network_error_description),IpConstants.OFFLINE_ERROR_CODE.toString())
                }
            }
        }
    }

    fun verifyPasswordFields(){
        if (edit_verify_new_password.text.toString() != edit_verify_confirm_password.text.toString()) {
            txt_verify_layout_confirm_password.setBackgroundResource(R.drawable.edit_text_red_border)
            confirm_verify_pass_error_layout.visibility = View.VISIBLE
            enableDisableButton()
        } else {
            txt_verify_layout_confirm_password.setBackgroundResource(R.drawable.edit_text_border)
            confirm_verify_pass_error_layout.visibility = View.GONE
            enableDisableButton()
        }
    }
    private fun enableDisableButton() {
        if (edit_verify_new_password.text!!.isNotEmpty() && edit_verify_confirm_password.text!!.isNotEmpty() &&
            (edit_verify_new_password.text.toString() == edit_verify_confirm_password.text.toString())
        ) {
            btn_verify_confirm_new_password.isEnabled = true
            btn_verify_confirm_new_password.setBackgroundResource(R.drawable.button_border_blue)
            btn_verify_confirm_new_password.setTextColor(getColor(R.color.header_color))
        } else {
            btn_verify_confirm_new_password.isEnabled = false
            btn_verify_confirm_new_password.setBackgroundResource(R.drawable.button_border)
            btn_verify_confirm_new_password.setTextColor(getColor(R.color.dark_black_button_opacity_13))
        }
    }



    private fun checkValidation(): Boolean {
        var flag = true
        val upperCaseRegex = ".*[A-Z].*"
        val numberRegex = ".*[0-9].*"
        val numberPatter: Pattern = Pattern.compile(numberRegex)
        val upperCasePatter: Pattern = Pattern.compile(upperCaseRegex)
        when {
            edit_verify_new_password.text!!.isEmpty() -> {

                edit_verify_new_password.requestFocus()
                flag = false
            }
        }
        when {
            edit_verify_confirm_password.text!!.isEmpty() -> {
                flag = false
            }
        }
        when {
            edit_verify_new_password.text!!.length<8 -> {
                flag = false
            }
        }
        when {
            !upperCasePatter.matcher(edit_verify_new_password.text.toString().trim()).matches() -> {
                flag = false
            }

        }
        when {
            !numberPatter.matcher(edit_verify_new_password.text.toString().trim()).matches() -> {
                flag = false
            }

        }
        when {
            edit_verify_new_password.text!!.length<8 -> {
                flag = false
            }
        }
        when {
            edit_verify_new_password.text.toString() != edit_verify_confirm_password.text.toString() -> {
                flag = false
            }
        }
        return flag
    }

    private fun callChallengeApi() {
        showProgressDialog()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("session", StorePrefData.Session)
            jsonObject.put("challengeName","NEW_PASSWORD_REQUIRED")
            val challengeParametersJson = JSONObject()
            challengeParametersJson.put("userId", StorePrefData.USER_ID_FOR_SRP)
            challengeParametersJson.put("newPassword", edit_verify_confirm_password.text.toString())
            jsonObject.put("challengeParameters", challengeParametersJson)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val call = passwordApiInterface.changePassword(jsonObject.toString())
        call.enqueue(object : retrofit2.Callback<LoginSuccess> {
            override fun onResponse(call: retrofit2.Call<LoginSuccess>, response: retrofit2.Response<LoginSuccess>) {
                dismissProgressDialog()
                if(response.isSuccessful){
                    if (response.code() == IpConstants.RESPONSE_CODE_200) {
                        loginViewModel.getLogin(LoginRequest(StorePrefData.email, edit_verify_new_password.text.toString()))
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ChallengeParamFail>() {
                    }.type
                    val errorResponse = gson.fromJson<ChallengeParamFail>(
                            response.errorBody()!!.charStream(), type
                    )
                    when (errorResponse.code) {
                        IpConstants.CODE_MISMATCH -> {
                            showNewPasswordDialog(getString(R.string.invalid_verification_code_dialog_title_text), getString(R.string.invalid_verification_code_dialog_description_text),errorResponse.code)
                        }
                        IpConstants.CODE_EXPIRED -> {
                            showNewPasswordDialog(getString(R.string.session_expired_dialog_title_text), getString(R.string.session_expired_dialog_description_text),errorResponse.code)
                        }
                        IpConstants.SESSION_EXPIRED -> {
                            showNewPasswordDialog(getString(R.string.session_expired_dialog_title_text), getString(R.string.session_expired_dialog_description_text),errorResponse.code)
                        }
                        else -> {
                            showErrorDialog()
                        }
                    }

                }
            }

            override fun onFailure(call: retrofit2.Call<LoginSuccess>, t: Throwable) {
                dismissProgressDialog()
                if(!networkHelper.isNetworkConnected()){
                    showNewPasswordDialog(getString(R.string.network_error_title),
                                          getString(R.string.network_error_description),IpConstants.OFFLINE_ERROR_CODE.toString())

                } else {
                    showErrorDialog()
                }
            }


        })
    }

    fun showNewPasswordDialog(title: String, description: String,  errorCode: String) {
        newPasswordDialog = DialogUtil.getErrorDialogAccessDialog(
                this,
                title,
                description,
                getString(R.string.ok_text),
                {
                    if(errorCode != IpConstants.OFFLINE_ERROR_CODE.toString()){
                        newPasswordDialog?.dismiss()
                        newPasswordDialog = null
                        StorePrefData.token = ""
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }else{
                        newPasswordDialog?.dismiss()
                        newPasswordDialog = null
                    }

                },
                null,
                null
        )
        newPasswordDialog?.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setObserver() {
        loginViewModel.loginMutableLiveData.observe(this, {
            when (it.status) {
                Status.LOADING -> showProgressDialog()
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    if (it.data!!.authenticationResult !=null) {
                        StorePrefData.encryptedPassword = EncryptionAndDecryptionUtil.encrypt(edit_verify_confirm_password.text.toString())
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
                        showNewPasswordDialog(getString(R.string.network_error_title),
                                              getString(R.string.network_error_description),IpConstants.OFFLINE_ERROR_CODE.toString())
                    }else{
                        showErrorDialog()
                    }
                }
            }
        })
    }

    fun showErrorDialog(){
        if(newPasswordDialog == null){
            newPasswordDialog = DialogUtil.getErrorDialogAccessDialog(
                    this@NewPasswordActivity,
                    getString(R.string.reset_password_api_failed_text),
                    getString(R.string.unexpected_error_text),
                    getString(R.string.retry_text),
                    {
                        newPasswordDialog?.dismiss()
                        newPasswordDialog = null
                        loginViewModel.getLogin(LoginRequest(StorePrefData.email, edit_verify_new_password.text.toString()))
                    },
                    getString(R.string.cancel_text),
                    {
                        newPasswordDialog?.dismiss()
                        newPasswordDialog = null
                    }
            )
            newPasswordDialog?.show()
        }
    }
}