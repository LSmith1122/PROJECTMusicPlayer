package com.seebaldtart.projectmusicplayer.utils

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

object PermissionHelper {
    private const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
    private const val READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO"

    @JvmStatic
    fun checkNecessaryPermissions(activity: ComponentActivity,
                                  onSuccess: () -> Unit,
                                  onFailure: (Map<String, Boolean>) -> Unit) {
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var missingPermission = false
            permissions.entries.forEach { entry ->
                when (entry.key) {
                    READ_EXTERNAL_STORAGE -> {
                        val isValidVersion = Build.VERSION.SDK_INT in Build.VERSION_CODES.KITKAT .. Build.VERSION_CODES.S_V2
                        if (isValidVersion && !entry.value) {
                            missingPermission = true
                        }
                    }
                    READ_MEDIA_AUDIO -> {
                        val isValidVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        if (isValidVersion && !entry.value) {
                            missingPermission = true
                        }
                    }
                }
            }

            if (missingPermission) {
                onFailure.invoke(permissions.filter { !it.value })
            } else {
                onSuccess.invoke()
            }
        }.launch(
            arrayOf(
                READ_EXTERNAL_STORAGE,
                READ_MEDIA_AUDIO
            )
        )
    }
}