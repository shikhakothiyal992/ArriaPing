package com.arria.ping.ui.generalview

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.RequiresApi
import com.arria.ping.R
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.util.NetworkHelper
import com.arria.ping.model.forgotpassword.ForgotPassword
import com.arria.ping.model.forgotpassword.ForgotPasswordFail
import com.arria.ping.util.IpConstants
import com.arria.ping.util.Validation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.arria.ping.util.DialogUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_forgot_password.*
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ForgotPasswordActivity : BaseActivity() {

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var forgotPasswordApiInterface: ApiInterface

    private var forgotPasswordDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        enableDisableButton()
        btn_send_email.setOnClickListener {
            hideKeyBoard()
            if (checkValidation()) {
                if (networkHelper.isNetworkConnected()) {
                    callForgotPasswordApi()
                } else {
                    showForgotScreenDialog(getString(R.string.network_error_title),
                                           getString(R.string.network_error_description))
                }
            }

        }

        edit_forgot_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (checkValidation()) {
                    edit_forgot_email.setBackgroundResource(R.drawable.edit_text_border)
                    error_layout.visibility = View.GONE
                }

            }

            override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                // Not yet implemented

            }

            override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                if (s.isNotEmpty()) {
                    enableDisableButton()
                } else {
                    enableDisableButton()
                }

            }
        })

        cancel_image_button.setOnClickListener {
            hideKeyBoard()
            finish()}

    }

    override fun onResume() {
        super.onResume()
        edit_forgot_email.setBackgroundResource(R.drawable.edit_text_border)
        error_layout.visibility = View.INVISIBLE

    }

    private fun checkValidation(): Boolean {
        var flag = true
        if (edit_forgot_email.text!!.isNotEmpty() && !Validation().isEmailValid(edit_forgot_email.text.toString())) {
            edit_forgot_email.requestFocus()
            edit_forgot_email.setBackgroundResource(R.drawable.edit_text_red_border)
            error_layout.visibility = View.VISIBLE
            flag = false
        }
        return flag
    }


    private fun callForgotPasswordApi() {
        showProgressDialog()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("email", edit_forgot_email.text.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val call = forgotPasswordApiInterface.forgotPassword(jsonObject.toString())
        call.enqueue(object : retrofit2.Callback<ForgotPassword> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                    call: retrofit2.Call<ForgotPassword>,
                    response: retrofit2.Response<ForgotPassword>
            ) {
                dismissProgressDialog()
                if (response.isSuccessful) {
                    if (response.code() == IpConstants.RESPONSE_CODE_201) {
                        showVerificationCodeView()
                    }
                } else {
                    val gson = Gson()
                    val type = object : TypeToken<ForgotPasswordFail>() {
                    }.type
                    val errorResponse = gson.fromJson<ForgotPasswordFail>(
                            response.errorBody()!!
                                    .charStream(), type
                    )

                    if(IpConstants.CODE_LIMIT_EXCEED == errorResponse.code){
                        showForgotScreenDialog(getString(R.string.attempt_limit_exceed_dialog_title_text), getString(R.string.attempt_limit_exceed_dialog_description_text))
                    }else{
                        showForgotScreenDialog(getString(R.string.forgot_password_api_failed_text),
                                               getString(R.string.unexpected_error_text))
                    }

                }


            }

            override fun onFailure(
                    call: retrofit2.Call<ForgotPassword>,
                    t: Throwable
            ) {
                dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    showForgotScreenDialog(getString(R.string.network_error_title), getString(R.string.network_error_description))
                }else{
                    if(forgotPasswordDialog == null){
                        forgotPasswordDialog = DialogUtil.getErrorDialogAccessDialog(
                                this@ForgotPasswordActivity,
                                getString(R.string.forgot_password_api_failed_text),
                                getString(R.string.unexpected_error_text),
                                getString(R.string.retry_text),
                                {
                                    forgotPasswordDialog?.dismiss()
                                    forgotPasswordDialog = null
                                    callForgotPasswordApi()
                                },
                                getString(R.string.cancel_text),
                                {
                                    forgotPasswordDialog?.dismiss()
                                    forgotPasswordDialog = null
                                }
                        )
                        forgotPasswordDialog?.show()
                    }
                }
            }


        })
    }

    private fun showVerificationCodeView() {
        val confirmationCodeActivity = Intent(this, ConfirmationCodeActivity::class.java)
        confirmationCodeActivity.putExtra("email", edit_forgot_email.text.toString())
        startActivity(confirmationCodeActivity)
    }


    private fun enableDisableButton() {
        if (edit_forgot_email.text!!.isNotEmpty() && checkValidation()) {
            btn_send_email.isEnabled = true
            btn_send_email.setBackgroundResource(R.drawable.button_border_blue)
            btn_send_email.setTextColor(getColor(R.color.header_color))
        } else {
            btn_send_email.isEnabled = false
            btn_send_email.setBackgroundResource(R.drawable.button_border)
            btn_send_email.setTextColor(getColor(R.color.dark_black_button_opacity_13))
        }
    }

    fun showForgotScreenDialog(
            title: String,
            description: String,
    ) {
        forgotPasswordDialog = DialogUtil.getErrorDialogAccessDialog(
                this,
                title,
                description,
                getString(R.string.ok_text),
                {
                    forgotPasswordDialog?.dismiss()
                    forgotPasswordDialog = null
                },
                null,
                null
        )
        forgotPasswordDialog?.show()
    }


}