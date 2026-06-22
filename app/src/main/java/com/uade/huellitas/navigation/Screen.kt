package com.uade.huellitas.navigation

sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Onboarding   : Screen("onboarding")
    object Login        : Screen("login")
    object Register     : Screen("register")
    object Home         : Screen("home")
    object CreateAlert  : Screen("create_alert")
    object Profile      : Screen("profile")
    object MyPets       : Screen("my_pets")
    object MyAlerts     : Screen("my_alerts")
    object ExpressAlert : Screen("express_alert")

    object EditProfile : Screen("edit_profile")
    object Map          : Screen("map")
    object AlertDetail  : Screen("alert_detail/{alertId}") {
        fun createRoute(alertId: String) = "alert_detail/$alertId"
    }
    object EditPet      : Screen("edit_pet/{petId}") {
        fun createRoute(petId: String) = "edit_pet/$petId"
    }
    object CreatePet    : Screen("create_pet")
}
