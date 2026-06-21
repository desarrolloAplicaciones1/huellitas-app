package com.uade.huellitas.di

import android.content.Context
import com.uade.huellitas.data.local.AppDatabase
import com.uade.huellitas.data.local.NetworkMonitor
import com.uade.huellitas.data.local.OnboardingPreferences
import com.uade.huellitas.data.remote.FirebaseAuthDataSource
import com.uade.huellitas.data.remote.FirestoreAlertDataSource
import com.uade.huellitas.data.remote.FirestorePetDataSource
import com.uade.huellitas.data.remote.FirestoreUserDataSource
import com.uade.huellitas.data.repository.AndroidDeviceLocationRepository
import com.uade.huellitas.data.repository.AndroidGeocodingRepository
import com.uade.huellitas.data.repository.AlertRepository as AlertRepositoryImpl
import com.uade.huellitas.data.repository.FirebasePhotoStorageRepository
import com.uade.huellitas.data.repository.PetRepository as PetRepositoryImpl
import com.uade.huellitas.data.repository.PreferencesSettingsRepository
import com.uade.huellitas.data.repository.UserRepository as UserRepositoryImpl
import com.uade.huellitas.domain.repository.GeocodingRepository as GeocodingRepositoryContract
import com.uade.huellitas.domain.repository.AlertRepository as AlertRepositoryContract
import com.uade.huellitas.domain.repository.DeviceLocationRepository as DeviceLocationRepositoryContract
import com.uade.huellitas.domain.repository.PetRepository as PetRepositoryContract
import com.uade.huellitas.domain.repository.PhotoStorageRepository as PhotoStorageRepositoryContract
import com.uade.huellitas.domain.repository.SettingsRepository as SettingsRepositoryContract
import com.uade.huellitas.domain.repository.UserRepository as UserRepositoryContract
import com.uade.huellitas.domain.usecase.alert.CreateAlertUseCase
import com.uade.huellitas.domain.usecase.alert.DeleteAlertUseCase
import com.uade.huellitas.domain.usecase.alert.FilterAlertsByRadiusUseCase
import com.uade.huellitas.domain.usecase.alert.GetActiveAlertsUseCase
import com.uade.huellitas.domain.usecase.alert.GetAlertByIdUseCase
import com.uade.huellitas.domain.usecase.alert.GetMyAlertsUseCase
import com.uade.huellitas.domain.usecase.alert.ResolveAlertUseCase
import com.uade.huellitas.domain.usecase.alert.SyncAlertsUseCase
import com.uade.huellitas.domain.usecase.alert.UpdateAlertUseCase
import com.uade.huellitas.domain.usecase.auth.GetCurrentUserIdUseCase
import com.uade.huellitas.domain.usecase.auth.IsLoggedInUseCase
import com.uade.huellitas.domain.usecase.auth.LoginUseCase
import com.uade.huellitas.domain.usecase.auth.LogoutUseCase
import com.uade.huellitas.domain.usecase.auth.RegisterUserUseCase
import com.uade.huellitas.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.uade.huellitas.domain.usecase.location.CalculateDistanceMetersUseCase
import com.uade.huellitas.domain.usecase.location.GeocodeAddressUseCase
import com.uade.huellitas.domain.usecase.location.GetCurrentDeviceLocationUseCase
import com.uade.huellitas.domain.usecase.location.ResolveReferenceLocationUseCase
import com.uade.huellitas.domain.usecase.media.DeletePhotoUseCase
import com.uade.huellitas.domain.usecase.media.UploadAlertPhotoUseCase
import com.uade.huellitas.domain.usecase.media.UploadPetPhotoUseCase
import com.uade.huellitas.domain.usecase.media.UploadProfilePhotoUseCase
import com.uade.huellitas.domain.usecase.pet.DeletePetUseCase
import com.uade.huellitas.domain.usecase.pet.GetMyPetsUseCase
import com.uade.huellitas.domain.usecase.pet.GetPetByIdUseCase
import com.uade.huellitas.domain.usecase.pet.SavePetUseCase
import com.uade.huellitas.domain.usecase.pet.UpdatePetUseCase
import com.uade.huellitas.domain.usecase.settings.GetAppSettingsUseCase
import com.uade.huellitas.domain.usecase.settings.SetAlertRadiusUseCase
import com.uade.huellitas.domain.usecase.settings.SetDarkModeUseCase
import com.uade.huellitas.domain.usecase.settings.SetFollowSystemThemeUseCase
import com.uade.huellitas.domain.usecase.settings.SetOfflineModeUseCase
import com.uade.huellitas.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.uade.huellitas.domain.usecase.user.ChangePasswordUseCase
import com.uade.huellitas.domain.usecase.user.GetCurrentUserUseCase
import com.uade.huellitas.domain.usecase.user.SyncCurrentUserProfileUseCase
import com.uade.huellitas.domain.usecase.user.UpdateUserProfileUseCase
import com.uade.huellitas.presentation.alert.create.CreateAlertViewModel
import com.uade.huellitas.presentation.auth.LoginViewModel
import com.uade.huellitas.presentation.home.HomeViewModel
import com.uade.huellitas.presentation.onboarding.OnboardingViewModel
import com.uade.huellitas.presentation.profile.alerts.MyAlertsViewModel
import com.uade.huellitas.presentation.profile.pets.MyPetsViewModel

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val onboardingPreferences = OnboardingPreferences(context)
    val networkMonitor = NetworkMonitor(context)

    private val authDataSource = FirebaseAuthDataSource()
    private val firestoreAlertDataSource = FirestoreAlertDataSource()
    private val firestoreUserDataSource = FirestoreUserDataSource()
    private val firestorePetDataSource = FirestorePetDataSource()

    private val userRepository: UserRepositoryContract =
        UserRepositoryImpl(database.userDao(), authDataSource, firestoreUserDataSource)

    private val petRepository: PetRepositoryContract =
        PetRepositoryImpl(database.petDao(), firestorePetDataSource)

    private val photoStorageRepository: PhotoStorageRepositoryContract =
        FirebasePhotoStorageRepository()

    private val alertRepository: AlertRepositoryContract =
        AlertRepositoryImpl(database.alertDao(), firestoreAlertDataSource, photoStorageRepository)

    private val settingsRepository: SettingsRepositoryContract =
        PreferencesSettingsRepository(context)

    private val geocodingRepository: GeocodingRepositoryContract =
        AndroidGeocodingRepository(context)

    private val deviceLocationRepository: DeviceLocationRepositoryContract =
        AndroidDeviceLocationRepository(context)

    val getCurrentUserIdUseCase = GetCurrentUserIdUseCase(userRepository)
    val isLoggedInUseCase = IsLoggedInUseCase(userRepository)
    val loginUseCase = LoginUseCase(userRepository)
    val logoutUseCase = LogoutUseCase(userRepository)
    val registerUserUseCase = RegisterUserUseCase(userRepository)
    val sendPasswordResetEmailUseCase = SendPasswordResetEmailUseCase(userRepository)

    val getCurrentUserUseCase = GetCurrentUserUseCase(userRepository)
    val syncCurrentUserProfileUseCase = SyncCurrentUserProfileUseCase(userRepository)
    val updateUserProfileUseCase = UpdateUserProfileUseCase(userRepository)
    val changePasswordUseCase = ChangePasswordUseCase(userRepository)

    val getActiveAlertsUseCase = GetActiveAlertsUseCase(alertRepository)
    val getAlertByIdUseCase = GetAlertByIdUseCase(alertRepository)
    val getMyAlertsUseCase = GetMyAlertsUseCase(alertRepository, userRepository)
    val createAlertUseCase = CreateAlertUseCase(alertRepository)
    val updateAlertUseCase = UpdateAlertUseCase(alertRepository)
    val resolveAlertUseCase = ResolveAlertUseCase(alertRepository)
    val deleteAlertUseCase = DeleteAlertUseCase(alertRepository)
    val syncAlertsUseCase = SyncAlertsUseCase(alertRepository)

    val getMyPetsUseCase = GetMyPetsUseCase(petRepository, userRepository)
    val getPetByIdUseCase = GetPetByIdUseCase(petRepository)
    val savePetUseCase = SavePetUseCase(petRepository)
    val updatePetUseCase = UpdatePetUseCase(petRepository)
    val deletePetUseCase = DeletePetUseCase(petRepository)

    val getAppSettingsUseCase = GetAppSettingsUseCase(settingsRepository)
    val setFollowSystemThemeUseCase = SetFollowSystemThemeUseCase(settingsRepository)
    val setDarkModeUseCase = SetDarkModeUseCase(settingsRepository)
    val setAlertRadiusUseCase = SetAlertRadiusUseCase(settingsRepository)
    val setOfflineModeUseCase = SetOfflineModeUseCase(settingsRepository)

    val geocodeAddressUseCase = GeocodeAddressUseCase(geocodingRepository)
    val getCurrentDeviceLocationUseCase = GetCurrentDeviceLocationUseCase(deviceLocationRepository)
    val calculateDistanceMetersUseCase = CalculateDistanceMetersUseCase()
    val resolveReferenceLocationUseCase = ResolveReferenceLocationUseCase(
        getCurrentDeviceLocationUseCase,
        geocodeAddressUseCase
    )
    val filterAlertsByRadiusUseCase = FilterAlertsByRadiusUseCase(calculateDistanceMetersUseCase)
    val deletePhotoUseCase = DeletePhotoUseCase(photoStorageRepository)
    val uploadAlertPhotoUseCase = UploadAlertPhotoUseCase(photoStorageRepository)
    val uploadProfilePhotoUseCase = UploadProfilePhotoUseCase(photoStorageRepository)
    val uploadPetPhotoUseCase = UploadPetPhotoUseCase(photoStorageRepository)

    val completeOnboardingUseCase = CompleteOnboardingUseCase(onboardingPreferences)
    val onboardingViewModel = OnboardingViewModel(completeOnboardingUseCase)

    val loginViewModel = LoginViewModel(loginUseCase, sendPasswordResetEmailUseCase)
    val homeViewModel = HomeViewModel(
        getActiveAlertsUseCase,
        getCurrentUserUseCase,
        resolveReferenceLocationUseCase,
        filterAlertsByRadiusUseCase,
        networkMonitor
    )
    val createAlertViewModel = CreateAlertViewModel(
        createAlertUseCase,
        getCurrentUserIdUseCase,
        getCurrentUserUseCase,
        geocodeAddressUseCase,
        uploadAlertPhotoUseCase
    )
    val myAlertsViewModel = MyAlertsViewModel(getMyAlertsUseCase)
    val myPetsViewModel = MyPetsViewModel(getMyPetsUseCase, deletePetUseCase)
}
