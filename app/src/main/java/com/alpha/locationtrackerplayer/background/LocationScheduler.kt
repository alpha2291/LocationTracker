package com.alpha.locationtrackerplayer.background

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object LocationScheduler {

    private const val TAG = "LocationScheduler"
    private const val WORK_NAME = "locationTracking"

    fun start(context: Context, userId: String) {


        val data = workDataOf("userId" to userId)

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val request = PeriodicWorkRequestBuilder<LocationWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setInputData(data)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )

        Log.d(TAG, "WorkManager job enqueued (name: $WORK_NAME)")

        val serviceIntent = LocationForegroundService.startIntent(context)
        context.startForegroundService(serviceIntent)

        Log.d(TAG, "Foreground service started")
    }

    fun stop(context: Context) {
        Log.d(TAG, "STOPPING all location tracking")

        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork(WORK_NAME)
        wm.cancelAllWorkByTag(WORK_NAME)

        try {
            val stopIntent = LocationForegroundService.stopIntent(context)
            context.startService(stopIntent)
            Log.d(TAG, " Foreground service stop intent sent")
        } catch (e: Exception) {
            Log.w(TAG, "Stop intent failed: ${e.message}")
        }

        try {
            context.stopService(
                android.content.Intent(context, LocationForegroundService::class.java)
            )
        } catch (e: Exception) {
        }

        Log.d(TAG, " All location services stopped")
    }
}

