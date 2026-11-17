<h1 align="center">

_Inyección de dependencias (di)_

</h1>

**En pocas palabras:** las clases de la app necesitan otras para funcionar (un ViewModel necesita un caso de uso, y este, un repositorio). En lugar de crearlas a mano, **Hilt** las fabrica y las entrega. Esta carpeta le dice a Hilt **cómo** fabricar cada una.

<br>

## Qué hay aquí

| Archivo | Qué entrega | Cuánto vive |
|---|---|---|
| `FirebaseModule.kt` | `FirebaseAuth` y `FirebaseFirestore` | Toda la app (una sola instancia) |
| `RepositoryModule.kt` | Qué implementación usa cada contrato del dominio | Toda la app |
| `UseCaseModule.kt` | Los casos de uso, con sus repositorios | Mientras viva la pantalla que los pidió |

<br>

## Cómo funciona

`RepositoryModule` es el lugar donde se decide la tecnología:

```kotlin
// "Cuando alguien pida un PostRepository, entrégale FirestorePostRepository"
@Binds @Singleton abstract fun bindPost(impl: FirestorePostRepository): PostRepository
```

Así se arma la cadena completa, por ejemplo, para el login:

```text
LoginViewModel
  └─ necesita LoginUseCase                 ← UseCaseModule lo fabrica
       └─ necesita AuthRepository          ← RepositoryModule: FirebaseAuthRepository
            └─ necesita FirebaseAuth       ← FirebaseModule
```

- `PetAdoptionApp` (`@HiltAndroidApp`) arranca Hilt y `MainActivity` (`@AndroidEntryPoint`) lo habilita en las pantallas.
- Los ViewModels se marcan con `@HiltViewModel` y se obtienen con `hiltViewModel()`.

<br>

## Para desarrolladores

- **¿Un caso de uso nuevo?** Agrega su función `@Provides` en `UseCaseModule`. Los casos de uso no llevan anotaciones de Hilt: así el dominio sigue siendo Kotlin puro.
- **¿Cambiar de tecnología** (por ejemplo, otra base de datos)? Escribe la nueva implementación en `data/` y cambia una línea en `RepositoryModule`. Nada más se entera.
