package com.seebaldtart.projectmusicplayer.services

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException

interface ThumbnailService {
    @Throws(
        CancellationException::class,
        ExecutionException::class,
        InterruptedException::class
    )
    /** This function attempts to retrieve the [Bitmap] image associated with the provided [uri].
     * The caller of this function should handle errors in the form of a [Throwable].
     *
     * @param context The current [Context] of the caller or the application context.
     * @param uri The [Uri] of the image source. This may be a file path or URL.
     * @param size The desired width/height size, represented by [Size], of the image to return
     * @throws CancellationException If the computation was cancelled
     * @throws ExecutionException If the computation threw an exception
     * @throws InterruptedException If the current thread was interrupted */
    fun fetchBitmap(context: Context, uri: Uri, size: Size?): Bitmap
}