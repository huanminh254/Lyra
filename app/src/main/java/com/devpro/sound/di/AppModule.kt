package com.devpro.sound.di

import com.devpro.sound.data.audio.AudioWaveformExtractor
import com.devpro.sound.data.local.PlaybackStateStore
import com.devpro.sound.data.local.SharedPreferencesPlaybackStateStore
import com.devpro.sound.data.remote.datasource.SongRemoteDataSource
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.data.repository.impl.SongRepositoryImpl
import com.devpro.sound.player.AudioPlayer
import com.devpro.sound.player.AudioPlayerManager
import android.content.Context
import com.devpro.sound.data.remote.datasource.AuthRemoteDataSource
import com.devpro.sound.data.remote.datasource.UserRemoteDataSource
import com.devpro.sound.data.remote.datasource.SongUploadRemoteDataSource
import com.devpro.sound.data.remote.datasource.CommentRemoteDataSource
import com.devpro.sound.data.remote.storage.SupabaseStorageClient
import com.devpro.sound.data.repository.CommentRepository
import com.devpro.sound.data.repository.UserRepository
import com.devpro.sound.data.repository.impl.UserRepositoryImpl
import com.devpro.sound.data.repository.impl.CommentRepositoryImpl
import com.devpro.sound.data.repository.AuthRepository
import com.devpro.sound.data.repository.impl.AuthRepositoryImpl
import com.devpro.sound.data.repository.UploadSongRepository
import com.devpro.sound.data.repository.impl.UploadSongRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePlaybackStateStore(
        @ApplicationContext context: Context
    ): PlaybackStateStore = SharedPreferencesPlaybackStateStore(context)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth{
        return FirebaseAuth.getInstance()
    }
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAudioWaveformExtractor(
        @ApplicationContext context: Context
    ): AudioWaveformExtractor {
        return AudioWaveformExtractor(context)
    }
    @Provides
    @Singleton
    fun provideSongRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): SongRemoteDataSource {
        return SongRemoteDataSource(firestore, firebaseAuth)
    }
    @Provides
    @Singleton
    fun provideSongRepository(
        dataSource: SongRemoteDataSource
    ): SongRepository{
        return SongRepositoryImpl(dataSource)
    }

    @Provides
    @Singleton
    fun provideCommentRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): CommentRemoteDataSource {
        return CommentRemoteDataSource(firestore, firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(
        dataSource: CommentRemoteDataSource
    ): CommentRepository {
        return CommentRepositoryImpl(dataSource)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(@ApplicationContext context: Context): AudioPlayer{
        return AudioPlayerManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        firebaseAuth: FirebaseAuth
    ): AuthRemoteDataSource {
        return AuthRemoteDataSource(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        dataSource: AuthRemoteDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(dataSource)
    }

    @Provides
    @Singleton
    fun provideSongUploadRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        audioWaveformExtractor: AudioWaveformExtractor,
        supabaseStorageClient: SupabaseStorageClient
    ): SongUploadRemoteDataSource {
        return SongUploadRemoteDataSource(
            firestore,
            firebaseAuth,
            audioWaveformExtractor,
            supabaseStorageClient
        )
    }

    @Provides
    @Singleton
    fun provideUploadSongRepository(
        dataSource: SongUploadRemoteDataSource
    ): UploadSongRepository {
        return UploadSongRepositoryImpl(dataSource)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
        firestore: FirebaseFirestore, firebaseAuth: FirebaseAuth
    ) : UserRemoteDataSource{
        return UserRemoteDataSource(firestore, firebaseAuth)
    }
    @Provides
    @Singleton
    fun provideUserRepository(dataSource: UserRemoteDataSource): UserRepository{
        return UserRepositoryImpl(dataSource)
    }
}
