package com.seebaldtart.projectmusicplayer.dependencyinjection

import com.seebaldtart.projectmusicplayer.utils.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers

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
}