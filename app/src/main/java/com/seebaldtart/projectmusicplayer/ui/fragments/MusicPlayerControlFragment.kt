package com.seebaldtart.projectmusicplayer.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.seebaldtart.projectmusicplayer.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicPlayerControlFragment : Fragment() {

//    val viewModel: IMusicPlayerStateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_player_control, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MusicPlayerControlFragment()
    }
}