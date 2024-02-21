package com.seebaldtart.projectmusicplayer.services

import android.content.Context
import android.net.Uri
import android.util.Size
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import javax.inject.Inject

class ThumbnailServiceImpl @Inject constructor()
    : ThumbnailService {
    override fun fetchBitmap(
        context: Context,
        uri: Uri,
        size: Size?
    ) = Glide.with(context)
        .load(uri)
        .submit()
        .get()
        .let { drawable ->
            val width = size?.width ?: drawable.intrinsicWidth
            val height = size?.height ?: drawable.intrinsicHeight
            drawable.toBitmap(width, height)
        }
}
