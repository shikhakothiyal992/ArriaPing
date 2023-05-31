package com.arria.ping.util

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.arria.ping.R

class CustomAlertDialog  : Dialog, View.OnClickListener {


    interface DialogViewListener {
        fun onOkPress()
        fun onCancelPress()
    }

    private var tv_title: TextView? = null
    private var tv_msg: TextView? = null
    private var btnOk: TextView? = null
    private var btnCancel: TextView? = null
    private var mListener: DialogViewListener? = null


    constructor(context: Context?) : super(context!!, R.style.CustomDialog) {
        setContentView(R.layout.two_button_dialog_layout)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        init()
    }

    constructor(
            context: Context?,
            isCancelable: Boolean
    ) : super(context!!, R.style.CustomDialog) {
        setContentView(R.layout.two_button_dialog_layout)
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(false)
        init()
    }

    private fun init() {
        initViews()
        initListener()
    }

    private fun initListener() {
        btnOk!!.setOnClickListener(this)
        btnCancel!!.setOnClickListener(this)
    }

    private fun initViews() {
        tv_title = findViewById(R.id.title_text)
        tv_msg = findViewById(R.id.description_text)
        btnOk = findViewById(R.id.btn_ok_text)
        btnCancel = findViewById(R.id.btn_cancel_text)
    }

    fun showAlert(
            title: String?,
            message: String?,
            btnOkText: String?,
            btnCancelText: String?
    ) {
        tv_title?.text = title
        tv_msg?.text = message
        btnOk!!.text = btnOkText
        if (!TextUtils.isEmpty(btnCancelText)) {
            btnCancel!!.text = btnCancelText
            btnCancel!!.visibility = View.VISIBLE
        } else {
            btnCancel!!.visibility = View.GONE
        }
        show()
    }

    fun setOkListener(mListener: DialogViewListener?) {
        this.mListener = mListener
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_ok_text -> mListener!!.onOkPress()
            R.id.btn_cancel_text -> mListener!!.onCancelPress()
        }
    }

    companion object {
        var alertDialog: CustomAlertDialog? = null
        var context: Context? = null
        fun getInstance(mContext: Context?): CustomAlertDialog? {
            dismissDialog()
            context = mContext
            alertDialog = CustomAlertDialog(mContext)
            return alertDialog
        }

        fun getInstance(
                mContext: Context?,
                isCancelable: Boolean
        ): CustomAlertDialog? {
            dismissDialog()
            context = mContext
            alertDialog = CustomAlertDialog(mContext, isCancelable)
            return alertDialog
        }

        fun dismissDialog() {
            try {
                if (alertDialog != null) {
                    alertDialog!!.dismiss()
                }
            } catch (e: Exception) {
            }
        }
    }
}