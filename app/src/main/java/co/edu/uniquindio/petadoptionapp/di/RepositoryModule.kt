package co.edu.uniquindio.petadoptionapp.di

import co.edu.uniquindio.petadoptionapp.data.repository.memory.AuthRepositoryImpl
import co.edu.uniquindio.petadoptionapp.data.repository.memory.CatalogRepositoryImpl
import co.edu.uniquindio.petadoptionapp.data.repository.memory.ChatRepositoryImpl
import co.edu.uniquindio.petadoptionapp.data.repository.memory.CommentRepositoryImpl
import co.edu.uniquindio.petadoptionapp.data.repository.memory.ImportantVoteRepositoryImpl
import co.edu.uniquindio.petadoptionapp.data.repository.memory.NotificationRepositoryImpl
import co.edu.uniquindio.petadoptionapp.data.repository.memory.PostRepositoryImpl
import co.edu.uniquindio.petadoptionapp.data.repository.memory.ReputationRepositoryImpl
import co.edu.uniquindio.petadoptionapp.data.repository.memory.UserRepositoryImpl
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
Une cada interfaz del dominio con su implementación.
Anota la clase abstracta como un módulo de Dagger e indica a Hilt que se instale en el componente Singleton.
*/
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Enlaza la implementación en memoria del repositorio de autenticación con su respectiva interfaz del dominio como un singleton.
    @Binds @Singleton abstract fun bindAuth(impl: AuthRepositoryImpl): AuthRepository
    // Enlaza la implementación del repositorio de usuarios con la interfaz UserRepository.
    @Binds @Singleton abstract fun bindUser(impl: UserRepositoryImpl): UserRepository
    // Enlaza la implementación del repositorio de catálogos con la interfaz CatalogRepository.
    @Binds @Singleton abstract fun bindCatalog(impl: CatalogRepositoryImpl): CatalogRepository
    // Enlaza la implementación del repositorio de publicaciones con la interfaz PostRepository.
    @Binds @Singleton abstract fun bindPost(impl: PostRepositoryImpl): PostRepository
    // Enlaza la implementación del repositorio de comentarios con la interfaz CommentRepository.
    @Binds @Singleton abstract fun bindComment(impl: CommentRepositoryImpl): CommentRepository
    // Enlaza la implementación del repositorio de votos importantes con la interfaz ImportantVoteRepository.
    @Binds @Singleton abstract fun bindVote(impl: ImportantVoteRepositoryImpl): ImportantVoteRepository
    // Enlaza la implementación del repositorio de notificaciones con la interfaz NotificationRepository.
    @Binds @Singleton abstract fun bindNotification(impl: NotificationRepositoryImpl): NotificationRepository
    // Enlaza la implementación del repositorio de chats con la interfaz ChatRepository.
    @Binds @Singleton abstract fun bindChat(impl: ChatRepositoryImpl): ChatRepository
    // Enlaza la implementación del repositorio de reputación con la interfaz ReputationRepository.
    @Binds @Singleton abstract fun bindReputation(impl: ReputationRepositoryImpl): ReputationRepository
}