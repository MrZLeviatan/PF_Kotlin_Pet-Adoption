<h1 align="center">

_Navegación_

</h1>

La carpeta `navigation` define **cómo se pasa de una pantalla a otra**. Usa Navigation Compose con **rutas con tipo** (`@Serializable`): cada destino es un objeto o una clase de Kotlin, y sus argumentos (por ejemplo, `postId`) viajan como propiedades, sin construir textos de URL a mano.

<br>

## Archivos

### `MainRoutes.kt`
Rutas de nivel superior: `Splash`, `Login`, `Register`, `RecoverPassword`, `Main` (el dashboard con barra inferior), `PostDetail(postId)`, `EditPost(postId)`, `PostComments(postId, replyToCommentId)`, `Chat(chatId)`, `UserProfile(userId)`, `RejectPost(postId)`, `CreatePost`, `Settings`, `EditProfile`, `Notifications`, `Metrics`, `MyPosts`, `MyLikes`.

### `AppNavigation.kt`
El `NavHost` principal. Para cada ruta crea su pantalla y le entrega lambdas de navegación:

```kotlin
composable<MainRoutes.PostDetail> {
    PostDetailScreen(onOpenChat = { chatId -> navController.navigate(MainRoutes.Chat(chatId)) }, …)
}
```

Los ViewModels leen sus argumentos de la ruta desde `SavedStateHandle` (por ejemplo, `savedStateHandle["chatId"]`), así las pantallas no los reciben por parámetro.

<br>

## Flujo principal

```text
Splash ──(hay sesión)──▶ Main
   └──(sin sesión)──▶ Login ──▶ (A2F en diálogo) ──▶ Main
                        ├──▶ Register
                        └──▶ RecoverPassword

Main (barra inferior, features/dashboard)
  usuario:    Inicio · Explorar · Mensajes · Perfil
  moderador:  Inicio · Publicaciones · Estadísticas · Perfil
     └──▶ PostDetail, Chat, UserProfile, CreatePost, Settings, Notifications…
```

- Al iniciar sesión, cerrarla o eliminar la cuenta se limpia la pila (`popUpTo`): "atrás" nunca vuelve a una pantalla de la sesión anterior.
- Las pestañas de la barra inferior tienen su propio `NavHost` en `features/dashboard/DashboardNavigation.kt` (rutas en `DashboardRoutes`).
