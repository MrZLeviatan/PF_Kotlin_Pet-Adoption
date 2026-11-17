# Inyección de Dependencias

## Propósito

La aplicación utiliza **Hilt** (basado en Dagger) para construir sus objetos y entregarlos a quien los necesita. Ninguna clase crea sus dependencias manualmente: un ViewModel declara en su constructor el caso de uso que requiere, el caso de uso declara sus repositorios y Hilt resuelve toda la cadena. Esta carpeta contiene los módulos que indican a Hilt cómo construir cada pieza.

## Contenido por archivo

| Archivo | Componente | Responsabilidad | Funciones principales |
|---|---|---|---|
| `FirebaseModule.kt` | `SingletonComponent` | Entrega una única instancia de Firebase Authentication (con los correos en el idioma de la aplicación) y de Cloud Firestore. Ambas leen su configuración de `google-services.json`. | `provideFirebaseAuth`, `provideFirestore` |
| `RepositoryModule.kt` | `SingletonComponent` | Enlaza cada contrato del dominio con su implementación mediante `@Binds @Singleton`. Es el único lugar donde se decide qué tecnología usa la aplicación. | `bindAuth`, `bindUser`, `bindPost`, `bindCatalog`, `bindComment`, `bindVote`, `bindNotification`, `bindChat`, `bindReputation`, `bindImageStorage`, `bindAiAnalysis`, `bindPostAnalysisScheduler`, `bindGeocoding` |
| `UseCaseModule.kt` | `ViewModelComponent` | Construye cada caso de uso con `@Provides @ViewModelScoped`, entregándole sus repositorios y los casos de uso de los que depende. | Una función `provide…UseCase` por caso de uso |

## Ciclo de vida de las dependencias

| Ámbito | Qué contiene | Motivo |
|---|---|---|
| `Singleton` | Firebase, repositorios y servicios remotos | Una sola instancia para toda la aplicación: comparten caché, sesión y *listeners* |
| `ViewModelScoped` | Casos de uso | Viven mientras exista el ViewModel que los solicitó |

## Cadena de resolución

```text
@HiltViewModel LoginViewModel(loginUseCase, verifyTwoFactorCode, …)
   └─ UseCaseModule.provideLoginUseCase(authRepository, checkProfile)
        └─ RepositoryModule.bindAuth: AuthRepository → FirebaseAuthRepository
             ├─ FirebaseModule.provideFirebaseAuth()
             ├─ FirebaseModule.provideFirestore()
             └─ EmailJsCodeSender (@Inject constructor)
```

## Puntos de entrada

| Clase | Anotación | Función |
|---|---|---|
| `PetAdoptionApp` (`Application.kt`) | `@HiltAndroidApp` | Crea el contenedor global de dependencias al iniciar el proceso |
| `MainActivity` | `@AndroidEntryPoint` | Habilita la inyección en la actividad y en sus pantallas |
| ViewModels | `@HiltViewModel` | Se obtienen en las pantallas con `hiltViewModel()` |

## Decisiones de diseño

- **Dominio sin Hilt.** Los casos de uso son clases Kotlin puras sin anotaciones; `UseCaseModule` actúa como fábrica. Esto mantiene el dominio independiente del framework.
- **Reemplazo de tecnología localizado.** Sustituir Firestore por otra base de datos solo requiere escribir la nueva implementación en `data/` y cambiar la línea correspondiente en `RepositoryModule`.

## Guía de extensión

- Un caso de uso nuevo requiere una función `@Provides` en `UseCaseModule`.
- Un repositorio nuevo requiere su interfaz en el dominio, su implementación anotada con `@Inject constructor` y un `@Binds` en `RepositoryModule`.
