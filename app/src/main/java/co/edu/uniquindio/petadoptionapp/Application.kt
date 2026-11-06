package co.edu.uniquindio.petadoptionapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
Clase Application de la app: es el primer objeto que Android crea al iniciar el proceso,
antes de cualquier Activity. Su única responsabilidad aquí es servir de punto de arranque
para Hilt: la anotación @HiltAndroidApp genera el contenedor de dependencias global
(SingletonComponent) del que salen Firebase, los repositorios de la capa de datos y los
casos de uso del dominio que luego se inyectan en MainActivity y en los ViewModels.
Se registra en el AndroidManifest.xml mediante el atributo android:name.
**/
// Indica a Hilt que genere el componente raíz de inyección de dependencias ligado al ciclo de vida de la aplicación.
@HiltAndroidApp
// Declara la clase de aplicación del proyecto; hereda de Application sin lógica adicional porque Hilt hace el trabajo.
class PetAdoptionApp : Application()