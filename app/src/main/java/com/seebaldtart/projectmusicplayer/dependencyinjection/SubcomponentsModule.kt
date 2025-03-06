package com.seebaldtart.projectmusicplayer.dependencyinjection

import android.app.Application
import android.content.Context
import android.media.AudioManager
import com.seebaldtart.projectmusicplayer.utils.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@InstallIn(SingletonComponent::class)
@Module
object SubcomponentsModule {
    @Provides
    @Reusable
    fun providesDispatcherContextProvider(): DispatcherProvider {
        return DispatcherProvider(
            Dispatchers.Main,
            Dispatchers.Unconfined,
            Dispatchers.IO,
            Dispatchers.Default
        )
    }

    @Provides
    @Reusable
    fun providesAudioManager(application: Application): AudioManager {
        return application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}