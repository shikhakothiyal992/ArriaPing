package com.arria.ping.log

import android.util.Log
import com.arria.ping.log.data.LogEntity
import com.arria.ping.log.data.Payloads
import com.arria.ping.log.data.UserInfo
import com.arria.ping.util.StorePrefData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

object Logger {

    fun info(message: String, context: String, jsonObject: JSONObject){

        try{
            val userInfo = UserInfo(email = StorePrefData.email, role = StorePrefData.role)
            val payloads = Payloads(userInfo, queryParams = jsonObject.toString())
            val levelInfo = LogEntity("INFO", message, context, payloads)

            val data = Json.encodeToString(levelInfo)

            Thread {
                CloudWatchService.createLogStreamAndEvents(data)
            }.start()

        }catch (exception : Exception){
            Log.e("Logger", "exception $exception")
        }

    }

    fun info(message: String, context: String){
        try{

            val userInfo = UserInfo(email = StorePrefData.email, role = StorePrefData.role)
            val payloads = Payloads(userInfo, null)
            val levelInfo = LogEntity("INFO", message, context, payloads)

            val data = Json.encodeToString(levelInfo)

            Thread {
                CloudWatchService.createLogStreamAndEvents(data)
            }.start()

        }catch (exception : Exception){
            Log.e("Logger", "exception $exception")
        }

    }

    fun warn(message: String, context: String){
        try{

            val userInfo = UserInfo(email = StorePrefData.email, role = StorePrefData.role)
            val payloads = Payloads(userInfo, null)
            val levelInfo = LogEntity("WARN", message, context, payloads)
            val warnData = Json.encodeToString(levelInfo)

            Thread {
                CloudWatchService.createLogStreamAndEvents(warnData)
            }.start()
        }catch (exception : Exception){
            Log.e("Logger", "exception $exception")
        }

    }

    fun error(
            message: String,
            context: String
    ) {
        try {
            val userInfo = UserInfo(email = StorePrefData.email, role = StorePrefData.role)
            val payloads = Payloads(userInfo, null)
            val logEntity = LogEntity("ERROR", message, context, payloads)

            val data = Json.encodeToString(logEntity)

            Thread {
                CloudWatchService.createLogStreamAndEvents(data)
            }.start()
        }  catch (exception: Exception) {
            Log.e("Logger", "exception $exception")
        }
    }
}