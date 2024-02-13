package com.seebaldtart.projectmusicplayer.dependencyinjection

import com.seebaldtart.projectmusicplayer.repositories.AudioTrackRepository
import com.seebaldtart.projectmusicplayer.repositories.AudioTrackRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindsAudioTrackRepository(repo: AudioTrackRepositoryImpl): AudioTrackRepository

}