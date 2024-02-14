@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.seebaldtart.projectmusicplayer.ui.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.ui.fragments.AudioPlayListFragment
import dagger.hilt.android.AndroidEntryPoint

private const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
private const val READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO"

@AndroidEntryPoint
class MusicPlayerActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.content_container, AudioPlayListFragment())
            .commitNow()

        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var missingPermission = false
            permissions.entries.find { it.key == READ_EXTERNAL_STORAGE }
                ?.let { readExternal ->
                    val isValidVersion = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2
                    if (isValidVersion && !readExternal.value) {
                        missingPermission = true
                    }
                }
            permissions.entries.find { it.key == READ_MEDIA_AUDIO }
                ?.let { readMediaAudio ->
                    if (!readMediaAudio.value) {
                        missingPermission = true
                    }
                }

            if (missingPermission) {
                finishAffinity()
            }
        }.launch(arrayOf(
            READ_EXTERNAL_STORAGE,
            READ_MEDIA_AUDIO
        ))
    }
}
