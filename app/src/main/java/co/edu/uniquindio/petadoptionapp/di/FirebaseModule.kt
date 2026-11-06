package co.edu.uniquindio.petadoptionapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

/**
Entrega las instancias de Firebase. Leen su configuración de app/google-services.json.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        // Los correos de Firebase (recuperar contraseña) salen en el idioma de la app.
        useAppLanguage()
    }

    // Firestore guarda una copia local: la app abre rápido y funciona sin conexión.
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
