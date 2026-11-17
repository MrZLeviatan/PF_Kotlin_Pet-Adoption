<h1 align="center">

_Pantallas (features)_

</h1>

**En pocas palabras:** aquí está **todo lo que se ve** en la app, organizado por funcionalidad. Cada pantalla tiene dos partes: la **pantalla** (lo que dibuja Jetpack Compose) y su **ViewModel** (el que guarda el estado y pide las acciones al dominio).

<br>

## Cómo está hecha cada pantalla (MVVM)

```text
   La persona toca o escribe
             │
             ▼
   Screen ──────────► ViewModel ──────────► Casos de uso (domain)
     ▲                    │
     └──── estado ◄───────┘
```

| Pieza | Qué es | Ejemplo |
|---|---|---|
| `XxxUiState` | Todo lo que la pantalla muestra en un momento dado | `LoginUiState(email, password, isLoading, loginError)` |
| `XxxEvent` | Avisos de una sola vez, como "navegar" | `LoginEvent.LoginSuccess` |
| `XxxViewModel` | Recibe las acciones, llama a los casos de uso y actualiza el estado | `LoginViewModel.onLoginClick()` |
| `XxxScreen` | Conecta el ViewModel y reacciona a los eventos | `LoginScreen` |
| `XxxContent` | Solo dibuja; por eso se puede previsualizar | `LoginContent` |

Los errores del dominio se convierten en mensajes de `strings.xml` (español, inglés y francés). La persona nunca ve un error técnico.

<br>

## Funcionalidades

### Inicio y cuenta
| Carpeta | Qué hace |
|---|---|
| `splash/` | Pantalla de carga. Si hay sesión guardada entra directo; si no, va al login |
| `auth/login/` | Inicio de sesión y diálogo del código de verificación (A2F) |
| `auth/register/` | Registro en dos pasos: datos personales y ubicación en el mapa |
| `auth/recoverpassword/` | Pide el correo para restablecer la contraseña |

### Personas usuarias (`user/`)
| Carpeta | Qué hace |
|---|---|
| `home/` | Feed de publicaciones verificadas, con filtros y orden |
| `explore/` | Mapa con un marcador por publicación (ícono y color de su categoría), filtros y búsqueda de lugares |
| `post/create/` | Crear y editar una publicación por pasos: datos, mascota, fotos y ubicación |
| `post/detail/` | Detalle: fotos, zona aproximada, "Es importante", comentarios, "Me interesa adoptar" y acciones del autor o del moderador |
| `post/comments/` | Comentarios con respuestas y "me gusta" |
| `post/myposts/` · `likes/` | Mis publicaciones y las que marqué como importantes |
| `chat/` | Lista de chats y conversación en vivo, con fotos y solicitudes de adopción |
| `profile/` | Perfil, editar perfil (foto, datos y ubicación), ajustes y métricas |
| `publicprofile/` | Perfil de otra persona, según su privacidad |

### Moderadores (`moderator/`)
| Carpeta | Qué hace |
|---|---|
| `home/` | Cola de revisión por prioridad, con el resumen de IA y el botón "Analizar con IA" |
| `reviews/` | Publicaciones que el moderador tomó o revisó |
| `rejectPost/` | Rechazar con un motivo obligatorio |
| `stats/` | Estadísticas: aprobadas, rechazadas, actividad semanal |
| `profile/` | Datos y ajustes del moderador |

### Compartido
| Carpeta | Qué hace |
|---|---|
| `dashboard/` | La barra inferior. Muestra pestañas distintas según el rol |
| `notification/` | Bandeja de notificaciones; cada una abre su publicación, chat o métricas |

<br>

## Para desarrolladores

- **¿Una pantalla nueva?** Crea `XxxUiState`, `XxxViewModel` (con `@HiltViewModel`) y `XxxScreen`/`XxxContent`; luego agrega su ruta en `navigation/`.
- **Las pantallas no conocen Firebase:** solo llaman casos de uso. Si necesitas datos nuevos, empieza por el dominio.
- **Navegar:** la pantalla recibe funciones como `onPostClick(postId)` y no sabe a dónde llevan; eso lo decide `navigation/AppNavigation.kt`.
- **Textos:** agrégalos en `res/values/strings.xml` y en sus traducciones `values-en/` y `values-fr/`.
