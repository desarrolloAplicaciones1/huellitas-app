package com.uade.huellitas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.uade.huellitas.HuellitasApplication
import com.uade.huellitas.presentation.auth.LoginScreen
import com.uade.huellitas.presentation.auth.RegisterScreen
import com.uade.huellitas.presentation.alert.create.CreateAlertScreen
import com.uade.huellitas.presentation.alert.express.ExpressAlertScreen
import com.uade.huellitas.presentation.alert.detail.AlertDetailScreen
import com.uade.huellitas.presentation.home.HomeScreen
import com.uade.huellitas.presentation.map.MapScreen
import com.uade.huellitas.presentation.onboarding.OnboardingScreen
import com.uade.huellitas.presentation.profile.edit.EditProfileScreen
import com.uade.huellitas.presentation.profile.ProfileScreen
import com.uade.huellitas.presentation.profile.alerts.MyAlertsScreen
import com.uade.huellitas.presentation.profile.pets.CreatePetScreen
import com.uade.huellitas.presentation.profile.pets.MyPetsScreen
import com.uade.huellitas.presentation.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    val appContainer = (LocalContext.current.applicationContext as HuellitasApplication).appContainer

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) { popUpTo(0) }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = appContainer.onboardingViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = appContainer.loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) { popUpTo(0) }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) { popUpTo(0) }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = appContainer.homeViewModel,
                onNavigateToDetail = { alertId ->
                    navController.navigate(Screen.AlertDetail.createRoute(alertId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
                onNavigateToCreateAlert = { navController.navigate(Screen.CreateAlert.route) },
                onNavigateToExpressAlert = { navController.navigate(Screen.ExpressAlert.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.CreateAlert.route) {
            CreateAlertScreen(
                viewModel = appContainer.createAlertViewModel,
                onBack = { navController.popBackStack() },
                onAlertCreated = { navController.popBackStack() }
            )
        }

        composable(Screen.ExpressAlert.route) {
            ExpressAlertScreen(
                onBack = { navController.popBackStack() },
                onPublished = { navController.popBackStack() }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { alertId ->
                    navController.navigate(Screen.AlertDetail.createRoute(alertId))
                }
            )
        }

        composable(
            route = Screen.AlertDetail.route,
            arguments = listOf(navArgument("alertId") { type = NavType.StringType })
        ) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString("alertId") ?: return@composable
            AlertDetailScreen(
                alertId = alertId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
                onNavigateToAlertDetail = { alertId ->
                    navController.navigate(Screen.AlertDetail.createRoute(alertId))
                },
                onNavigateToMyAlerts = {
                    navController.navigate(Screen.MyAlerts.route)
                },
                onNavigateToMyPets = {
                    navController.navigate(Screen.MyPets.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.MyPets.route) {
            MyPetsScreen(
                viewModel = appContainer.myPetsViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToCreatePet = { navController.navigate(Screen.CreatePet.route) }
            )
        }

        composable(Screen.CreatePet.route) {
            CreatePetScreen(
                onBack = { navController.popBackStack() },
                onPetCreated = { navController.popBackStack() }
            )
        }

        composable(Screen.MyAlerts.route) {
            MyAlertsScreen(
                viewModel = appContainer.myAlertsViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { alertId ->
                    navController.navigate(Screen.AlertDetail.createRoute(alertId))
                }
            )
        }

        composable(
            route = Screen.EditAlert.route,
            arguments = listOf(navArgument("alertId") { type = NavType.StringType })
        ) { }
    }
}
