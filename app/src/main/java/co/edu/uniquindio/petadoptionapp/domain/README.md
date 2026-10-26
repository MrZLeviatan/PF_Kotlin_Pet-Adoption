<h1 align="center">

_Capa de Dominio_

</h1>

La capa `domain` contiene las reglas y conceptos centrales de la aplicación de adopción de mascotas. Define qué información maneja el sistema, qué acciones se pueden realizar y qué operaciones de datos necesita el negocio.

Su objetivo es mantener la lógica del proyecto separada de las pantallas y de la tecnología de almacenamiento. La capa de dominio no dibuja interfaces ni implementa Firebase, bases de datos o servicios externos.

<br>

## ¿Qué contiene?

### `model/`

Contiene los modelos del negocio: publicaciones, usuarios, ubicaciones, comentarios, chats, solicitudes de adopción, notificaciones y reputación, entre otros.

Los modelos representan datos y, cuando corresponde, reglas pequeñas asociadas a ellos. Por ejemplo, `Post` determina si una publicación cumple las condiciones para aparecer en el feed público.

Consulta [model/README.md](./model/README.md) para ver la descripción de sus carpetas y relaciones.

<br>

### `repository/`

Contiene interfaces que definen las operaciones que el dominio necesita para consultar o modificar datos. Por ejemplo, `PostRepository` declara cómo crear y consultar publicaciones, pero no especifica dónde se guardan.

Las implementaciones concretas pertenecen a `data/`.

Consulta [repository/README.md](./repository/README.md) para conocer cada contrato.

<br>

### `usecase/`

Contiene las acciones de la aplicación y sus reglas de negocio. Los casos de uso validan datos, comprueban permisos y coordinan uno o más repositorios.

Por ejemplo, un caso de uso de aprobación verifica que quien actúa sea un moderador autorizado y luego solicita al repositorio actualizar la publicación.

Consulta [usecase/README.md](./usecase/README.md) para ver las funciones organizadas por área.

<br>

### `error/`

Contiene errores propios del dominio, agrupados por área funcional: autenticación, usuarios, publicaciones, comentarios, chats, catálogos, notificaciones, adopción y votos.

Los casos de uso devuelven estos errores (frecuentemente mediante `Result`) para que la capa de presentación pueda mostrar una respuesta apropiada. El dominio define el significado del error; la interfaz decide cómo comunicarlo.

<br>

## Cómo se conectan las partes

```text
Pantalla / ViewModel
        ↓
Caso de uso
  aplica reglas y coordina la acción
        ↓
Modelos del dominio
  representan información y estados
        ↓
Interfaces de repositorio
  declaran operaciones de datos
        ↓
Implementaciones en data/
  ejecutan consultas y cambios
```

Los casos de uso pueden utilizar modelos, validadores y otros casos de uso. Cuando necesitan datos, dependen de interfaces de repositorio, no de las implementaciones concretas. Así, el almacenamiento puede cambiar sin trasladar esas reglas a las pantallas.

<br>

## Relación con otras capas

- **`features/`**: presenta la interfaz y sus ViewModels. Estos llaman a los casos de uso.
- **`data/`**: implementa los repositorios y proporciona los datos.
- **`di/`**: conecta cada interfaz de repositorio con su implementación mediante Hilt.
- **`core/`**: reúne elementos compartidos de la interfaz, como tema y componentes; no forma parte de las reglas del dominio.

<br>

## Alcance y límites

- `domain` describe las reglas y operaciones del negocio; no garantiza que cada caso de uso esté conectado a una pantalla.
- Los contratos de repositorio no son tablas ni una base de datos.
- La lógica del dominio debe evitar depender de Jetpack Compose, componentes visuales o implementaciones específicas de almacenamiento.
