<h1 align="center">

_Núcleo compartido (core)_

</h1>

La carpeta `core` reúne todo lo que **varias pantallas comparten**: el sistema de diseño (tema, colores, tipografía), los componentes visuales reutilizables, el mapa y las utilidades del teléfono. No contiene reglas de negocio ni accede a Firebase: recibe datos ya listos y avisa de las acciones del usuario mediante *callbacks*.

Así, cada pantalla de `features/` se arma con piezas consistentes, y un cambio de diseño (un color, la forma de un botón) se hace en un solo lugar.

<br>

## Estructura

```text
core/
├── theme/       → Colores, tipografía, tema claro/oscuro (AppTheme)
├── component/   → Componentes de UI reutilizables (Compose)
│   ├── button/      PrimaryButton, SecondaryButton, DangerButton, BackButton, MenuActionButton…
│   ├── card/        AppCard, SectionCard, StatCard, PostSummaryCard, NotificationCard, BadgeItem…
│   ├── dialog/      AppInfoDialog, AppConfirmDialog, AppActionDialog, AppFormDialog
│   ├── form/        AppTextField, AppTextArea, AppChipGroup, FormDropdown, SwitchRow, PasswordStrengthBar…
│   ├── header/      AppTopBar e IconHeader
│   ├── icon/        Círculos e íconos de marca (PawCircle, IconCircle…)
│   ├── indicator/   Indicadores de progreso y pasos
│   ├── list/        Listas reutilizables
│   ├── map/         Mapa MapLibre, marcadores, selector de ubicación, ubicación actual
│   ├── navigation/  Barra de navegación inferior
│   ├── photo/       Selector y visor de fotos (cámara/galería)
│   └── text/        Textos de error, enlaces, títulos
└── util/        → Idioma, ubicación del teléfono, fotos locales, compartir, "hace X tiempo", alias aleatorio
```

<br>

## `theme/` — Sistema de diseño

- **`Color.kt`** define la paleta base (cafés, cremas, amarillo, rojo de error, tonos nocturnos).
- **`AppColors.kt`** define colores **semánticos** (`actionPrimary`, `surface`, `textPrimary`, `stateError`, `border`…) en dos juegos: `LightAppColors` y `DarkAppColors`. Las pantallas nunca usan un color "crudo": piden `AppTheme.colors.surface`, y el tema decide el valor según el modo claro u oscuro.
- **`Type.kt`** define la tipografía de Material 3 que usa toda la app.
- **`Theme.kt`** expone `PetAdoptionAppTheme { … }` (envuelve a `MaterialTheme` y entrega los colores con un `CompositionLocal`) y el acceso `AppTheme.colors`.
- **`LegacyColors.kt`** conserva nombres antiguos mientras se migran pantallas.

<br>

## `component/` — Componentes reutilizables

Reglas que siguen todos los componentes:

- **Sin estado de negocio**: reciben valores (`text`, `enabled`, `selected`…) y avisan con lambdas (`onClick`, `onValueChange`). Quien los usa (un ViewModel) decide qué pasa.
- **Accesibles**: área táctil mínima de 48 dp, `contentDescription` en íconos con significado, `Role` y `liveRegion` donde aplica (TalkBack anuncia errores).
- **Tematizados**: solo colores de `AppTheme.colors`, así funcionan en modo claro y oscuro.
- **Con `@Preview`** para verlos en Android Studio sin ejecutar la app.

### `component/map/` — Mapas con MapLibre

| Archivo | Función |
|---|---|
| `AppMap.kt` | Envuelve el `MapView` de MapLibre en Compose (`AndroidView`) y maneja su ciclo de vida. Dibuja marcadores con una `SymbolLayer`, áreas aproximadas con una `FillLayer` (fuentes GeoJSON) y detecta toques sobre marcadores con `queryRenderedFeatures`. |
| `MarkerImages.kt` | Genera los íconos de los marcadores como *bitmaps* a partir de íconos de Material (`CanvasDrawScope`): color, forma (círculo o cuadrado) e ícono; oscuro sobre fondos claros. |
| `MapModels.kt` | `GeoPoint`, `MapMarker`, `MarkerShape`, `MapArea`: datos simples que el mapa sabe dibujar. |
| `LocationPicker.kt` + `LocationPickerViewModel.kt` | Selector de ubicación: ciudad, búsqueda de dirección y toque en el mapa (usa los casos de uso de geocodificación). |
| `CurrentLocationRequester.kt` | Pide el permiso de ubicación y entrega la posición actual del teléfono. |

El estilo del mapa es **OpenFreeMap** (`https://tiles.openfreemap.org/styles/liberty`): gratuito y sin llaves.

<br>

## `util/` — Utilidades

| Archivo | Función |
|---|---|
| `AppLanguage.kt` | Idiomas de la app (español, inglés, francés) con `AppCompatDelegate.setApplicationLocales`; el cambio se aplica sin reiniciar. |
| `DeviceLocation.kt` | Lee la ubicación actual con `LocationManager`. |
| `ApproximateLocation.kt` | Desplaza de forma estable el punto de una publicación (radio de 400 m) para no revelar la dirección exacta. |
| `LocalImageStore.kt` | Copia a la carpeta privada de la app las fotos elegidas (cámara o galería), reducidas a 1600 px en JPEG, antes de subirlas a Cloudinary. |
| `ShareImage.kt` | Comparte una imagen (por ejemplo, la captura de las métricas) con un `FileProvider`. |
| `TimeAgo.kt` | Convierte fechas en textos como "hace 5 min". |
| `UserNameGenerator.kt` | Genera el alias aleatorio del registro a partir del nombre. |

<br>

## Conexión con las demás capas

```text
features/ (pantallas) ──usa──▶ core/component + core/theme + core/util
core/component/map/LocationPickerViewModel ──▶ domain/usecase/location
```

- `core` **no depende de `features`** ni de `data`. La única excepción controlada es el selector de ubicación, que tiene su propio ViewModel y usa casos de uso de `domain/usecase/location`.
- Las pantallas de `features` combinan estos componentes y les pasan su estado.
