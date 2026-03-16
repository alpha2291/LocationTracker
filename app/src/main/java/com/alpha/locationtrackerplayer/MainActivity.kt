package com.alpha.locationtrackerplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.alpha.locationtrackerplayer.background.PermissionManager
import com.alpha.locationtrackerplayer.data.database.RealmManager
import com.alpha.locationtrackerplayer.data.model.User
import com.alpha.locationtrackerplayer.navigation.AppNavigatorHost
import com.alpha.locationtrackerplayer.ui.theme.LocationTrackerPlayerTheme
import io.github.xilinjia.krdb.ext.query

class MainActivity : ComponentActivity() {

    // Step 1: fine location
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) requestBackgroundLocation()
    }

    // Step 2: background location (must be after fine location)
    private val backgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        requestNotificationPermission()
    }

    // Step 3: notification (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Done — no auto-opening of settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val loggedUser = RealmManager.realm.query<User>(
                "isLoggedIn == true"
            ).first().find()

            LocationTrackerPlayerTheme {
                AppNavigatorHost(loggedUser)
            }
        }

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (!PermissionManager.hasLocationPermission(this)) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            requestBackgroundLocation()
        }
    }

    private fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !PermissionManager.hasBackgroundLocationPermission(this)
        ) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionManager.hasNotificationPermission(this)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Nothing else — no settings, no battery dialog
    }
}


