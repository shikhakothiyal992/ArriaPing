package com.arria.ping.util.globalexceptionhandler

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.arria.ping.log.Logger
import kotlin.system.exitProcess


class GlobalExceptionHandler(context: Activity) : Thread.UncaughtExceptionHandler {

    private var mContext: Context = context

    init {

        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(
            thread: Thread,
            ex: Throwable
    ) {
        object : Thread() {
            override fun run() {
                Looper.prepare()
                Logger.error(ex.message.toString(), "UN-CAUGHT Exception")
                //showMaterialDialog(mContext,"Something went wrong.","We're working on this.")
                Looper.loop()
            }
        }.start()

        try {
            Thread.sleep(5000)
            exitProcess(1)
        } catch (e: InterruptedException) {
           Log.e("GlobalExceptionHandler","error ${e.message.toString()}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showMaterialDialog(context: Context, title: String, description: String) {
        AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(description)
                .show()
    }
}