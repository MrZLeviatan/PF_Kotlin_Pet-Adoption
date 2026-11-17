# Capa de Presentación (features)

## Propósito

La capa de presentación contiene todas las pantallas de la aplicación, organizadas por funcionalidad. Cada pantalla se construye con **Jetpack Compose** siguiendo el patrón **MVVM**: un `ViewModel` mantiene el estado y ejecuta las acciones a través de los casos de uso del dominio, y la pantalla dibuja ese estado y reenvía las interacciones de la persona usuaria.

Las pantallas no conocen Firebase ni los repositorios; únicamente invocan casos de uso.

## Patrón de cada pantalla

```text
     Interacción de la persona (onClick, onValueChange)
  Screen ─────────────────────────────────────────► ViewModel ─────► Casos de uso
    ▲                                                   │
    └──── uiState (StateFlow) + events (Channel) ◄──────┘
```

| Elemento | Responsabilidad |
|---|---|
| `XxxUiState` | `data class` inmutable con todo lo que la pantalla muestra: valores, errores e indicadores de carga. Se actualiza con `_uiState.update { it.copy(...) }`. |
| `XxxEvent` | Eventos de una sola ocurrencia (navegar, cerrar un diálogo), enviados por un `Channel` para que no se repitan al rotar la pantalla. |
| `XxxViewModel` | Anotado con `@HiltViewModel`. Recibe los casos de uso por constructor, procesa las acciones y traduce los errores del dominio a errores de interfaz. |
| `XxxScreen` | Obtiene el ViewModel con `hiltViewModel()`, observa el estado con `collectAsStateWithLifecycle()` y atiende los eventos en un `LaunchedEffect`. |
| `XxxContent` | Función sin estado que solo dibuja; se puede previsualizar con `@Preview`. |

Los textos y mensajes de error provienen de `res/values/strings.xml` y sus traducciones al inglés (`values-en`) y al francés (`values-fr`).

## Estructura

```text
features/
├── splash/            Pantalla de carga y restauración de sesión
├── auth/              Inicio de sesión, verificación en dos pasos, registro y recuperación
├── dashboard/         Contenedor con barra inferior según el rol
├── notification/      Bandeja de notificaciones
├── user/              Funcionalidades de la persona usuaria
└── moderator/         Funcionalidades del moderador
```

## Contenido por módulo

### `splash/`

| Archivo | Responsabilidad |
|---|---|
| `SplashScreen.kt` | Muestra la identidad de la aplicación mientras, en paralelo, se recupera la sesión. Informa a la navegación si existe una sesión válida. |
| `SplashViewModel.kt` | Expone `hasSession()`, que ejecuta `RestoreSessionUseCase`. |

### `auth/`

| Archivo | Responsabilidad | Casos de uso |
|---|---|---|
| `login/LoginScreen.kt` | Formulario de correo y contraseña, mensajes de error y enlaces a registro y recuperación. Abre el diálogo de verificación cuando corresponde. | — |
| `login/LoginViewModel.kt` | Valida los campos, inicia sesión y gestiona el estado del código A2F: dígitos, verificación, reenvío con cuenta regresiva de 30 segundos y cancelación. | `LoginUseCase`, `VerifyTwoFactorCodeUseCase`, `ResendTwoFactorCodeUseCase`, `LogoutUseCase` |
| `login/TwoFactorDialog.kt` | Diálogo con una casilla por dígito, botón de verificación y reenvío. | — |
| `register/RegisterScreen.kt` | Registro en dos fases: datos personales con indicador de seguridad de la contraseña, y ubicación en el mapa. | — |
| `register/RegisterViewModel.kt` | Valida cada campo, comprueba la disponibilidad del correo y el teléfono antes de la segunda fase, genera el alias y crea la cuenta. | `CheckRegistrationAvailabilityUseCase`, `RegisterUseCase` |
| `recoverpassword/RecoverPasswordScreen.kt` | Solicita el correo para restablecer la contraseña. | — |
| `recoverpassword/RecoverPasswordViewModel.kt` | Envía la solicitud y muestra el resultado; responde igual exista o no la cuenta. | `SendPasswordResetUseCase` |
| `newpassword/NewPasswordScreen.kt` / `NewPasswordViewModel.kt` | Pantalla heredada de definición de contraseña. Con Firebase, la nueva contraseña se define desde el enlace del correo. | — |

### `dashboard/`

| Archivo | Responsabilidad |
|---|---|
| `MainScreen.kt` | Contenedor principal: lee el rol de la sesión y muestra la barra inferior y las pantallas correspondientes. |
| `DashboardViewModel.kt` | Expone el rol a partir de `ObserveSessionUseCase`. |
| `DashboardDestination.kt` | Opciones de la barra inferior: texto, ícono, roles permitidos y ruta. |
| `DashboardRoutes.kt` | Rutas internas: `HomeUser`, `Explore`, `Messages`, `Profile`, `HomeMonitor`, `Publications`, `ModeratorStats`. |
| `DashboardNavigation.kt` | `NavHost` interno de las pestañas. |
| `BottomNavigationBar.kt` | Barra inferior sin estado. |

### `notification/`

| Archivo | Responsabilidad | Casos de uso |
|---|---|---|
| `NotificationsScreen.kt` | Lista paginada con filtros (todas, sin leer, mensajes); cada notificación abre su destino. | — |
| `NotificationsViewModel.kt.kt` | Carga, filtra y marca notificaciones; resuelve el destino (publicación, chat o métricas). | `GetNotificationsUseCase`, `MarkNotificationReadUseCase`, `MarkAllNotificationsReadUseCase`, `DeleteNotificationUseCase` |

### `user/` — Persona usuaria

| Archivo | Responsabilidad | Casos de uso principales |
|---|---|---|
| `home/HomeUserScreen.kt` / `HomeUserViewModel.kt` | Feed de publicaciones con saludo, contador de notificaciones, búsqueda de publicaciones o personas (con "@") y voto "Es importante". | `GetFeedUseCase`, `ToggleImportantUseCase`, `SearchUsersUseCase`, `CountUnreadNotificationsUseCase` |
| `explore/ExploreScreen.kt` | Mapa MapLibre con las publicaciones cercanas; el color, el ícono y la forma de cada marcador provienen de su categoría. Incluye leyenda, búsqueda de lugares y hoja de resumen. | — |
| `explore/ExploreViewModel.kt` | Determina el centro (ubicación actual, del perfil o primera ciudad), aplica los filtros y calcula las distancias. | `GetFeedUseCase`, `ObserveCategoriesUseCase`, `ObserveCurrentUserUseCase`, `SearchAddressUseCase` |
| `explore/ExploreFiltersDialog.kt` | Filtros de categoría, tipo de animal, tamaño, vacunación, ciudad y distancia. | — |
| `post/create/CreatePostScreen.kt` / `CreatePostViewModel.kt` | Formulario de cuatro pasos para crear o editar: información, mascota, fotos y ubicación. | `CreatePostUseCase`, `UpdatePostUseCase`, `ReverseGeocodeUseCase` |
| `post/create/step/StepInfoContent.kt` | Paso 1: título, categoría y descripción. | — |
| `post/create/step/StepPetContent.kt` | Paso 2: tipo de animal, raza, tamaño, edad y salud. | — |
| `post/create/step/StepPhotosContent.kt` | Paso 3: fotos desde cámara o galería. | — |
| `post/create/step/StepLocationContent.kt` | Paso 4: ubicación actual o elegida en el mapa. | — |
| `post/create/step/StepErrors.kt` | Traducción de errores de campo a textos. | — |
| `post/detail/PostDetailScreen.kt` / `PostDetailViewModel.kt` | Detalle con fotos, zona aproximada, voto, comentarios y acciones según el rol: editar, eliminar y finalizar (autor); "Me interesa adoptar" y chat (otras personas); tomar, aprobar, rechazar y analizar con IA (moderador). | `GetPostDetailUseCase`, `StartReviewUseCase`, `ApprovePostUseCase`, `FinishPostUseCase`, `AnalyzePostUseCase`, `SendAdoptionInterestUseCase`, `StartChatUseCase` |
| `post/comments/CommentsScreen.kt` / `CommentsViewModel.kt` | Comentarios paginados con hilos de respuestas y "me gusta". | `GetCommentsUseCase`, `CreateCommentUseCase`, `ToggleCommentLikeUseCase` |
| `post/comments/CommentViews.kt` / `CommentUi.kt` | Componentes visuales y modelo de presentación de comentarios. | — |
| `post/myposts/MyPostsScreen.kt` / `MyPostsViewModel.kt` | Publicaciones propias agrupadas por estado, con acciones de finalizar y eliminar. | `GetMyPostsUseCase`, `FinishPostUseCase`, `DeletePostUseCase` |
| `likes/MyLikesScreen.kt` / `MyLikesViewModel.kt` | Publicaciones marcadas como importantes, con pestañas por categoría. | `GetMyImportantPostsUseCase`, `ToggleImportantUseCase` |
| `chat/list/ChatListScreen.kt` / `ChatListViewModel.kt` | Lista de chats con búsqueda, vista previa del último mensaje y no leídos; resuelve la otra persona y la publicación de cada chat. | `GetMyChatsUseCase`, `GetUserProfileUseCase`, `GetPostDetailUseCase`, `DeleteChatUseCase` |
| `chat/conversation/ChatScreen.kt` / `ChatViewModel.kt` | Conversación en tiempo real con texto y fotos, separadores por día, indicadores de enviado y visto, carga de mensajes anteriores y eliminación del chat. | `ObserveMessagesUseCase`, `SendMessageUseCase`, `GetOlderMessagesUseCase`, `MarkChatReadUseCase`, `DeleteChatUseCase` |
| `chat/ChatTime.kt` / `DeleteChatDialog.kt` | Formato de fechas del chat y diálogo de eliminación (para mí o para ambos). | — |
| `profile/ProfileScreen.kt` / `ProfileViewModel.kt` | Perfil propio con nivel de reputación y menú de accesos. | `ObserveCurrentUserUseCase` |
| `profile/edit/EditProfileScreen.kt` / `EditProfileViewModel.kt` | Edición de foto, nombre, teléfono y ubicación (actual o elegida en el mapa). | `UpdateProfileUseCase`, `ReverseGeocodeUseCase` |
| `profile/settings/SettingsScreen.kt` / `SettingsViewModel.kt` | Configuración de usuarios y moderadores: contraseña, verificación en dos pasos, privacidad, idioma, cierre de sesión y eliminación de cuenta. | `UpdateSettingsUseCase`, `ChangePasswordUseCase`, `DeleteAccountUseCase`, `LogoutUseCase` |
| `profile/metric/MetricsScreen.kt` / `MetricsViewModel.kt` | Métricas personales, nivel e insignias; permite compartir una captura. | `GetPostStatsUseCase`, `EvaluateMyBadgesUseCase`, `ObserveBadgesUseCase` |
| `profile/BadgeIcons.kt` | Asigna un ícono a cada tipo de requisito de insignia. | — |
| `publicprofile/UserProfileScreen.kt` / `UserProfileViewModel.kt` | Perfil de otra persona según su privacidad; los moderadores lo ven completo. | `GetUserProfileUseCase`, `GetUserPostsUseCase` |

### `moderator/` — Moderación

| Archivo | Responsabilidad | Casos de uso principales |
|---|---|---|
| `home/ModeratorHomeScreen.kt` / `ModeratorHomeViewModel.kt` | Inicio del moderador: tarjetas por prioridad que filtran la cola, búsqueda, resumen de IA en cada publicación y análisis por lotes de las publicaciones sin analizar. | `GetModerationStatsUseCase`, `GetModerationQueueUseCase`, `AnalyzePostUseCase` |
| `reviews/MyReviewsScreen.kt` / `MyReviewsViewModel.kt` | Publicaciones que el moderador tomó y aún no decide, con pestañas por prioridad. | `GetMyReviewsUseCase` |
| `rejectPost/RejectPostScreen.kt` / `RejectPostViewModel.kt` | Rechazo con motivo predefinido y descripción obligatoria. | `RejectPostUseCase` |
| `stats/ModeratorStatsScreen.kt` / `ModeratorStatsViewModel.kt` | Desempeño: aprobadas, rechazadas, en revisión, actividad de siete días y distribución por categoría y prioridad. | `GetModeratorPerformanceUseCase` |
| `profile/ModeratorProfileScreen.kt` / `ModeratorProfileViewModel.kt` | Datos del moderador y resumen de desempeño. | `GetCurrentModeratorUseCase`, `GetModeratorPerformanceUseCase` |
| `AiReviewViews.kt` | `AiReviewSummary` (versión breve para la cola) y `AiReviewCard` (versión completa para el detalle). | — |
| `PriorityUi.kt` | Color y texto localizado de cada nivel de prioridad. | — |

## Interconexión

```text
navigation/AppNavigation ──► XxxScreen ──► XxxViewModel ──► domain/usecase ──► domain/repository ◄── data
                                 │
                                 └──► core/component, core/theme, core/util
```

- La navegación entrega a cada pantalla funciones como `onPostClick(postId)`; la pantalla no conoce su destino.
- Los argumentos de navegación (por ejemplo, `postId`) se leen en el ViewModel desde `SavedStateHandle`.
- Las listas y el perfil de la sesión se actualizan en tiempo real gracias a los flujos expuestos por el dominio.

## Guía de extensión

1. Crear `XxxUiState`, `XxxViewModel` (con `@HiltViewModel`) y `XxxScreen` con su `XxxContent`.
2. Registrar la ruta en `navigation/MainRoutes.kt` y su pantalla en `AppNavigation.kt`.
3. Agregar los textos en `strings.xml` y en sus traducciones.
4. Si la pantalla requiere datos nuevos, comenzar por el caso de uso en el dominio.
