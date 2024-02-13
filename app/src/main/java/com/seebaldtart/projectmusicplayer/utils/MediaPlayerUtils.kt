package com.seebaldtart.projectmusicplayer.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object MediaPlayerUtils {
    @JvmStatic
    fun getMediaTime(time: Int): String {
        val min = (time / 1000) / 60
        val sec = (time / 1000) % 60
        val formatter: NumberFormat = DecimalFormat("00")
        val minutes = if (min < 10) {
            String.format(Locale.getDefault(), "%02d", min)
        } else {
            String.format(Locale.getDefault(), "%01d", min)
        }
        val seconds = formatter.format(sec.toLong())
        return "$minutes:$seconds"
    }
}