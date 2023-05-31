package com.arria.ping.ui.generalview

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat
import com.arria.ping.R
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.model.forgotpassword.ForgotPassword
import com.arria.ping.model.forgotpassword.ForgotPasswordFail
import com.arria.ping.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_confirmation_code.*
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class ConfirmationCodeActivity : BaseActivity(), View.OnKeyListener {

    var email = ""
    var pinValues = ""

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var confirmationForgotApiInterface: ApiInterface
    private var confirmationDialog: Dialog? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation_code)
        init()

        if (intent.hasExtra("email")) {
            email = intent.getStringExtra("email")!!

            text_hint_confirmation_code.text = HtmlCompat.fromHtml(
                    getString(R.string.email_confirmation_text, email),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        confirm_code_heading.setOnClickListener {
            callLoginActivity()
        }

        btn_confirm.setOnClickListener {
            if (networkHelper.isNetworkConnected()) {
                navigateToSetNewPasswordScreen(pinValues)
            } else {
                showConfirmationDialog(getString(R.string.network_error_title),
                                       getString(R.string.network_error_description))
            }
        }

        text_resend_confirm_code.setOnClickListener {
            if (networkHelper.isNetworkConnected()) {
                clearEnteredVerificationPin()
                callConfirmationCodeApi()
            } else {
                showConfirmationDialog(getString(R.string.network_error_title),
                                       getString(R.string.network_error_description))
            }
        }
    }

    private fun navigateToSetNewPasswordScreen(pinValue: String) {
        val confirmationCodeActivity = Intent(
                this, NewChangePasswordAfterOtpVerifyActivity::class.java
        )
        confirmationCodeActivity.putExtra("otp_code", pinValue)
        confirmationCodeActivity.putExtra("email", email)
        startActivity(confirmationCodeActivity)
    }

    private fun callConfirmationCodeApi() {
        showProgressDialog()
        val jsonObjectConfirmationForgot = JSONObject()

        try {
            jsonObjectConfirmationForgot.put("email", email)
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val confirmationForgotCall = confirmationForgotApiInterface.forgotPassword(jsonObjectConfirmationForgot.toString())
        confirmationForgotCall.enqueue(object : retrofit2.Callback<ForgotPassword> {
            override fun onResponse(
                    call: retrofit2.Call<ForgotPassword>,
                    response: retrofit2.Response<ForgotPassword>
            ) {
                dismissProgressDialog()
                if (response.isSuccessful) {
                    if (response.code() == IpConstants.RESPONSE_CODE_201) {
                        showConfirmationDialog(getString(R.string.verification_code_dialog_title_text), resources.getString(R.string
                                                                                                                                    .verification_code_dialog_description_text))
                    }
                } else {
                    val gsonConfirmationForgot = Gson()
                    val typeConfirmationForgot = object : TypeToken<ForgotPasswordFail>() {
                    }.type
                    val errorResponseConfirmationForgot = gsonConfirmationForgot.fromJson<ForgotPasswordFail>(
                            response.errorBody()!!
                                    .charStream(), typeConfirmationForgot
                    )
                    if(IpConstants.CODE_LIMIT_EXCEED == errorResponseConfirmationForgot.code){
                        showConfirmationDialog(getString(R.string.attempt_limit_exceed_dialog_title_text),getString(R.string.attempt_limit_exceed_dialog_description_text))
                    }else{
                        if(confirmationDialog == null){
                            confirmationDialog = DialogUtil.getErrorDialogAccessDialog(
                                    this@ConfirmationCodeActivity,
                                    getString(R.string.verification_code_api_failed_text),
                                    getString(R.string.unexpected_error_text),
                                    getString(R.string.retry_text),
                                    {
                                        confirmationDialog?.dismiss()
                                        confirmationDialog = null
                                        callConfirmationCodeApi()
                                    },
                                    getString(R.string.cancel_text),
                                    {
                                        confirmationDialog?.dismiss()
                                        confirmationDialog = null
                                    }
                            )
                            confirmationDialog?.show()
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
                    showConfirmationDialog(getString(R.string.network_error_title),
                                           getString(R.string.network_error_description))
                } else {
                    if(confirmationDialog == null){
                        confirmationDialog = DialogUtil.getErrorDialogAccessDialog(
                                this@ConfirmationCodeActivity,
                                getString(R.string.verification_code_api_failed_text),
                                getString(R.string.unexpected_error_text),
                                getString(R.string.retry_text),
                                {
                                    confirmationDialog?.dismiss()
                                    confirmationDialog = null
                                    callConfirmationCodeApi()
                                },
                                getString(R.string.cancel_text),
                                {
                                    confirmationDialog?.dismiss()
                                    confirmationDialog = null
                                }
                        )
                        confirmationDialog?.show()
                    }

                }
            }


        })
    }

    override fun onKey(
            p0: View?,
            p1: Int,
            p2: KeyEvent?
    ): Boolean {
        if (p0!!.id == R.id.iv_pin2 && p1 == KeyEvent.KEYCODE_DEL && p2?.action == KeyEvent.ACTION_DOWN) {
            iv_pin2.setText("")
            iv_pin1.requestFocus()
            iv_pin1.isFocusableInTouchMode = true
        }

        if (p0.id == R.id.iv_pin3 && p1 == KeyEvent.KEYCODE_DEL && p2?.action == KeyEvent.ACTION_DOWN) {
            iv_pin3.setText("")
            iv_pin2.requestFocus()
            iv_pin2.isFocusableInTouchMode = true
        }
        if (p0.id == R.id.iv_pin4 && p1 == KeyEvent.KEYCODE_DEL && p2?.action == KeyEvent.ACTION_DOWN) {
            iv_pin4.setText("")
            iv_pin3.requestFocus()
            iv_pin3.isFocusableInTouchMode = true
        }
        if (p0.id == R.id.iv_pin5 && p1 == KeyEvent.KEYCODE_DEL && p2?.action == KeyEvent.ACTION_DOWN) {
            iv_pin5.setText("")
            iv_pin4.requestFocus()
            iv_pin4.isFocusableInTouchMode = true
        }
        if (p0.id == R.id.iv_pin6 && p1 == KeyEvent.KEYCODE_DEL && p2?.action == KeyEvent.ACTION_DOWN) {
            iv_pin6.setText("")
            iv_pin5.requestFocus()
            iv_pin5.isFocusableInTouchMode = true
        }

        if (p0.id == R.id.iv_pin1 && p1 == KeyEvent.KEYCODE_DEL && p2?.action == KeyEvent.ACTION_DOWN) {
            iv_pin1.setText("")
            iv_pin1.requestFocus()
            iv_pin1.isFocusableInTouchMode = true
        }


        if (p1 == KeyEvent.FLAG_EDITOR_ACTION) {
            println("FLAG_EDITOR_ACTION $p1")
        }

        return false
    }

    override fun onResume() {
        super.onResume()
        clearEnteredVerificationPin()
    }

    private fun init() {
        enableDisableButton()
        iv_pin1.setOnKeyListener(this)
        iv_pin2.setOnKeyListener(this)
        iv_pin3.setOnKeyListener(this)
        iv_pin4.setOnKeyListener(this)
        iv_pin5.setOnKeyListener(this)
        iv_pin6.setOnKeyListener(this)

        iv_pin1.isFocusableInTouchMode = true
        iv_pin1.requestFocus()

        iv_pin1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s!!.isNotEmpty() -> {
                        iv_pin2.isFocusableInTouchMode = true
                        iv_pin2.requestFocus()
                        iv_pin2.isCursorVisible = true
                        enableDisableButton()
                    }
                }
            }

            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                //beforeTextChanged
            }

            override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                if (s!!.length >= 2) {
                    iv_pin1.setText(
                            s.toString()
                                    .substring(s.length - 1, s.length)
                    )
                }
            }
        })

        iv_pin2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s!!.isNotEmpty() -> {
                        iv_pin3.isFocusableInTouchMode = true
                        iv_pin3.requestFocus()
                        iv_pin3.isCursorVisible = true
                        enableDisableButton()
                    }
                }
            }

            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                //beforeTextChanged
            }

            override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                if (s!!.isEmpty()) {
                    iv_pin1.requestFocus()
                    iv_pin1.isFocusableInTouchMode = true
                    iv_pin1.isCursorVisible = true
                    enableDisableButton()
                } else {
                    if (s.length >= 2) {
                        iv_pin2.setText(
                                s.toString()
                                        .substring(s.length - 1, s.length)
                        )
                    }
                }
            }
        })

        iv_pin3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s!!.isNotEmpty() -> {
                        iv_pin4.isFocusableInTouchMode = true
                        iv_pin4.requestFocus()
                        iv_pin4.isCursorVisible = true
                        enableDisableButton()
                    }
                }
            }

            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                //beforeTextChanged
            }

            override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                if (s!!.isEmpty()) {
                    iv_pin2.requestFocus()
                    iv_pin2.isFocusableInTouchMode = true
                    iv_pin2.isCursorVisible = true
                    enableDisableButton()
                } else {
                    if (s.length >= 2) {
                        iv_pin3.setText(
                                s.toString()
                                        .substring(s.length - 1, s.length)
                        )
                    }
                }
            }
        })

        iv_pin4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s!!.isNotEmpty() -> {
                        iv_pin5.isFocusableInTouchMode = true
                        iv_pin5.requestFocus()
                        iv_pin5.isCursorVisible = true
                        enableDisableButton()
                    }
                }
            }

            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                //beforeTextChanged
            }

            override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                if (s!!.isEmpty()) {
                    iv_pin3.requestFocus()
                    iv_pin3.isFocusableInTouchMode = true
                    iv_pin3.isCursorVisible = true
                    enableDisableButton()
                } else {
                    if (s.length >= 2) {
                        iv_pin4.setText(
                                s.toString()
                                        .substring(s.length - 1, s.length)
                        )
                    }
                }
            }
        })

        iv_pin5.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s!!.isNotEmpty() -> {
                        iv_pin6.isFocusableInTouchMode = true
                        iv_pin6.requestFocus()
                        enableDisableButton()
                    }
                }
            }

            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                //beforeTextChanged
            }

            override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                if (s!!.isEmpty()) {
                    iv_pin4.requestFocus()
                    iv_pin4.isFocusableInTouchMode = true
                    iv_pin4.isCursorVisible = true
                    enableDisableButton()
                } else {
                    if (s.length >= 2) {
                        iv_pin5.setText(
                                s.toString()
                                        .substring(s.length - 1, s.length)
                        )
                    }
                }

            }
        })

        iv_pin6.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s!!.isNotEmpty() -> {
                    }
                }
            }

            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                //beforeTextChanged

            }

            override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                when {
                    s!!.isNotEmpty() -> {
                        if (s.length >= 2) {
                            iv_pin6.setText(
                                    s.toString()
                                            .substring(s.length - 1, s.length)
                            )
                        }
                        enableDisableButton()
                    }
                    else -> {
                        iv_pin5.requestFocus()
                        iv_pin5.isFocusableInTouchMode = true
                        iv_pin5.isCursorVisible = true
                        enableDisableButton()

                    }
                }
            }
        })


        iv_pin1.setOnEditorActionListener {_, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_PREVIOUS -> {
                    iv_pin1.setText("")
                    true
                }
                else -> false
            }
        }

        iv_pin2.setOnEditorActionListener {_, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_PREVIOUS -> {
                    iv_pin2.setText("")
                    iv_pin1.requestFocus()
                    true
                }
                else -> false
            }
        }

        iv_pin3.setOnEditorActionListener {_, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_PREVIOUS -> {
                    iv_pin3.setText("")
                    iv_pin2.requestFocus()
                    true
                }
                else -> false
            }
        }

        iv_pin4.setOnEditorActionListener {_, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_PREVIOUS -> {
                    iv_pin4.setText("")
                    iv_pin3.requestFocus()
                    true
                }
                else -> false
            }
        }

        iv_pin5.setOnEditorActionListener {_, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_PREVIOUS -> {
                    iv_pin5.setText("")
                    iv_pin4.requestFocus()
                    true
                }
                else -> false
            }
        }
    }

    private fun enableDisableButton() {
        if (iv_pin1.text!!.isNotEmpty() && iv_pin2.text!!.isNotEmpty() && iv_pin3.text!!.isNotEmpty() && iv_pin4.text!!
                    .isNotEmpty() && iv_pin5.text!!.isNotEmpty() && iv_pin6.text!!.isNotEmpty()
        ) {
            hideKeyBoard()
            pinValues =
                    iv_pin1.text.toString() + iv_pin2.text.toString() + iv_pin3.text.toString() + iv_pin4.text.toString() + iv_pin5.text.toString() + iv_pin6.text.toString()

            btn_confirm.isEnabled = true
            btn_confirm.setBackgroundResource(R.drawable.button_border_blue)
            btn_confirm.setTextColor(getColor(R.color.header_color))
        } else {
            btn_confirm.isEnabled = false
            btn_confirm.setBackgroundResource(R.drawable.button_border)
            btn_confirm.setTextColor(getColor(R.color.dark_black_button_opacity_13))
        }
    }

    private fun callLoginActivity() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun showConfirmationDialog(
            title: String,
            description: String
    ) {
        confirmationDialog = DialogUtil.getErrorDialogAccessDialog(
                this,
                title,
                description,
                getString(R.string.ok_text),
                {
                    confirmationDialog?.dismiss()
                    confirmationDialog = null
                },
                null,
                null
        )
        confirmationDialog?.show()
    }

    fun clearEnteredVerificationPin(){
        iv_pin1.setText("")
        iv_pin2.setText("")
        iv_pin3.setText("")
        iv_pin4.setText("")
        iv_pin5.setText("")
        iv_pin6.setText("")
    }


}