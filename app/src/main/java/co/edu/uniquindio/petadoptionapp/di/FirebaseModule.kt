package co.edu.uniquindio.petadoptionapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

/**
Módulo de Hilt que entrega las instancias de Firebase al resto de la app.
Ambas leen su configuración de app/google-services.json (procesado por el plugin google-services).
Pertenece a la capa de inyección de dependencias (di): los repositorios de la capa de datos
(data/repository/firebase) reciben FirebaseAuth y FirebaseFirestore por constructor gracias a
estos proveedores, de modo que ninguna clase llama directamente a getInstance().
**/
// Declara la clase como módulo de Dagger, es decir, una fuente de dependencias.
@Module
// Instala el módulo en el componente Singleton: sus objetos viven mientras viva la aplicación.
@InstallIn(SingletonComponent::class)
// Se usa un object porque los proveedores no necesitan estado ni instancia propia.
object FirebaseModule {

    // Indica a Hilt que esta función fabrica una dependencia que puede inyectar.
    @Provides
    // Garantiza una única instancia compartida de FirebaseAuth en toda la app.
    @Singleton
    // Entrega la instancia de Firebase Authentication configurada para la app.
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        // Los correos de Firebase (por ejemplo, recuperar contraseña) salen en el idioma de la app.
        useAppLanguage()
    }

    // Indica a Hilt que esta función fabrica la dependencia de la base de datos.
    @Provides
    // Garantiza una única instancia de Firestore; así su caché local se comparte en toda la app.
    @Singleton
    // Entrega Firestore, que guarda una copia local: la app abre rápido y funciona sin conexión.
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
