package com.arria.ping.ui.generalview

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.arria.ping.R
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.model.forgotpassword.ForgotPassword
import com.arria.ping.model.forgotpassword.ForgotPasswordFail
import com.arria.ping.model.login.LoginRequest
import com.arria.ping.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_confirmation_code.*
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_new_change_password_after_otp_verify.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class NewChangePasswordAfterOtpVerifyActivity : BaseActivity() {

    private var otpCode = ""
    var email = ""
    var confirmedPassword = ""

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var otpVerifyApiInterface: ApiInterface
    private var changePasswordDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_new_change_password_after_otp_verify)
        if (intent.hasExtra("otp_code")) {
            otpCode = intent.getStringExtra("otp_code")!!
        }
        if (intent.hasExtra("email")) {
            email = intent.getStringExtra("email")!!
        }
        val upperCaseRegex = ".*[A-Z].*"
        val numberRegex = ".*[0-9].*"
        val lengthRegex = ".{8,}"

        val upperCasePatter: Pattern = Pattern.compile(upperCaseRegex)
        val numberPatter: Pattern = Pattern.compile(numberRegex)
        val lengthPatter: Pattern = Pattern.compile(lengthRegex)

        confirm_verify_code_heading.setOnClickListener {
            navigateToLoginScreen()
        }
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


            }
        })
        edit_verify_confirm_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                verifyPasswordFields()
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


            }
        })


        btn_verify_confirm_new_password.setOnClickListener {
            hideKeyBoard()
            if (checkValidation()) {
                if (networkHelper.isNetworkConnected()) {
                    callChangePasswordApi()
                } else {
                    showErrorDialog(getString(R.string.network_error_title),
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
            confirmedPassword = edit_verify_confirm_password.text.toString()
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
            edit_verify_new_password.text!!.length < 8 -> {
                flag = false
            }
        }
        when {
            !upperCasePatter.matcher(
                    edit_verify_new_password.text.toString()
                            .trim()
            )
                    .matches() -> {
                flag = false
            }

        }
        when {
            !numberPatter.matcher(
                    edit_verify_new_password.text.toString()
                            .trim()
            )
                    .matches() -> {
                flag = false
            }

        }
        when {
            edit_verify_new_password.text!!.length < 8 -> {
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

    private fun callChangePasswordApi() {
        showProgressDialog()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("confirmationCode", otpCode)
            jsonObject.put("email", email)
            jsonObject.put("password", confirmedPassword)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val call = otpVerifyApiInterface.forgotConfirmPassword(jsonObject.toString())
        call.enqueue(object : retrofit2.Callback<ForgotPassword> {
            override fun onResponse(
                    call: retrofit2.Call<ForgotPassword>,
                    response: retrofit2.Response<ForgotPassword>,
            ) {
                dismissProgressDialog()
                if (response.isSuccessful) {
                    println("Token----" + response.body()!!)
                    if (response.code() == IpConstants.RESPONSE_CODE_201) {
                        showSuccessDialog(getString(R.string.success_text),
                                          getString(R.string.password_change_succesfull))
                    }
                } else {
                    val gson = Gson()
                    val type = object : TypeToken<ForgotPasswordFail>() {
                    }.type
                    val errorResponse = gson.fromJson<ForgotPasswordFail>(
                            response.errorBody()!!
                                    .charStream(), type
                    )

                    when (errorResponse.code) {
                        IpConstants.CODE_MISMATCH -> {
                            showErrorDialog(getString(R.string.invalid_verification_code_dialog_title_text), getString(R.string.invalid_verification_code_dialog_description_text),errorResponse.code)
                        }
                        IpConstants.CODE_EXPIRED -> {
                            showErrorDialog(getString(R.string.session_expired_dialog_title_text), getString(R.string.session_expired_dialog_description_text),errorResponse.code)
                        }
                        else -> {
                            showChangePasswordErrorDialog()
                        }
                    }
                }
            }

            override fun onFailure(
                    call: retrofit2.Call<ForgotPassword>,
                    t: Throwable
            ) {
                dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    showErrorDialog(getString(R.string.network_error_title),
                                    getString(R.string.network_error_description),IpConstants.OFFLINE_ERROR_CODE.toString())
                }else{
                    showChangePasswordErrorDialog()
                }
            }
        })
    }
    fun showSuccessDialog(title: String, description: String) {
        changePasswordDialog = DialogUtil.getErrorDialogAccessDialog(
                this,
                title,
                description,
                getString(R.string.ok_text),
                {
                    changePasswordDialog?.dismiss()
                    changePasswordDialog = null
                    navigateToLoginScreen()
                },
                null,
                null
        )
        changePasswordDialog?.show()

    }


    fun showErrorDialog(title: String, description: String, errorCode: String) {
        changePasswordDialog = DialogUtil.getErrorDialogAccessDialog(
                this,
                title,
                description,
                getString(R.string.ok_text),
                {
                    changePasswordDialog?.dismiss()
                    changePasswordDialog = null
                    if(IpConstants.CODE_EXPIRED == errorCode || IpConstants.CODE_LIMIT_EXCEED == errorCode){
                        navigateToLoginScreen()
                    }else if(IpConstants.CODE_MISMATCH == errorCode){
                        navigateToVerificationCodeScreen()
                    }

                },
                null,
                null
        )
        changePasswordDialog?.show()

    }



    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun navigateToVerificationCodeScreen() {
        val intent = Intent(this, ConfirmationCodeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    fun showChangePasswordErrorDialog(){
        if(changePasswordDialog == null){
            changePasswordDialog = DialogUtil.getErrorDialogAccessDialog(
                    this@NewChangePasswordAfterOtpVerifyActivity,
                    getString(R.string.reset_password_api_failed_text),
                    getString(R.string.unexpected_error_text),
                    getString(R.string.retry_text),
                    {
                        changePasswordDialog?.dismiss()
                        changePasswordDialog = null
                        callChangePasswordApi()
                    },
                    getString(R.string.cancel_text),
                    {
                        changePasswordDialog?.dismiss()
                        changePasswordDialog = null
                    }
            )
            changePasswordDialog?.show()

        }    }
}