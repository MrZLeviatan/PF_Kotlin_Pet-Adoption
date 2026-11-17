<h1 align="center">

_Navegación_

</h1>

**En pocas palabras:** esta carpeta decide **a qué pantalla se va** después de cada acción. Usa Navigation Compose con rutas tipadas: cada pantalla es un objeto de Kotlin y sus datos (por ejemplo, el id de la publicación) viajan como propiedades.

<br>

## Qué hay aquí

| Archivo | Qué contiene |
|---|---|
| `MainRoutes.kt` | La lista de destinos: `Splash`, `Login`, `Register`, `Main`, `PostDetail(postId)`, `Chat(chatId)`, `UserProfile(userId)`, `CreatePost`, `EditPost(postId)`, `Settings`, `Notifications`… |
| `AppNavigation.kt` | El mapa de navegación: para cada destino, qué pantalla se muestra y a dónde lleva cada botón |

```kotlin
composable<MainRoutes.PostDetail> {
    PostDetailScreen(onOpenChat = { chatId -> navController.navigate(MainRoutes.Chat(chatId)) })
}
```

<br>

## Recorrido de la app

```text
Splash ── ¿hay sesión? ──sí──► Main
   └──no──► Login ──► (código A2F) ──► Main
              ├──► Registro
              └──► Recuperar contraseña

Main = barra inferior (features/dashboard)
   Usuario:    Inicio · Explorar · Mensajes · Perfil
   Moderador:  Inicio · Publicaciones · Estadísticas · Perfil
        └──► Detalle, Chat, Perfil público, Publicar, Ajustes, Notificaciones…
```

- Al iniciar o cerrar sesión se limpia el historial: el botón "atrás" nunca vuelve a la sesión anterior.
- Las pestañas de la barra inferior tienen su propia navegación en `features/dashboard/DashboardNavigation.kt`.

<br>

## Para desarrolladores

- **¿Una pantalla nueva?** Agrega su ruta en `MainRoutes.kt` y su `composable<…>` en `AppNavigation.kt`.
- **¿Pasar un dato?** Ponlo como propiedad de la ruta (`data class PostDetail(val postId: String)`); el ViewModel lo lee de `SavedStateHandle`.
