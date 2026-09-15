package com.devpro.sound.di

import com.devpro.sound.data.remote.datasource.SongRemoteDataSource
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.data.repository.impl.SongRepositoryImpl
import com.devpro.sound.player.AudioPlayer
import com.devpro.sound.player.AudioPlayerManager
import android.content.Context
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
    fun provideFirestore(): FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }
    @Provides
    @Singleton
    fun provideSongRemoteDataSource(firestore: FirebaseFirestore): SongRemoteDataSource{
        return SongRemoteDataSource(firestore)
    }
    @Provides
    @Singleton
    fun provideSongRepository(dataSource: SongRemoteDataSource): SongRepository{
        return SongRepositoryImpl(dataSource)
    }
    @Provides
    @Singleton
    fun provideAudioPlayer(@ApplicationContext context: Context): AudioPlayer{
        return AudioPlayerManager(context)
    }
}
