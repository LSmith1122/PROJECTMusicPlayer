package com.seebaldtart.projectmusicplayer.ui.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.models.enums.GroupItemSelectionState
import com.seebaldtart.projectmusicplayer.repositories.AudioTrackRepository
import com.seebaldtart.projectmusicplayer.ui.fragments.AudioPlayListFragment
import com.seebaldtart.projectmusicplayer.ui.fragments.AudioPlayListSelectionFragment
import com.seebaldtart.projectmusicplayer.viewmodels.AudioPlayListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
private const val READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO"

@AndroidEntryPoint
class MusicPlayerActivity : FragmentActivity() {

    @Inject
    lateinit var audioTrackRepo: AudioTrackRepository

    private val audioPlayListViewModel: AudioPlayListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                audioPlayListViewModel.groupItemSelectionState
                    .onEach {
                        handleFragmentByState(it)
                    }.collect()
            }
        }

        checkPermissions()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Still using onBackPressed to support versions < Tiramisu
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            // TODO: Prompt user to either stay in app or quit the app
            super.onBackPressed()
        }
    }

    private fun handleFragmentByState(state: GroupItemSelectionState) {
        when (state) {
            GroupItemSelectionState.AUDIO_PLAY_LIST_SELECTION -> showAudioPlayListSelection()
            GroupItemSelectionState.AUDIO_PLAY_LIST -> showAudioPlayList()
            GroupItemSelectionState.AUDIO_PLAY_DETAILS -> TODO()
        }
    }

    private fun showAudioPlayListSelection() {
        val fragmentName = AudioPlayListSelectionFragment::class.java.simpleName
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack(
                fragmentName,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.content_container,
                AudioPlayListSelectionFragment(),
                fragmentName
            ).commit()
    }

    private fun showAudioPlayList() {
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.content_container,
                AudioPlayListFragment(),
                AudioPlayListFragment::class.java.simpleName
            ).commit()
    }

    private fun checkPermissions() {
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var missingPermission = false
            permissions.entries.forEach { entry ->
                when (entry.key) {
                    READ_EXTERNAL_STORAGE -> {
                        val isValidVersion = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2
                        if (isValidVersion && !entry.value) {
                            missingPermission = true
                        }
                    }

                    READ_MEDIA_AUDIO -> {
                        if (!entry.value) {
                            missingPermission = true
                        }
                    }
                }
            }

            if (missingPermission) {
                finishAffinity()
            } else {
                audioTrackRepo.initialize()
            }
        }.launch(
            arrayOf(
                READ_EXTERNAL_STORAGE,
                READ_MEDIA_AUDIO
            )
        )
    }
}
