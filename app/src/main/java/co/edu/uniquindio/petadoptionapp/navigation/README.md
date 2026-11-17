# Navegación

## Propósito

Esta carpeta define el recorrido entre pantallas. Utiliza **Navigation Compose** con rutas tipadas: cada destino es un objeto o una clase de Kotlin anotada con `@Serializable`, y sus argumentos (por ejemplo, el identificador de una publicación) viajan como propiedades, sin construir cadenas de texto manualmente.

Las pantallas no se conocen entre sí. Cada una expone funciones como `onPostClick(postId)` y esta capa decide a qué destino conducen.

## Contenido por archivo

| Archivo | Responsabilidad | Elementos principales |
|---|---|---|
| `MainRoutes.kt` | Catálogo de destinos de nivel superior. Los `data object` representan pantallas sin argumentos y las `data class` llevan los argumentos necesarios. | `Splash`, `Login`, `Register`, `RecoverPassword`, `NewPassword`, `Main`, `CreatePost`, `EditPost(postId)`, `PostDetail(postId)`, `PostComments(postId, replyToCommentId)`, `Chat(chatId)`, `UserProfile(userId)`, `RejectPost(postId)`, `Settings`, `EditProfile`, `Notifications`, `Metrics`, `MyPosts`, `MyLikes` |
| `AppNavigation.kt` | Grafo de navegación raíz. Registra cada ruta en un `NavHost`, crea su pantalla y conecta sus eventos con acciones del `NavController`. Lo monta `MainActivity`. | `AppNavigation()` |

## Flujo de la aplicación

```text
Splash
 ├─ sesión válida ──────────────────────────────► Main
 └─ sin sesión ──► Login
                    ├─ credenciales correctas ──► (código A2F, si aplica) ──► Main
                    ├─► Register
                    └─► RecoverPassword

Main (barra inferior, definida en features/dashboard)
 ├─ Usuario:    Inicio · Explorar · Mensajes · Perfil
 └─ Moderador:  Inicio · Publicaciones · Estadísticas · Perfil
        │
        └─► PostDetail ─► PostComments, Chat, UserProfile, EditPost, RejectPost
            CreatePost, Settings, EditProfile, Notifications, Metrics, MyPosts, MyLikes
```

## Comportamiento relevante

- **Limpieza de la pila.** Al iniciar o cerrar sesión, y al eliminar la cuenta, se elimina el historial con `popUpTo`, de modo que el botón "Atrás" nunca regresa a una sesión anterior.
- **Argumentos.** Los ViewModels leen los argumentos de la ruta desde `SavedStateHandle` (por ejemplo, `savedStateHandle["chatId"]`).
- **Navegación interna del panel.** Las pestañas de la barra inferior tienen su propio `NavHost`, definido en `features/dashboard/DashboardNavigation.kt` con las rutas de `DashboardRoutes`.

## Guía de extensión

1. Declarar la ruta en `MainRoutes.kt`.
2. Registrar su `composable<…>` en `AppNavigation.kt` y conectar los eventos de la pantalla.
3. Si la pantalla necesita datos de entrada, declararlos como propiedades de la ruta y leerlos en el ViewModel.
