package com.alpha.locationtrackerplayer.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class AppNavigator {

    val backStack = mutableStateListOf<Screen>()
    var navDirection by mutableStateOf(NavDirection.Forward)
        private set

    fun navigate(screen: Screen) {
        navDirection = NavDirection.Forward
        backStack.add(screen)
    }

    fun pop(): Boolean {
        return if (backStack.size > 1) {
            navDirection = NavDirection.Back
            backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }
    }

    fun clearAndNavigate(screen: Screen) {
        navDirection = NavDirection.Forward
        backStack.clear()
        backStack.add(screen)
    }

    val currentScreen: Screen
        get() = backStack.last()

}

enum class NavDirection {
    Forward, Back
}

val LocalNavigator = staticCompositionLocalOf<AppNavigator> {
    error("Navigator not provided")
}
