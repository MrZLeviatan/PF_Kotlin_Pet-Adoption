<h1 align="center">

_Capa de Presentación (features)_

</h1>

La carpeta `features` contiene **todas las pantallas** de la aplicación, organizadas por funcionalidad. Cada pantalla sigue el patrón **MVVM** con Jetpack Compose: un `ViewModel` guarda el estado y llama a los casos de uso; la pantalla solo dibuja ese estado y le reenvía las acciones del usuario.

<br>

## Patrón de cada pantalla

```text
            acciones del usuario (onClick, onValueChange…)
  Screen ───────────────────────────────────────────────▶ ViewModel ──▶ casos de uso (domain)
    ▲                                                        │
    └──────── uiState (StateFlow)  +  events (Channel) ◀─────┘
```

- **`XxxUiState`**: `data class` inmutable con todo lo que la pantalla muestra (campos, errores, cargando…). Se actualiza con `_uiState.update { it.copy(...) }`.
- **`XxxEvent`**: eventos de un solo uso (navegar, cerrar un diálogo) enviados por un `Channel`, para que no se repitan al rotar la pantalla.
- **Errores**: el ViewModel traduce las excepciones del dominio a enums de la UI (`LoginError.INVALID_CREDENTIALS`); la pantalla los convierte en textos de `strings.xml` (español, inglés y francés).
- **`XxxScreen`** conecta el ViewModel (`hiltViewModel()`), observa el estado con `collectAsStateWithLifecycle()` y escucha eventos en un `LaunchedEffect`.
- **`XxxContent`** solo dibuja: no conoce el ViewModel, por eso se puede previsualizar con `@Preview`.
- Los ViewModels son `@HiltViewModel`: Hilt les inyecta los casos de uso (fabricados en `di/UseCaseModule.kt`).

<br>

## Funcionalidades

### `splash/`
Pantalla de carga. Mientras muestra la marca, `SplashViewModel` intenta **recuperar la sesión guardada** (`RestoreSessionUseCase`): con sesión va directo al inicio; sin ella, al login.

### `auth/`
- **`login/`**: inicio de sesión. Si la cuenta tiene A2F, abre `TwoFactorDialog` (una casilla por dígito, "Verificar código", "Reenviar código" con cuenta regresiva de 30 s; la X cancela y cierra la sesión pendiente).
- **`register/`**: registro en dos pasos. Paso 1: datos personales, con revisión de que el correo y el teléfono no estén usados. Paso 2: ciudad y dirección (búsqueda o toque en el mapa). El alias se genera solo.
- **`recoverpassword/`**: pide el correo de recuperación a Firebase (dice lo mismo exista o no la cuenta).
- **`newpassword/`**: pantalla heredada; con Firebase la contraseña nueva se escribe en el enlace del correo.

### `dashboard/`
Contenedor con la **barra inferior**. `MainScreen` lee el rol de la sesión y muestra las pestañas de usuario (Inicio, Explorar, Mensajes, Perfil) o de moderador (Inicio, Publicaciones, Estadísticas, Perfil). `DashboardNavigation` es el `NavHost` interno de esas pestañas.

### `user/`
- **`home/`**: feed de publicaciones verificadas con filtros y orden.
- **`explore/`**: mapa (MapLibre) con un marcador por publicación; ícono, color y forma salen de la categoría. Filtros, leyenda, búsqueda de lugares y una hoja con el resumen de la publicación elegida.
- **`post/create/`**: crear y editar publicación por pasos (datos, fotos con cámara o galería, ubicación actual o en el mapa). Al guardar, las fotos suben a Cloudinary y el análisis de IA se programa en segundo plano.
- **`post/detail/`**: detalle con fotos, ubicación aproximada, "Es importante", comentarios, "Me interesa adoptar", acciones del autor (editar, finalizar) y del moderador (tomar, aprobar, rechazar, análisis de IA).
- **`post/comments/`**: comentarios con respuestas y "me gusta".
- **`post/myposts/`**, **`likes/`**: mis publicaciones y las que marqué como importantes.
- **`chat/`**: lista de chats y conversación en vivo, con fotos y la solicitud de adopción (aceptar o rechazar).
- **`profile/`**: perfil, edición (foto, nombre, teléfono, ubicación), ajustes (idioma, privacidad, A2F, contraseña, cerrar sesión, eliminar cuenta) y métricas (puntos, nivel, insignias, compartir).
- **`publicprofile/`**: perfil de otra persona según su privacidad (los moderadores ven el perfil completo).

### `moderator/`
- **`home/`**: cola de revisión ordenada por prioridad, tarjetas por prioridad que filtran, resumen de IA en cada publicación y botón "Analizar con IA (N sin analizar)".
- **`reviews/`**: publicaciones que el moderador tomó o revisó.
- **`rejectPost/`**: rechazo con motivo obligatorio.
- **`stats/`**: estadísticas del moderador (aprobadas, rechazadas, actividad de 7 días, por categoría y prioridad).
- **`profile/`**: datos y ajustes del moderador.
- **`AiReviewViews.kt`** y **`PriorityUi.kt`**: piezas compartidas (resumen y tarjeta de IA, colores y textos de prioridad).

### `notification/`
Bandeja de notificaciones: comentarios, verificaciones, votos, solicitudes y respuestas de adopción, mensajes, insignias y niveles. Cada una abre su destino (publicación, chat o métricas).

<br>

## Conexión con las demás capas

```text
navigation/AppNavigation ──▶ Screen ──▶ ViewModel ──▶ domain/usecase ──▶ domain/repository ◀── data
                                │
                                └──▶ core/component, core/theme, core/util
```

- Las pantallas **no conocen Firebase** ni los repositorios: solo casos de uso.
- La navegación entre pantallas la resuelve `navigation/`; las pantallas reciben lambdas como `onPostClick(postId)` y no saben a dónde llevan.
