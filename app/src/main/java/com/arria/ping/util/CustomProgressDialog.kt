
package com.arria.ping.util

import android.app.ProgressDialog
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CustomProgressDialog @Inject constructor(@ApplicationContext private var context: Context){
    private lateinit var progressDialog: ProgressDialog


    fun showProgressDialog() {
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please wait...")
        progressDialog.show()
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setCancelable(false)

    }

    fun dismissProgressDialog() {
        if(progressDialog.isShowing){
            progressDialog.dismiss()

        }

    }

}
