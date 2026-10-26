package co.edu.uniquindio.petadoptionapp.di

import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ChangePasswordUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.LoginUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.RegisterUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ResendTwoFactorCodeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.SendPasswordResetUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.VerifyTwoFactorCodeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.CheckPostCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.CreateAnimalTypeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.CreateCategoryUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveAnimalTypesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.UpdateAnimalTypeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.UpdateCategoryUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.CountUnreadNotificationsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetFeedUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.AwardPointsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.CheckProfileUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.DeleteAccountUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetUserProfileUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireOwnUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.SearchUsersUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.UpdateProfileUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.UpdateSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent

/**
Documenta el módulo de Hilt que fabrica todos los casos de uso.
Cada función @Provides es una receta: los parámetros son lo que el caso de uso necesita
y Hilt los entrega solo. Así `domain` sigue sin depender de Hilt.
 */
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {


    // Logín

    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(
        authRepository: AuthRepository,
        checkProfile: CheckProfileUseCase,
    ): LoginUseCase = LoginUseCase(authRepository, checkProfile)

    @Provides
    @ViewModelScoped
    fun provideCheckProfileUseCase(
        // TODO: ajustar a los parámetros reales del constructor.
        userRepository: UserRepository,
    ): CheckProfileUseCase = CheckProfileUseCase(userRepository)


    // Implementación de saber quien se logueo
    @Provides
    @ViewModelScoped
    fun provideObserveCurrentUserUseCase(
        userRepository: UserRepository,
    ): ObserveCurrentUserUseCase = ObserveCurrentUserUseCase(userRepository)


    @Provides
    @ViewModelScoped
    fun provideObserveSessionUseCase(
        authRepository: AuthRepository,
    ): ObserveSessionUseCase = ObserveSessionUseCase(authRepository)


    // Registrar Cuenta

    @Provides
    @ViewModelScoped
    fun provideRegisterUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository,
    ): RegisterUseCase = RegisterUseCase(authRepository, userRepository)


    // Home User

    @Provides
    @ViewModelScoped
    fun provideGetFeedUseCase(
        postRepository: PostRepository,
    ): GetFeedUseCase = GetFeedUseCase(postRepository)


    @Provides
    @ViewModelScoped
    fun providesLoadCatalogUseCase(
        catalogRepository: CatalogRepository,
    ): LoadCatalogUseCase = LoadCatalogUseCase(catalogRepository)


    @Provides
    @ViewModelScoped
    fun providesObserveCategoriesUseCase(
        catalogRepository: CatalogRepository,
    ): ObserveCategoriesUseCase = ObserveCategoriesUseCase(catalogRepository)


    @Provides
    @ViewModelScoped
    fun providesObserveAnimalTypesUseCase(
        catalogRepository: CatalogRepository,
    ): ObserveAnimalTypesUseCase = ObserveAnimalTypesUseCase(catalogRepository)


    @Provides
    @ViewModelScoped
    fun providesCountUnreadNotificationsUseCase(
        notificationRepository: NotificationRepository,
        authRepository: AuthRepository,
    ): CountUnreadNotificationsUseCase = CountUnreadNotificationsUseCase(notificationRepository, authRepository)



    @Provides
    @ViewModelScoped
    fun providesSearchUsersUseCase(
        userRepository: UserRepository,
        requiredUser: RequireUserUseCase,
    ): SearchUsersUseCase = SearchUsersUseCase(userRepository, requiredUser)


    @Provides
    @ViewModelScoped
    fun providesRequireUserUseCase(
        authRepository: AuthRepository,
    ): RequireUserUseCase = RequireUserUseCase(authRepository)


    // Home Moderador
}