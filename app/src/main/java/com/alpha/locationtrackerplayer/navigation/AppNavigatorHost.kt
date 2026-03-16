package com.alpha.locationtrackerplayer.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.alpha.locationtrackerplayer.data.model.User
import com.alpha.locationtrackerplayer.ui.Location.LocationListScreen
import com.alpha.locationtrackerplayer.ui.auth.LoginScreen
import com.alpha.locationtrackerplayer.ui.auth.SignupScreen
import com.alpha.locationtrackerplayer.ui.map.MapScreen

@Composable
fun AppNavigatorHost(loggedUser: User?) {

    val startDestination: Screen = if (loggedUser != null)
        Screen.LocationListScreen
    else
        Screen.LoginScreen

    val navigator = remember(startDestination) {
        AppNavigator().apply {
            backStack.clear()
            backStack.add(startDestination)
        }
    }

    Scaffold() { paddingValues ->
        Box (
            modifier = Modifier.padding(paddingValues)
        ){
            CompositionLocalProvider(LocalNavigator provides navigator) {

                NavDisplay(
                    backStack = navigator.backStack,

                    onBack = { navigator.pop() },

                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) togetherWith
                                slideOutHorizontally(targetOffsetX = { -it })
                    },

//                    popTransitionSpec = {
//                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
//                                slideOutHorizontally(targetOffsetX = { it })
//                    },

                    entryProvider = entryProvider {
                        entry<Screen.SignupScreen> { SignupScreen() }
                        entry<Screen.LoginScreen> { LoginScreen() }
                        entry<Screen.LocationListScreen> { LocationListScreen() }
                        entry<Screen.MapScreen> { screen ->
                            MapScreen(userId = screen.userId)
                        }
                    }
                )
            }
        }
    }
}
