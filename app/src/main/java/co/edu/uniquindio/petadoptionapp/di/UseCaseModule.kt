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
import co.edu.uniquindio.petadoptionapp.domain.usecase.image.UploadImagesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.ai.AnalyzePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.repository.AiAnalysisRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostAnalysisScheduler
import co.edu.uniquindio.petadoptionapp.domain.usecase.location.ReverseGeocodeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.location.SearchAddressUseCase
import co.edu.uniquindio.petadoptionapp.domain.repository.GeocodingRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageStorageRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.CheckRegistrationAvailabilityUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ResendTwoFactorCodeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.RestoreSessionUseCase
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
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetModerationQueueUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.ApprovePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.RejectPostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.StartReviewUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetMyReviewsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetModeratorPerformanceUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.UpdateModeratorTwoFactorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetModerationStatsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetCurrentModeratorUseCase
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
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.CountUnreadChatsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.DeleteChatUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.DeleteNotificationUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.GetMyChatsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.GetOlderMessagesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.MarkChatReadUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.ObserveMessagesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.RequireChatMemberUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.SendMessageUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.StartChatUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption.GetMyAdoptionRequestUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.LogoutUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.GetMyImportantPostsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.CreatePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.UpdatePostUseCase
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

Es el punto donde se unen las capas: los casos de uso (domain/usecase) son clases de Kotlin puro
que solo conocen interfaces de repositorio; RepositoryModule decide qué implementación de la capa
de datos cumple cada interfaz, y este módulo arma los casos de uso con ellas. Los ViewModels de
features/ los reciben por constructor (@HiltViewModel + @Inject) sin saber cómo se construyen.
Se instala en ViewModelComponent porque los casos de uso solo se usan desde ViewModels.
**/
// Declara el objeto como módulo de Dagger, fuente de dependencias.
@Module
// Instala el módulo en el componente de ViewModel: sus objetos viven lo mismo que el ViewModel que los pide.
@InstallIn(ViewModelComponent::class)
// Se usa un object porque los proveedores no necesitan estado propio.
object UseCaseModule {


    // Logín

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee LoginUseCase, que inicia sesión con correo y contraseña y valida que la cuenta tenga un perfil válido.
    fun provideLoginUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
        // Caso de uso que verifica que la cuenta autenticada tenga un perfil existente y no eliminado (Hilt lo construye con su propio proveedor).
        checkProfile: CheckProfileUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): LoginUseCase = LoginUseCase(authRepository, checkProfile)

    // Segundo paso del login (A2F): verificar el código y pedir uno nuevo.
    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee VerifyTwoFactorCodeUseCase, que comprueba el código A2F enviado al correo y completa el inicio de sesión.
    fun provideVerifyTwoFactorCodeUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
        // Caso de uso que verifica que la cuenta autenticada tenga un perfil existente y no eliminado (Hilt lo construye con su propio proveedor).
        checkProfile: CheckProfileUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): VerifyTwoFactorCodeUseCase = VerifyTwoFactorCodeUseCase(authRepository, checkProfile)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ResendTwoFactorCodeUseCase, que genera y envía un nuevo código A2F cuando el anterior venció o no llegó.
    fun provideResendTwoFactorCodeUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ResendTwoFactorCodeUseCase = ResendTwoFactorCodeUseCase(authRepository)

    // Recuperar contraseña (Firebase envía el correo con el enlace).
    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee SendPasswordResetUseCase, que pide a Firebase el correo con el enlace para restablecer la contraseña.
    fun provideSendPasswordResetUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): SendPasswordResetUseCase = SendPasswordResetUseCase(authRepository)

    // Al abrir la app: recuperar la sesión guardada en el teléfono.
    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee RestoreSessionUseCase, que recupera la sesión guardada al abrir la app y verifica que el perfil siga siendo válido.
    fun provideRestoreSessionUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
        // Caso de uso que verifica que la cuenta autenticada tenga un perfil existente y no eliminado (Hilt lo construye con su propio proveedor).
        checkProfile: CheckProfileUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): RestoreSessionUseCase = RestoreSessionUseCase(authRepository, checkProfile)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee CheckProfileUseCase, que verifica que la cuenta autenticada tenga un perfil existente y no eliminado.
    fun provideCheckProfileUseCase(
        // TODO: ajustar a los parámetros reales del constructor.
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): CheckProfileUseCase = CheckProfileUseCase(userRepository)


    // Permite saber quién inició sesión: observa en vivo el perfil del usuario de la sesión.
    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ObserveCurrentUserUseCase, que expone en vivo el perfil del usuario que tiene la sesión.
    fun provideObserveCurrentUserUseCase(
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ObserveCurrentUserUseCase = ObserveCurrentUserUseCase(userRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ObserveSessionUseCase, que expone en vivo la cuenta de la sesión (para reaccionar al iniciar o cerrar sesión).
    fun provideObserveSessionUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ObserveSessionUseCase = ObserveSessionUseCase(authRepository)


    // Registrar Cuenta

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee SearchAddressUseCase, que busca direcciones escritas y devuelve sus coordenadas.
    // Recibe el repositorio de geocodificación del teléfono (direcciones y coordenadas) y crea el caso de uso con él.
    fun providesSearchAddressUseCase(geocoding: GeocodingRepository): SearchAddressUseCase = SearchAddressUseCase(geocoding)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ReverseGeocodeUseCase, que convierte un punto del mapa en una dirección legible.
    // Recibe el repositorio de geocodificación del teléfono (direcciones y coordenadas) y crea el caso de uso con él.
    fun providesReverseGeocodeUseCase(geocoding: GeocodingRepository): ReverseGeocodeUseCase = ReverseGeocodeUseCase(geocoding)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee AnalyzePostUseCase, que analiza una publicación con IA para ayudar al moderador.
    fun providesAnalyzePostUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Repositorio de catálogos: categorías y tipos de animal.
        catalogRepository: CatalogRepository,
        // Repositorio de IA (OpenRouter) que analiza publicaciones.
        aiRepository: AiAnalysisRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): AnalyzePostUseCase = AnalyzePostUseCase(postRepository, catalogRepository, aiRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee UploadImagesUseCase, que sube imágenes locales al almacenamiento remoto y devuelve sus URLs.
    fun providesUploadImagesUseCase(
        // Repositorio de almacenamiento de imágenes (Cloudinary).
        imageStorage: ImageStorageRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): UploadImagesUseCase = UploadImagesUseCase(imageStorage)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee CheckRegistrationAvailabilityUseCase, que revisa si el correo, el alias y el teléfono están libres antes de registrar.
    fun providesCheckRegistrationAvailabilityUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): CheckRegistrationAvailabilityUseCase = CheckRegistrationAvailabilityUseCase(authRepository, userRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee RegisterUseCase, que crea la cuenta en Firebase Auth y guarda el perfil del usuario.
    fun provideRegisterUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): RegisterUseCase = RegisterUseCase(authRepository, userRepository)


    // Home User

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetFeedUseCase, que obtiene la página del feed de publicaciones aplicando los filtros.
    fun provideGetFeedUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetFeedUseCase = GetFeedUseCase(postRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee LoadCatalogUseCase, que fuerza la carga de categorías y tipos de animal.
    fun providesLoadCatalogUseCase(
        // Repositorio de catálogos: categorías y tipos de animal.
        catalogRepository: CatalogRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): LoadCatalogUseCase = LoadCatalogUseCase(catalogRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ObserveCategoriesUseCase, que expone en vivo la lista de categorías.
    fun providesObserveCategoriesUseCase(
        // Repositorio de catálogos: categorías y tipos de animal.
        catalogRepository: CatalogRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ObserveCategoriesUseCase = ObserveCategoriesUseCase(catalogRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ObserveAnimalTypesUseCase, que expone en vivo la lista de tipos de animal.
    fun providesObserveAnimalTypesUseCase(
        // Repositorio de catálogos: categorías y tipos de animal.
        catalogRepository: CatalogRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ObserveAnimalTypesUseCase = ObserveAnimalTypesUseCase(catalogRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee CountUnreadNotificationsUseCase, que cuenta las notificaciones sin leer del usuario de la sesión.
    fun providesCountUnreadNotificationsUseCase(
        // Repositorio de notificaciones (Firestore).
        notificationRepository: NotificationRepository,
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): CountUnreadNotificationsUseCase = CountUnreadNotificationsUseCase(notificationRepository, authRepository)



    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee SearchUsersUseCase, que busca usuarios por alias para encontrarlos y ver su perfil.
    fun providesSearchUsersUseCase(
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): SearchUsersUseCase = SearchUsersUseCase(userRepository, authRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee RequireUserUseCase, que garantiza que haya un usuario común con sesión y devuelve su cuenta.
    fun providesRequireUserUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): RequireUserUseCase = RequireUserUseCase(authRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee RequireModeratorUseCase, que garantiza que la sesión sea de un moderador válido.
    fun providesRequireModeratorUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): RequireModeratorUseCase = RequireModeratorUseCase(authRepository, userRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ToggleImportantUseCase, que marca o desmarca una publicación como "Es importante", notifica y otorga puntos.
    fun providesToggleImportanUserUseCase(
        // Repositorio de votos "Es importante" (Firestore).
        voteRepository: ImportantVoteRepository,
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requiredUser: RequireUserUseCase,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase,
        // Caso de uso que otorga puntos de reputación una sola vez por hecho y avisa si se sube de nivel (Hilt lo construye con su propio proveedor).
        awardPoints: AwardPointsUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ToggleImportantUseCase = ToggleImportantUseCase(voteRepository, postRepository, requiredUser, notify, awardPoints)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetMyVotedPostIdsUseCase, que indica cuáles de las publicaciones mostradas votó el usuario.
    fun providesGetMyVotedPostIdUseCase(
        // Repositorio de votos "Es importante" (Firestore).
        voteRepository: ImportantVoteRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requiredUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetMyVotedPostIdsUseCase = GetMyVotedPostIdsUseCase(voteRepository, requiredUser)



    // Notificaciones

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetNotificationsUseCase, que lista paginada de las notificaciones del usuario.
    fun providesGetNotificationsUseCase(
        // Repositorio de notificaciones (Firestore).
        notificationRepository: NotificationRepository,
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetNotificationsUseCase = GetNotificationsUseCase(notificationRepository, authRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee DeleteNotificationUseCase, que elimina una notificación propia.
    fun providesDeleteNotificationUseCase(
        // Repositorio de notificaciones (Firestore).
        notificationRepository: NotificationRepository,
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): DeleteNotificationUseCase = DeleteNotificationUseCase(notificationRepository, authRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee MarkNotificationReadUseCase, que marca una notificación propia como leída o no leída.
    fun providesMarkNotificationReadUseCase(
        // Repositorio de notificaciones (Firestore).
        notificationRepository: NotificationRepository,
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): MarkNotificationReadUseCase = MarkNotificationReadUseCase(notificationRepository, authRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee MarkAllNotificationsReadUseCase, que marca todas las notificaciones del usuario como leídas.
    fun providesMarkAllNotificationsReadUseCase(
        // Repositorio de notificaciones (Firestore).
        notificationRepository: NotificationRepository,
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): MarkAllNotificationsReadUseCase = MarkAllNotificationsReadUseCase(notificationRepository, authRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee LoadBadgesUseCase, que fuerza la carga del catálogo de insignias.
    fun providesLoadBadgesUseCase(
        // Repositorio de reputación: puntos, historial e insignias.
        reputationRepository: ReputationRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): LoadBadgesUseCase = LoadBadgesUseCase(reputationRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ObserveBadgesUseCase, que expone en vivo el catálogo de insignias.
    fun providesObserverBadgesUseCase(
        // Repositorio de reputación: puntos, historial e insignias.
        reputationRepository: ReputationRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ObserveBadgesUseCase = ObserveBadgesUseCase(reputationRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee NotifyUseCase, que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias).
    fun providesNotifyUseCase(
        // Repositorio de notificaciones (Firestore).
        notificationRepository: NotificationRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): NotifyUseCase = NotifyUseCase(notificationRepository)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee AwardPointsUseCase, que otorga puntos de reputación una sola vez por hecho y avisa si se sube de nivel.
    fun providesAwardPointsUseCase(
        // Repositorio de reputación: puntos, historial e insignias.
        reputationRepository: ReputationRepository,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): AwardPointsUseCase = AwardPointsUseCase(reputationRepository, notify)

    // Metricas

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee EvaluateMyBadgesUseCase, que revisa los logros del usuario y desbloquea las insignias que ya cumple.
    fun providesEvaluateMyBadgesUseCase(
        // Repositorio de reputación: puntos, historial e insignias.
        reputationRepository: ReputationRepository,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requiredUser: RequireUserUseCase,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): EvaluateMyBadgesUseCase = EvaluateMyBadgesUseCase(reputationRepository, userRepository, postRepository, requiredUser, notify)


    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetPostStatsUseCase, que calcula las estadísticas de las publicaciones del usuario para el dashboard de métricas.
    fun providesGetPostStatusUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requiredUser: RequireUserUseCase
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetPostStatsUseCase = GetPostStatsUseCase(postRepository, requiredUser)

    // Moderación

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetModerationStatsUseCase, que calcula los contadores del panel de moderación.
    fun providesGetModerationStatsUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetModerationStatsUseCase = GetModerationStatsUseCase(postRepository, requireModerator)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetModerationQueueUseCase, que obtiene la cola de publicaciones pendientes de revisión.
    fun providesGetModerationQueueUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetModerationQueueUseCase = GetModerationQueueUseCase(postRepository, requireModerator)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetCurrentModeratorUseCase, que obtiene el perfil del moderador de la sesión.
    fun providesGetCurrentModeratorUseCase(
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetCurrentModeratorUseCase = GetCurrentModeratorUseCase(requireModerator, userRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee StartReviewUseCase, que asigna una publicación pendiente al moderador y la pasa a revisión.
    fun providesStartReviewUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): StartReviewUseCase = StartReviewUseCase(postRepository, requireModerator)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetMyReviewsUseCase, que lista las publicaciones que el moderador tiene en revisión o ya revisó.
    fun providesGetMyReviewsUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetMyReviewsUseCase = GetMyReviewsUseCase(postRepository, requireModerator)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetModeratorPerformanceUseCase, que calcula el rendimiento del moderador (revisiones por periodo).
    fun providesGetModeratorPerformanceUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetModeratorPerformanceUseCase = GetModeratorPerformanceUseCase(postRepository, requireModerator)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee UpdateModeratorTwoFactorUseCase, que activa o desactiva la A2F del moderador.
    fun providesUpdateModeratorTwoFactorUseCase(
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): UpdateModeratorTwoFactorUseCase = UpdateModeratorTwoFactorUseCase(requireModerator, userRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ApprovePostUseCase, que aprueba una publicación, notifica al autor y le otorga puntos.
    fun providesApprovePostUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase,
        // Caso de uso que otorga puntos de reputación una sola vez por hecho y avisa si se sube de nivel (Hilt lo construye con su propio proveedor).
        awardPoints: AwardPointsUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ApprovePostUseCase = ApprovePostUseCase(postRepository, requireModerator, notify, awardPoints)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee RejectPostUseCase, que rechaza una publicación con su motivo, notifica al autor y ajusta puntos.
    fun providesRejectPostUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que la sesión sea de un moderador válido (Hilt lo construye con su propio proveedor).
        requireModerator: RequireModeratorUseCase,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase,
        // Caso de uso que otorga puntos de reputación una sola vez por hecho y avisa si se sube de nivel (Hilt lo construye con su propio proveedor).
        awardPoints: AwardPointsUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): RejectPostUseCase = RejectPostUseCase(postRepository, requireModerator, notify, awardPoints)



    // Configuración

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee RequireOwnUserUseCase, que garantiza que el usuario solo modifique su propia cuenta.
    fun providesRequireOwnUserUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): RequireOwnUserUseCase = RequireOwnUserUseCase(authRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee UpdateSettingsUseCase, que guarda los ajustes de la cuenta (privacidad, notificaciones, A2F).
    fun providesUpdateSettingsUseCase(
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
        // Caso de uso que garantiza que el usuario solo modifique su propia cuenta (Hilt lo construye con su propio proveedor).
        requireOwnUser: RequireOwnUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): UpdateSettingsUseCase = UpdateSettingsUseCase(userRepository, requireOwnUser)

    // Editar perfil

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee UpdateProfileUseCase, que actualiza los datos del perfil y sube la nueva foto si cambió.
    fun providesUpdateProfileUseCase(
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
        // Caso de uso que garantiza que el usuario solo modifique su propia cuenta (Hilt lo construye con su propio proveedor).
        requireOwnUser: RequireOwnUserUseCase,
        // Caso de uso que sube imágenes locales al almacenamiento remoto y devuelve sus URLs (Hilt lo construye con su propio proveedor).
        uploadImages: UploadImagesUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): UpdateProfileUseCase = UpdateProfileUseCase(userRepository, requireOwnUser, uploadImages)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ChangePasswordUseCase, que cambia la contraseña tras confirmar la actual.
    fun providesChangePasswordUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ChangePasswordUseCase = ChangePasswordUseCase(authRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee DeleteAccountUseCase, que elimina la cuenta: cierra publicaciones, anonimiza el perfil y borra la identidad.
    fun providesDeleteAccountUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que el usuario solo modifique su propia cuenta (Hilt lo construye con su propio proveedor).
        requireOwnUser: RequireOwnUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): DeleteAccountUseCase = DeleteAccountUseCase(authRepository, userRepository, postRepository, requireOwnUser)


    // Perfil de otra persona

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetUserProfileUseCase, que obtiene el perfil público de otra persona.
    fun providesGetUserProfileUseCase(
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetUserProfileUseCase = GetUserProfileUseCase(userRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetUserPostsUseCase, que lista las publicaciones visibles de otra persona.
    fun providesGetUserPostsUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetUserPostsUseCase = GetUserPostsUseCase(postRepository, userRepository, authRepository)


    // Mis publicaciones y detalle

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetMyPostsUseCase, que lista las publicaciones del usuario agrupables por estado.
    fun providesGetMyPostsUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetMyPostsUseCase = GetMyPostsUseCase(postRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee FinishPostUseCase, que marca una publicación propia como finalizada (por ejemplo, mascota adoptada).
    fun providesFinishPostUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase,
        // Caso de uso que otorga puntos de reputación una sola vez por hecho y avisa si se sube de nivel (Hilt lo construye con su propio proveedor).
        awardPoints: AwardPointsUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): FinishPostUseCase = FinishPostUseCase(postRepository, notify, awardPoints)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee DeletePostUseCase, que elimina (de forma lógica) una publicación propia.
    fun providesDeletePostUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): DeletePostUseCase = DeletePostUseCase(postRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetPostDetailUseCase, que obtiene el detalle de una publicación.
    fun providesGetPostDetailUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetPostDetailUseCase = GetPostDetailUseCase(postRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee SendAdoptionInterestUseCase, que registra el interés de adopción y abre el chat con el autor.
    fun providesSendAdoptionInterestUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Repositorio de catálogos: categorías y tipos de animal.
        catalogRepository: CatalogRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): SendAdoptionInterestUseCase =
        SendAdoptionInterestUseCase(chatRepository, postRepository, catalogRepository, requireUser, notify)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetMyAdoptionRequestUseCase, que consulta si el usuario ya envió una solicitud de adopción para la publicación.
    fun providesGetMyAdoptionRequestUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetMyAdoptionRequestUseCase = GetMyAdoptionRequestUseCase(chatRepository, requireUser)


    // Chats

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee RequireChatMemberUseCase, que garantiza que el usuario de la sesión participe en el chat.
    fun providesRequireChatMemberUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): RequireChatMemberUseCase = RequireChatMemberUseCase(chatRepository, requireUser)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetMyChatsUseCase, que lista paginada de las conversaciones del usuario.
    fun providesGetMyChatsUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetMyChatsUseCase = GetMyChatsUseCase(chatRepository, requireUser)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee CountUnreadChatsUseCase, que cuenta los chats con mensajes sin leer.
    fun providesCountUnreadChatsUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): CountUnreadChatsUseCase = CountUnreadChatsUseCase(chatRepository, requireUser)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ObserveMessagesUseCase, que expone en vivo los últimos mensajes de un chat.
    fun providesObserveMessagesUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que el usuario de la sesión participe en el chat (Hilt lo construye con su propio proveedor).
        requireChatMember: RequireChatMemberUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ObserveMessagesUseCase = ObserveMessagesUseCase(chatRepository, requireChatMember)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetOlderMessagesUseCase, que carga mensajes anteriores de un chat.
    fun providesGetOlderMessagesUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que el usuario de la sesión participe en el chat (Hilt lo construye con su propio proveedor).
        requireChatMember: RequireChatMemberUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetOlderMessagesUseCase = GetOlderMessagesUseCase(chatRepository, requireChatMember)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee SendMessageUseCase, que envía un mensaje (con imagen opcional) y notifica al otro participante.
    fun providesSendMessageUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que el usuario de la sesión participe en el chat (Hilt lo construye con su propio proveedor).
        requireChatMember: RequireChatMemberUseCase,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase,
        // Caso de uso que sube imágenes locales al almacenamiento remoto y devuelve sus URLs (Hilt lo construye con su propio proveedor).
        uploadImages: UploadImagesUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): SendMessageUseCase = SendMessageUseCase(chatRepository, requireChatMember, notify, uploadImages)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee MarkChatReadUseCase, que marca un chat como leído por el usuario.
    fun providesMarkChatReadUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que el usuario de la sesión participe en el chat (Hilt lo construye con su propio proveedor).
        requireChatMember: RequireChatMemberUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): MarkChatReadUseCase = MarkChatReadUseCase(chatRepository, requireChatMember)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee DeleteChatUseCase, que oculta o elimina una conversación.
    fun providesDeleteChatUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Caso de uso que garantiza que el usuario de la sesión participe en el chat (Hilt lo construye con su propio proveedor).
        requireChatMember: RequireChatMemberUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): DeleteChatUseCase = DeleteChatUseCase(chatRepository, requireChatMember)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee StartChatUseCase, que abre (o reutiliza) un chat con el autor de una publicación.
    fun providesStartChatUseCase(
        // Repositorio de chats, mensajes y solicitudes de adopción (Firestore).
        chatRepository: ChatRepository,
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): StartChatUseCase = StartChatUseCase(chatRepository, postRepository, requireUser)


    // Comentarios

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetCommentsUseCase, que lista paginada de comentarios y respuestas de una publicación.
    fun providesGetCommentsUseCase(
        // Repositorio de comentarios y sus "me gusta" (Firestore).
        commentRepository: CommentRepository,
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
        // Caso de uso que obtiene el detalle de una publicación (Hilt lo construye con su propio proveedor).
        getPostDetail: GetPostDetailUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetCommentsUseCase = GetCommentsUseCase(commentRepository, authRepository, getPostDetail)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee CreateCommentUseCase, que publica un comentario o respuesta, notifica y otorga puntos.
    fun providesCreateCommentUseCase(
        // Repositorio de comentarios y sus "me gusta" (Firestore).
        commentRepository: CommentRepository,
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Repositorio de perfiles de usuario y moderador (Firestore).
        userRepository: UserRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
        // Caso de uso que crea notificaciones para otros usuarios (comentarios, mensajes, aprobaciones, insignias) (Hilt lo construye con su propio proveedor).
        notify: NotifyUseCase,
        // Caso de uso que otorga puntos de reputación una sola vez por hecho y avisa si se sube de nivel (Hilt lo construye con su propio proveedor).
        awardPoints: AwardPointsUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): CreateCommentUseCase =
        CreateCommentUseCase(commentRepository, postRepository, userRepository, requireUser, notify, awardPoints)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee ToggleCommentLikeUseCase, que da o quita "me gusta" a un comentario.
    fun providesToggleCommentLikeUseCase(
        // Repositorio de comentarios y sus "me gusta" (Firestore).
        commentRepository: CommentRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): ToggleCommentLikeUseCase = ToggleCommentLikeUseCase(commentRepository, requireUser)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetLikedCommentsUseCase, que indica a qué comentarios dio "me gusta" el usuario.
    fun providesGetLikedCommentsUseCase(
        // Repositorio de comentarios y sus "me gusta" (Firestore).
        commentRepository: CommentRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetLikedCommentsUseCase = GetLikedCommentsUseCase(commentRepository, requireUser)


    // Cerrar sesión

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee LogoutUseCase, que cierra la sesión del usuario.
    fun providesLogoutUseCase(
        // Repositorio de autenticación (Firebase Auth): sesión y cuenta actual.
        authRepository: AuthRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): LogoutUseCase = LogoutUseCase(authRepository)


    // Me gusta

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee GetMyImportantPostsUseCase, que lista las publicaciones que el usuario marcó como importantes.
    fun providesGetMyImportantPostsUseCase(
        // Repositorio de votos "Es importante" (Firestore).
        voteRepository: ImportantVoteRepository,
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que garantiza que haya un usuario común con sesión y devuelve su cuenta (Hilt lo construye con su propio proveedor).
        requireUser: RequireUserUseCase,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): GetMyImportantPostsUseCase = GetMyImportantPostsUseCase(voteRepository, postRepository, requireUser)


    // Crear publicación

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee CheckPostCatalogUseCase, que verifica que la categoría y el tipo de animal elegidos existan y estén activos.
    fun providesCheckPostCatalogUseCase(
        // Repositorio de catálogos: categorías y tipos de animal.
        catalogRepository: CatalogRepository,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): CheckPostCatalogUseCase = CheckPostCatalogUseCase(catalogRepository)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee CreatePostUseCase, que crea una publicación: valida el catálogo, sube las fotos y programa el análisis con IA.
    fun providesCreatePostUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que verifica que la categoría y el tipo de animal elegidos existan y estén activos (Hilt lo construye con su propio proveedor).
        checkPostCatalog: CheckPostCatalogUseCase,
        // Caso de uso que sube imágenes locales al almacenamiento remoto y devuelve sus URLs (Hilt lo construye con su propio proveedor).
        uploadImages: UploadImagesUseCase,
        // Planificador que lanza el análisis con IA en segundo plano.
        analysisScheduler: PostAnalysisScheduler,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): CreatePostUseCase = CreatePostUseCase(postRepository, checkPostCatalog, uploadImages, analysisScheduler)

    // Indica a Hilt que esta función es la receta para crear el caso de uso de abajo.
    @Provides
    // Una sola instancia por ViewModel: se comparte en él y se descarta cuando el ViewModel se destruye.
    @ViewModelScoped
    // Provee UpdatePostUseCase, que edita una publicación propia: valida el catálogo, sube las fotos nuevas y reprograma el análisis.
    fun providesUpdatePostUseCase(
        // Repositorio de publicaciones (Firestore).
        postRepository: PostRepository,
        // Caso de uso que verifica que la categoría y el tipo de animal elegidos existan y estén activos (Hilt lo construye con su propio proveedor).
        checkPostCatalog: CheckPostCatalogUseCase,
        // Caso de uso que sube imágenes locales al almacenamiento remoto y devuelve sus URLs (Hilt lo construye con su propio proveedor).
        uploadImages: UploadImagesUseCase,
        // Planificador que lanza el análisis con IA en segundo plano.
        analysisScheduler: PostAnalysisScheduler,
    // Crea el caso de uso entregándole por constructor las dependencias recibidas.
    ): UpdatePostUseCase = UpdatePostUseCase(postRepository, checkPostCatalog, uploadImages, analysisScheduler)


    // Home Moderador
}