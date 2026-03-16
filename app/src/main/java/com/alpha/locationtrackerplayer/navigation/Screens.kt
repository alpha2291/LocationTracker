package com.alpha.locationtrackerplayer.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object SignupScreen : Screen

    @Serializable
    data object LoginScreen : Screen

    @Serializable
    data object LocationListScreen : Screen

    @Serializable
    data class MapScreen(
        val userId: String
    ) : Screen

}