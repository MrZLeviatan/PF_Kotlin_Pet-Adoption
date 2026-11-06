<h1 align="center">

_Inyección de dependencias (di)_

</h1>

La carpeta `di` configura **Hilt**, la librería que crea los objetos de la aplicación y se los entrega a quien los necesita. Gracias a ella ninguna clase construye sus dependencias a mano: un ViewModel pide un caso de uso por su constructor, el caso de uso pide un repositorio del dominio, y Hilt arma toda la cadena.

<br>

## Módulos

### `FirebaseModule.kt` — `SingletonComponent`
Entrega una única instancia de `FirebaseAuth` (con los correos en el idioma de la app) y de `FirebaseFirestore` para toda la aplicación. Ambas leen su configuración de `app/google-services.json`.

### `RepositoryModule.kt` — `SingletonComponent`
Une cada **interfaz del dominio** con su **implementación** de `data` mediante `@Binds @Singleton`:

```kotlin
@Binds @Singleton abstract fun bindPost(impl: FirestorePostRepository): PostRepository
```

Es el único lugar donde se decide qué tecnología usa la app. Cambiar Firestore por otra base de datos sería escribir otra implementación y cambiar esta línea.

### `UseCaseModule.kt` — `ViewModelComponent`
Fabrica los casos de uso con `@Provides @ViewModelScoped`. Los casos de uso del dominio son clases Kotlin puras (sin anotaciones de Hilt): este módulo es la "receta" que les pasa sus repositorios y otros casos de uso. Viven mientras vive el ViewModel que los pidió.

<br>

## Cómo fluye una dependencia

```text
@HiltViewModel LoginViewModel(loginUseCase)
        ▲ UseCaseModule.provideLoginUseCase(authRepository, checkProfile)
        │       ▲ RepositoryModule.bindAuth(FirebaseAuthRepository) → AuthRepository
        │       │       ▲ FirebaseModule.provideFirebaseAuth() / provideFirestore()
        │       │       ▲ EmailJsCodeSender (@Inject constructor)
```

- **Singleton**: repositorios, Firebase y servicios remotos (una instancia para toda la app; comparten caché, sesión y listeners).
- **ViewModelScoped**: casos de uso.
- `PetAdoptionApp` (`@HiltAndroidApp`) arranca Hilt; `MainActivity` (`@AndroidEntryPoint`) permite inyectar en las pantallas.
