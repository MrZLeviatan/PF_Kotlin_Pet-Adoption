package co.edu.uniquindio.petadoptionapp.di

import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
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
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.GetMyVotedPostIdsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.ToggleImportantUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.CountUnreadNotificationsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.GetNotificationsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.MarkAllNotificationsReadUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.MarkNotificationReadUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetFeedUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostStatsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetUserPostsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.AwardPointsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.EvaluateMyBadgesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.LoadBadgesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.ObserveBadgesUseCase
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
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetMyPostsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.FinishPostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.DeletePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostDetailUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.GetCommentsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.CreateCommentUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.ToggleCommentLikeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.GetLikedCommentsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption.SendAdoptionInterestUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption.GetMyAdoptionRequestUseCase
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


    @Provides
    @ViewModelScoped
    fun providesToggleImportanUserUseCase(
        voteRepository: ImportantVoteRepository,
        postRepository: PostRepository,
        requiredUser: RequireUserUseCase,
        notify: NotifyUseCase,
        awardPoints: AwardPointsUseCase,
    ): ToggleImportantUseCase = ToggleImportantUseCase(voteRepository, postRepository, requiredUser, notify, awardPoints)


    @Provides
    @ViewModelScoped
    fun providesGetMyVotedPostIdUseCase(
        voteRepository: ImportantVoteRepository,
        requiredUser: RequireUserUseCase,
    ): GetMyVotedPostIdsUseCase = GetMyVotedPostIdsUseCase(voteRepository, requiredUser)



    // Notificaciones

    @Provides
    @ViewModelScoped
    fun providesGetNotificationsUseCase(
        notificationRepository: NotificationRepository,
        authRepository: AuthRepository
    ): GetNotificationsUseCase = GetNotificationsUseCase(notificationRepository, authRepository)


    @Provides
    @ViewModelScoped
    fun providesMarkNotificationReadUseCase(
        notificationRepository: NotificationRepository,
        authRepository: AuthRepository
    ): MarkNotificationReadUseCase = MarkNotificationReadUseCase(notificationRepository, authRepository)


    @Provides
    @ViewModelScoped
    fun providesMarkAllNotificationsReadUseCase(
        notificationRepository: NotificationRepository,
        authRepository: AuthRepository
    ): MarkAllNotificationsReadUseCase = MarkAllNotificationsReadUseCase(notificationRepository, authRepository)


    @Provides
    @ViewModelScoped
    fun providesLoadBadgesUseCase(
        reputationRepository: ReputationRepository,
    ): LoadBadgesUseCase = LoadBadgesUseCase(reputationRepository)


    @Provides
    @ViewModelScoped
    fun providesObserverBadgesUseCase(
        reputationRepository: ReputationRepository,
    ): ObserveBadgesUseCase = ObserveBadgesUseCase(reputationRepository)


    @Provides
    @ViewModelScoped
    fun providesNotifyUseCase(
        notificationRepository: NotificationRepository,
    ): NotifyUseCase = NotifyUseCase(notificationRepository)


    @Provides
    @ViewModelScoped
    fun providesAwardPointsUseCase(
        reputationRepository: ReputationRepository,
        notify: NotifyUseCase
    ): AwardPointsUseCase = AwardPointsUseCase(reputationRepository, notify)

    // Metricas

    @Provides
    @ViewModelScoped
    fun providesEvaluateMyBadgesUseCase(
        reputationRepository: ReputationRepository,
        userRepository: UserRepository,
        postRepository: PostRepository,
        requiredUser: RequireUserUseCase,
        notify: NotifyUseCase
    ): EvaluateMyBadgesUseCase = EvaluateMyBadgesUseCase(reputationRepository, userRepository, postRepository, requiredUser, notify)


    @Provides
    @ViewModelScoped
    fun providesGetPostStatusUseCase(
        postRepository: PostRepository,
        requiredUser: RequireUserUseCase
    ): GetPostStatsUseCase = GetPostStatsUseCase(postRepository, requiredUser)



    // Configuración

    @Provides
    @ViewModelScoped
    fun providesRequireOwnUserUseCase(
        authRepository: AuthRepository,
    ): RequireOwnUserUseCase = RequireOwnUserUseCase(authRepository)

    @Provides
    @ViewModelScoped
    fun providesUpdateSettingsUseCase(
        userRepository: UserRepository,
        requireOwnUser: RequireOwnUserUseCase,
    ): UpdateSettingsUseCase = UpdateSettingsUseCase(userRepository, requireOwnUser)

    // Editar perfil

    @Provides
    @ViewModelScoped
    fun providesUpdateProfileUseCase(
        userRepository: UserRepository,
        requireOwnUser: RequireOwnUserUseCase,
    ): UpdateProfileUseCase = UpdateProfileUseCase(userRepository, requireOwnUser)

    @Provides
    @ViewModelScoped
    fun providesChangePasswordUseCase(
        authRepository: AuthRepository,
    ): ChangePasswordUseCase = ChangePasswordUseCase(authRepository)

    @Provides
    @ViewModelScoped
    fun providesDeleteAccountUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository,
        postRepository: PostRepository,
        requireOwnUser: RequireOwnUserUseCase,
    ): DeleteAccountUseCase = DeleteAccountUseCase(authRepository, userRepository, postRepository, requireOwnUser)


    // Perfil de otra persona

    @Provides
    @ViewModelScoped
    fun providesGetUserProfileUseCase(
        userRepository: UserRepository,
    ): GetUserProfileUseCase = GetUserProfileUseCase(userRepository)

    @Provides
    @ViewModelScoped
    fun providesGetUserPostsUseCase(
        postRepository: PostRepository,
        userRepository: UserRepository,
        authRepository: AuthRepository,
    ): GetUserPostsUseCase = GetUserPostsUseCase(postRepository, userRepository, authRepository)


    // Mis publicaciones y detalle

    @Provides
    @ViewModelScoped
    fun providesGetMyPostsUseCase(
        postRepository: PostRepository,
    ): GetMyPostsUseCase = GetMyPostsUseCase(postRepository)

    @Provides
    @ViewModelScoped
    fun providesFinishPostUseCase(
        postRepository: PostRepository,
        notify: NotifyUseCase,
        awardPoints: AwardPointsUseCase,
    ): FinishPostUseCase = FinishPostUseCase(postRepository, notify, awardPoints)

    @Provides
    @ViewModelScoped
    fun providesDeletePostUseCase(
        postRepository: PostRepository,
    ): DeletePostUseCase = DeletePostUseCase(postRepository)

    @Provides
    @ViewModelScoped
    fun providesGetPostDetailUseCase(
        postRepository: PostRepository,
    ): GetPostDetailUseCase = GetPostDetailUseCase(postRepository)

    @Provides
    @ViewModelScoped
    fun providesSendAdoptionInterestUseCase(
        chatRepository: ChatRepository,
        postRepository: PostRepository,
        catalogRepository: CatalogRepository,
        requireUser: RequireUserUseCase,
        notify: NotifyUseCase,
    ): SendAdoptionInterestUseCase =
        SendAdoptionInterestUseCase(chatRepository, postRepository, catalogRepository, requireUser, notify)

    @Provides
    @ViewModelScoped
    fun providesGetMyAdoptionRequestUseCase(
        chatRepository: ChatRepository,
        requireUser: RequireUserUseCase,
    ): GetMyAdoptionRequestUseCase = GetMyAdoptionRequestUseCase(chatRepository, requireUser)


    // Comentarios

    @Provides
    @ViewModelScoped
    fun providesGetCommentsUseCase(
        commentRepository: CommentRepository,
        authRepository: AuthRepository,
        getPostDetail: GetPostDetailUseCase,
    ): GetCommentsUseCase = GetCommentsUseCase(commentRepository, authRepository, getPostDetail)

    @Provides
    @ViewModelScoped
    fun providesCreateCommentUseCase(
        commentRepository: CommentRepository,
        postRepository: PostRepository,
        userRepository: UserRepository,
        requireUser: RequireUserUseCase,
        notify: NotifyUseCase,
        awardPoints: AwardPointsUseCase,
    ): CreateCommentUseCase =
        CreateCommentUseCase(commentRepository, postRepository, userRepository, requireUser, notify, awardPoints)

    @Provides
    @ViewModelScoped
    fun providesToggleCommentLikeUseCase(
        commentRepository: CommentRepository,
        requireUser: RequireUserUseCase,
    ): ToggleCommentLikeUseCase = ToggleCommentLikeUseCase(commentRepository, requireUser)

    @Provides
    @ViewModelScoped
    fun providesGetLikedCommentsUseCase(
        commentRepository: CommentRepository,
        requireUser: RequireUserUseCase,
    ): GetLikedCommentsUseCase = GetLikedCommentsUseCase(commentRepository, requireUser)


    // Home Moderador
}