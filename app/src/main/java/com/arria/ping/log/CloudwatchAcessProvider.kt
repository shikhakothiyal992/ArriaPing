package com.arria.ping.log

import android.content.Context
import android.util.Log
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.arria.ping.BuildConfig
import java.util.HashMap

fun awsCognitoAccessProvider(context: Context, userIdToken: String) {

    Thread {
        val credentialsProvider = CognitoCachingCredentialsProvider(
                context, BuildConfig.AWS_COGNITO_IDENTITY_POOL_ID, Regions.US_WEST_2
        )
        try {
            val logins: MutableMap<String, String> = HashMap()
            logins[BuildConfig.AWS_COGNITO_LOGIN_KEY] = userIdToken
            credentialsProvider.logins = logins
            CloudWatchService.init(credentialsProvider)
        } catch (e: AmazonServiceException) {
            Log.e("AWS", "AWS Service Cognito Exception ${e.message} ${e.statusCode}")
        } catch (e: AmazonServiceException) {
            Log.e("AWS", "AWS Service Cognito Exception ${e.message} ${e.statusCode}")
        } catch (e: Exception) {
            Log.e("AWS", "AWS Cognito Exception ${e.message}")
        }

    }.start()

}
