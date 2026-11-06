package co.edu.uniquindio.petadoptionapp.di

import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirebaseAuthRepository
import co.edu.uniquindio.petadoptionapp.data.repository.remote.CloudinaryImageRepository
import co.edu.uniquindio.petadoptionapp.data.repository.remote.OpenRouterAiRepository
import co.edu.uniquindio.petadoptionapp.data.repository.remote.BackgroundPostAnalysisScheduler
import co.edu.uniquindio.petadoptionapp.domain.repository.AiAnalysisRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostAnalysisScheduler
import co.edu.uniquindio.petadoptionapp.data.repository.device.AndroidGeocodingRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.GeocodingRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ImageStorageRepository
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCatalogRepository
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreChatRepository
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreCommentRepository
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreImportantVoteRepository
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreNotificationRepository
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestorePostRepository
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreReputationRepository
import co.edu.uniquindio.petadoptionapp.data.repository.firebase.FirestoreUserRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.CommentRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ImportantVoteRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

/**
Une cada interfaz del dominio con su implementación: Firebase (Auth y Firestore) para los datos,
Cloudinary para las imágenes, OpenRouter para la IA y el geocodificador del teléfono para direcciones.
Anota la clase abstracta como un módulo de Dagger e indica a Hilt que se instale en el componente Singleton.
*/
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Enlaza Firebase Auth (con la A2F por correo) como repositorio de autenticación.
    @Binds @Singleton abstract fun bindAuth(impl: FirebaseAuthRepository): AuthRepository
    // Enlaza Cloudinary como almacenamiento de imágenes.
    @Binds @Singleton abstract fun bindImageStorage(impl: CloudinaryImageRepository): ImageStorageRepository
    // Enlaza el geocodificador del teléfono (direcciones <-> puntos del mapa).
    @Binds @Singleton abstract fun bindGeocoding(impl: AndroidGeocodingRepository): GeocodingRepository
    // Enlaza OpenRouter como IA para analizar publicaciones, y quien lo lanza en segundo plano.
    @Binds @Singleton abstract fun bindAiAnalysis(impl: OpenRouterAiRepository): AiAnalysisRepository
    @Binds @Singleton abstract fun bindPostAnalysisScheduler(impl: BackgroundPostAnalysisScheduler): PostAnalysisScheduler
    // Enlaza la implementación del repositorio de usuarios con la interfaz UserRepository.
    @Binds @Singleton abstract fun bindUser(impl: FirestoreUserRepository): UserRepository
    // Enlaza la implementación del repositorio de catálogos con la interfaz CatalogRepository.
    @Binds @Singleton abstract fun bindCatalog(impl: FirestoreCatalogRepository): CatalogRepository
    // Enlaza la implementación del repositorio de publicaciones con la interfaz PostRepository.
    @Binds @Singleton abstract fun bindPost(impl: FirestorePostRepository): PostRepository
    // Enlaza la implementación del repositorio de comentarios con la interfaz CommentRepository.
    @Binds @Singleton abstract fun bindComment(impl: FirestoreCommentRepository): CommentRepository
    // Enlaza la implementación del repositorio de votos importantes con la interfaz ImportantVoteRepository.
    @Binds @Singleton abstract fun bindVote(impl: FirestoreImportantVoteRepository): ImportantVoteRepository
    // Enlaza la implementación del repositorio de notificaciones con la interfaz NotificationRepository.
    @Binds @Singleton abstract fun bindNotification(impl: FirestoreNotificationRepository): NotificationRepository
    // Enlaza la implementación del repositorio de chats con la interfaz ChatRepository.
    @Binds @Singleton abstract fun bindChat(impl: FirestoreChatRepository): ChatRepository
    // Enlaza la implementación del repositorio de reputación con la interfaz ReputationRepository.
    @Binds @Singleton abstract fun bindReputation(impl: FirestoreReputationRepository): ReputationRepository
}