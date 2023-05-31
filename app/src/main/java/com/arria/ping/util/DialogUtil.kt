package com.arria.ping.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import com.arria.ping.R
import kotlinx.android.synthetic.main.dialog_layout_with_single_button.*

object DialogUtil {

    fun getErrorDialogAccessDialog(context: Context?, title: String?, message: String?, btnPositiveText: String?,
                                   clickListener: View.OnClickListener?, btnNegativeText: String?, negativeListener: View.OnClickListener?): Dialog? {
        if (context == null) return null
        val dialog = Dialog(context)
        if (dialog.window != null) {
            dialog.window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        dialog.setContentView(R.layout.two_button_dialog_layout)

        val tvTitle = dialog.findViewById<TextView>(R.id.title_text)
        val dividerView = dialog.findViewById<View>(R.id.btn_divider_view)
        if (title == null || TextUtils.isEmpty(title)) {
            tvTitle.visibility = View.GONE
        } else {
            tvTitle.text = title
        }

        val tvMsg = dialog.findViewById<TextView>(R.id.description_text)
        if (message == null || TextUtils.isEmpty(message)) {
            tvMsg.visibility = View.GONE
        } else {
            tvMsg.text = message
        }

        val btnPositive = dialog.findViewById<TextView>(R.id.btn_ok_text)
        if (btnPositiveText.isNullOrEmpty()) {
            btnPositive.visibility = View.GONE
        } else {
            btnPositive.text = btnPositiveText
            if (null != clickListener) {
                btnPositive.setOnClickListener(clickListener)
            }
        }
        val btnNegative = dialog.findViewById<TextView>(R.id.btn_cancel_text)
        if (btnNegativeText.isNullOrEmpty()) {
            btnNegative.visibility = View.GONE
            dividerView.visibility = View.GONE
        } else {
            btnNegative.text = btnNegativeText
            if (null != negativeListener) {
                btnNegative.setOnClickListener(negativeListener)
            }
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    fun showSingleButtonAlertDialog(context: Context?, title: String?, message: String?, btnText: String?, clickListener:
    View
    .OnClickListener): Dialog? {
        if (context == null) return null
        val dialog = Dialog(context)
        if (dialog.window != null) {
            dialog.window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, 3)
        }
        dialog.setContentView(R.layout.two_button_dialog_layout)

        val tvTitle = dialog.findViewById<TextView>(R.id.title_text)
        if (title == null || TextUtils.isEmpty(title)) {
            tvTitle.visibility = View.GONE
        } else {
            tvTitle.text = title
        }
        val tvMsg = dialog.findViewById<TextView>(R.id.description_text)
        if (message == null || TextUtils.isEmpty(message)) {
            tvMsg.visibility = View.GONE
        } else {
            tvMsg.text = message
        }

        val btnPositive = dialog.findViewById<TextView>(R.id.btn_ok_text)
        if (btnText.isNullOrEmpty()) {
            btnPositive.visibility = View.GONE
        } else {
            btnPositive.text = btnText
            btnPositive.setOnClickListener(clickListener)
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun showAlertDialog(context: Context, title: String, description: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_layout_with_single_button)
        dialog.dialog_title_text.text = title
        dialog.dialog_description_text.text = description
        dialog.dialog_ok_btn.setOnClickListener {dialog.dismiss()}
        dialog.show()

    }

    fun createPromptInfo(userName: String): BiometricPrompt.PromptInfo =
            BiometricPrompt.PromptInfo.Builder().apply {
                setTitle("Welcome back, $userName")
                setDescription("Please log in to continue")
                setConfirmationRequired(false)
                setNegativeButtonText("LOG IN WITH PASSWORD")
            }.build()

    fun getLogoutDialog(context: Context?, btnPositiveText: String?,
                                   clickListener: View.OnClickListener?, btnNegativeText: String?, negativeListener: View.OnClickListener?): Dialog? {
        if (context == null) return null
        val logoutDialog = Dialog(context)
        if (logoutDialog.window != null) {
            logoutDialog.window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            logoutDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            logoutDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        logoutDialog.setContentView(R.layout.logout_dialog_layout)

        val btnLogout = logoutDialog.findViewById<TextView>(R.id.logout_btn)
        val btnCancel = logoutDialog.findViewById<TextView>(R.id.logout_btn_cancel_text)
        val dividerView = logoutDialog.findViewById<View>(R.id.logout_btn_divider_view)

        if (btnPositiveText.isNullOrEmpty()) {
            btnLogout.visibility = View.GONE
        } else {
            btnLogout.text = btnPositiveText
            if (null != clickListener) {
                btnLogout.setOnClickListener(clickListener)
            }
        }

        if (btnNegativeText.isNullOrEmpty()) {
            btnCancel.visibility = View.GONE
            dividerView.visibility = View.GONE
        } else {
            btnCancel.text = btnNegativeText
            if (null != negativeListener) {
                btnCancel.setOnClickListener(negativeListener)
            }
        }
        logoutDialog.setCanceledOnTouchOutside(false)
        logoutDialog.setCancelable(false)
        return logoutDialog
    }


}