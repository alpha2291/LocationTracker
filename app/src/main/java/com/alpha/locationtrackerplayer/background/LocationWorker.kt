package com.alpha.locationtrackerplayer.background

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alpha.locationtrackerplayer.data.database.RealmManager
import com.alpha.locationtrackerplayer.data.model.LocationHistory
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.github.xilinjia.krdb.ext.query
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

class LocationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "LocationWorker"
    }

    override suspend fun doWork(): Result {

        val userId = inputData.getString("userId") ?: ""
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        Log.d(TAG, " Worker STARTED | Time: $now | UserId: $userId | Attempt: $runAttemptCount")

        if (userId.isBlank()) {
            return Result.failure()
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(applicationContext)


        val location = withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                try {
                    val request = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setGranularity(Granularity.GRANULARITY_FINE)
                        .setMaxUpdateAgeMillis(0L) // force fresh — do NOT use cache
                        .setDurationMillis(8_000L)
                        .build()

                    fusedClient.getCurrentLocation(request, null)
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "getCurrentLocation failed: ${e.message}")
                            cont.resume(null)
                        }
                } catch (e: SecurityException) {
                    Log.e(TAG, "Permission denied: ${e.message}")
                    cont.resume(null)
                }
            }
        }


        val finalLocation = location ?: run {
            Log.w(TAG, "⚠ getCurrentLocation null/timeout — trying LocationCallback fallback...")

            withTimeoutOrNull(12_000L) {
                suspendCancellableCoroutine { cont ->
                    try {
                        val locationRequest = LocationRequest.Builder(
                            Priority.PRIORITY_HIGH_ACCURACY, 1000L
                        )
                            .setMinUpdateIntervalMillis(500L)
                            .setMaxUpdates(1)
                            .build()

                        val callback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                fusedClient.removeLocationUpdates(this)
                                cont.resume(result.lastLocation)
                            }
                        }

                        fusedClient.requestLocationUpdates(
                            locationRequest,
                            callback,
                            Looper.getMainLooper()
                        )

                        cont.invokeOnCancellation {
                            fusedClient.removeLocationUpdates(callback)
                        }
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Permission denied on fallback: ${e.message}")
                        cont.resume(null)
                    }
                }
            }
        }

        if (finalLocation == null) {
            return Result.success() // don't retry immediately, wait for next period
        }


        return try {
            val realm = RealmManager.realm

            val saved = realm.writeBlocking {
                copyToRealm(LocationHistory().apply {
                    this.userId    = userId
                    this.latitude  = finalLocation.latitude
                    this.longitude = finalLocation.longitude
                    this.timestamp = System.currentTimeMillis()
                })
            }

            val total = realm.query<LocationHistory>("userId == $0", userId).count().find()

            Log.d(TAG, " Worker COMPLETED SUCCESSFULLY")
            Result.success()

        } catch (e: Exception) {
            Result.retry()
        }
    }
}

