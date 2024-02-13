package com.seebaldtart.projectmusicplayer.utils

import kotlinx.coroutines.CoroutineDispatcher

data class DispatcherProvider(
    val main: CoroutineDispatcher,
    val unconfined: CoroutineDispatcher,
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher
)
