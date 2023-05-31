package com.arria.ping.log

import android.util.Log
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.logs.AmazonCloudWatchLogsClient
import com.amazonaws.services.logs.model.*
import com.arria.ping.BuildConfig
import java.util.*

object CloudWatchService {
    var awsLogsClient1: AmazonCloudWatchLogsClient? = null
    var streamName = ""
    var sequenceToken: String? = null


    fun init(credentialsProvider: CognitoCachingCredentialsProvider) {
        awsLogsClient1 = AmazonCloudWatchLogsClient(credentialsProvider)
        awsLogsClient1!!.setRegion(Region.getRegion(Regions.US_WEST_2))
    }

    fun createLogStreamAndEvents(logEvent: String) {

        if (streamName.isEmpty()) {
            createLogStream()
        }
        putLogEvent(streamName, logEvent)
    }

    private fun createLogStream() {
        val createLogStreamRequest = CreateLogStreamRequest()
        createLogStreamRequest.logStreamName = "${Calendar.getInstance().timeInMillis} Android"
        streamName = createLogStreamRequest.logStreamName
        createLogStreamRequest.logGroupName = BuildConfig.AWS_CLOUD_LOGS_GROUP

        try {
            Thread {
                try {
                    awsLogsClient1?.createLogStream(createLogStreamRequest)
                } catch (e: AmazonServiceException) {
                    Log.e("AWS", "AWS SERVICE Exception in Creating Logs Stream$e")
                } catch (e: Exception) {
                    Log.e("AWS", "Exception in Creating Logs Stream$e")
                }

            }.start()
        } catch (e: Exception) {
            Log.e("AWS", "Creating Logs Stream${e.cause}")
        }
    }


    private fun putLogEvent(
            requestedStream: String,
            logEvent: String
    ) {
        try {

            val logEvents = ArrayList<InputLogEvent>()
            val logInputEvent = InputLogEvent()
            logInputEvent.timestamp = Calendar.getInstance().timeInMillis
            logInputEvent.message = logEvent
            logEvents.add(logInputEvent)

            var token: String? = null
            val logStreamsRequest = DescribeLogStreamsRequest().withLogGroupName(BuildConfig.AWS_CLOUD_LOGS_GROUP)
                    .withLogStreamNamePrefix(requestedStream)

            val result = awsLogsClient1?.describeLogStreams(logStreamsRequest)
            for (stream in result?.logStreams!!) {
                if (requestedStream == stream.logStreamName) token = stream.uploadSequenceToken
            }

            val putLogEventsRequest = PutLogEventsRequest()
            if (token != null) {
                putLogEventsRequest.logGroupName = BuildConfig.AWS_CLOUD_LOGS_GROUP
                putLogEventsRequest.logStreamName = requestedStream
                putLogEventsRequest.setLogEvents(logEvents)
                putLogEventsRequest.sequenceToken = token
                awsLogsClient1?.putLogEvents(putLogEventsRequest)
            } else {
                putLogEventsRequest.logGroupName = BuildConfig.AWS_CLOUD_LOGS_GROUP
                putLogEventsRequest.logStreamName = requestedStream
                putLogEventsRequest.setLogEvents(logEvents)
                awsLogsClient1?.putLogEvents(putLogEventsRequest)
            }
        } catch (invalidSequenceToken: InvalidSequenceTokenException) {
            Log.e("AWS", "InvalidSequenceTokenException put Logs $invalidSequenceToken")
            val expectedSequenceToken = invalidSequenceToken.expectedSequenceToken
            if(expectedSequenceToken != null){
                sequenceToken = expectedSequenceToken
                putLogEvent(streamName, logEvent)
            }

        } catch (e: AmazonServiceException) {
            Log.e("AWS", "AmazonService Exception put Logs $e")

        } catch (e: Exception) {
            Log.e("AWS", "Put Logs Exception $e")
        }
    }

}

