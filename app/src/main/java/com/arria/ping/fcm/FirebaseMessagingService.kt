package com.arria.ping.fcm

import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arria.ping.log.Logger
import com.arria.ping.util.StorePrefData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessagingService : FirebaseMessagingService() {
    private var message = ""
    private var notificationTitle =""
    private var notificationBody =""
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("newToken", token)
        StorePrefData.firebaseDeviceToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification = NotificationManagerCompat.from(this)
        val isEnabled = notification.areNotificationsEnabled()
        try {
            remoteMessage.data.isNotEmpty().let{
                if(!remoteMessage.data.isNullOrEmpty()){
                    message =remoteMessage.data.toString()
                    Log.v("messageToDisplay", "Message payload: $message")
                     notificationTitle = remoteMessage.data["pinpoint.notification.title"].toString()
                    notificationBody = remoteMessage.data["pinpoint.notification.body"].toString()
                    if(isEnabled){
                        sendMessage(notificationBody)
                    }
                }
            }

        }catch (e:Exception){
            Logger.error("FCM error: ${e.printStackTrace()}","FireBase")
        }
    }
    private fun sendMessage(message: String) {
        val intent = Intent("NotificationData")
        intent.putExtra("Data", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}