package com.alpha.locationtrackerplayer.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alpha.locationtrackerplayer.data.database.RealmManager
import com.alpha.locationtrackerplayer.data.model.User
import io.github.xilinjia.krdb.ext.query

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        Log.d(TAG, "BroadcastReceiver triggered")

        val isBootAction = action == Intent.ACTION_BOOT_COMPLETED ||
                action == "android.intent.action.QUICKBOOT_POWERON" ||
                action == "android.intent.action.REBOOT"

        if (!isBootAction) {
            return
        }


        try {
            val realm = RealmManager.realm
            val loggedUser = realm.query<User>("isLoggedIn == true").first().find()

            if (loggedUser != null) {
                val userId = loggedUser._id.toHexString()
                Log.d(TAG, "Found logged-in user: $userId")
                Log.d(TAG, "  Email: ${loggedUser.email}")

                LocationScheduler.start(context, userId)

            } else {
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in BootReceiver: ${e.message}", e)
        }

    }
}
