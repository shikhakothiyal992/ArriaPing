package com.arria.ping.ui.generalview

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.arria.ping.R
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.util.NetworkHelper
import com.arria.ping.model.ChallengeParamFail
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.util.IpConstants
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.Validation
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_login_activty.*
import kotlinx.android.synthetic.main.activity_setup_new_password.*
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class SetupNewPasswordActivity : BaseActivity() {

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var setPasswordApiInterface: ApiInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_new_password)

        btn_confirm.setOnClickListener {
            hideKeyBoard()
            if (checkValidation()) {
                if (networkHelper.isNetworkConnected()) {
                    callChallengeApi()
                } else {
                    Validation().showMessageToast(this, resources.getString(R.string.internet_connection))
                }
            }
        }
        edit_new_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // Not yet implemented

            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
                // Not yet implemented

            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if(s.isNotEmpty()){
                    enableDisableButton()
                }else{
                    enableDisableButton()
                }

            }
        })

        edit_confirm_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // Not yet implemented

            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
                // Not yet implemented

            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if(s.isNotEmpty()){
                    enableDisableButton()
                }else{
                    enableDisableButton()
                }

            }
        })

    }


    private fun checkValidation(): Boolean {
        var flag = true
        when {
            edit_new_password.text!!.isEmpty() -> {
                edit_new_password.error = "Please enter new password"
                edit_new_password.requestFocus()
                flag = false
            }
        }
        when {
            edit_confirm_password.text!!.isEmpty() -> {
                txt_layout_pswrd.endIconMode = TextInputLayout.END_ICON_NONE
                edit_confirm_password.error = "Please enter confirm Password"
                flag = false
            }
        }
        when {
            edit_new_password.text.toString() != edit_confirm_password.text.toString() -> {
                Validation().showMessageToast(this, resources.getString(R.string.password_not_matched))
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
            challengeParametersJson.put("newPassword", edit_confirm_password.text.toString())
            jsonObject.put("challengeParameters", challengeParametersJson)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val call = setPasswordApiInterface.changePassword(jsonObject.toString())
        call.enqueue(object : retrofit2.Callback<LoginSuccess> {
            override fun onResponse(call: retrofit2.Call<LoginSuccess>, response: retrofit2.Response<LoginSuccess>) {
                dismissProgressDialog()
                if(response.isSuccessful){
                    if (response.code() == IpConstants.RESPONSE_CODE_200) {

                        showAlert(resources.getString(R.string.change_pwd_successfully))
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ChallengeParamFail>() {
                    }.type
                    val errorResponse = gson.fromJson<ChallengeParamFail>(
                        response.errorBody()!!.charStream(), type
                    )

                    Toast.makeText(
                        this@SetupNewPasswordActivity,
                        errorResponse.message,
                        Toast.LENGTH_LONG
                    ).show()
                }


            }

            override fun onFailure(call: retrofit2.Call<LoginSuccess>, t: Throwable) {
                dismissProgressDialog()
                if(!networkHelper.isNetworkConnected()){
                    Validation().showMessageToast(
                            this@SetupNewPasswordActivity,
                            resources.getString(R.string.internet_connection)

                    )
                }
            }


        })
    }
    fun showAlert(msg:String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Ok") { dialog: DialogInterface?, _: Int ->
            dialog!!.dismiss()
            StorePrefData.token = ""
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        alertDialog.show()
    }
    private fun enableDisableButton(){
        if(edit_new_password.text!!.isNotEmpty() && edit_confirm_password.text!!.isNotEmpty()){
            btn_confirm.isEnabled = true
            btn_confirm.setBackgroundResource(R.drawable.button_border_blue)
            btn_confirm.setTextColor(getColor(R.color.header_color))

        }else{
            btn_confirm.isEnabled = false
            btn_confirm.setBackgroundResource(R.drawable.button_border)
            btn_confirm.setTextColor(getColor(R.color.dark_black_button_opacity_13))

        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideKeyBoard()
        }
        return super.dispatchTouchEvent(ev)
    }
}