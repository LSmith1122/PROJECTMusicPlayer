package com.seebaldtart.projectmusicplayer.dependencyinjection

import com.seebaldtart.projectmusicplayer.services.ThumbnailService
import com.seebaldtart.projectmusicplayer.services.ThumbnailServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ServiceModule {
    @Singleton
    @Binds
    fun bindsThumbnailService(service: ThumbnailServiceImpl): ThumbnailService
}
