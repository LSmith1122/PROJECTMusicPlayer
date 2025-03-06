@file:Suppress(
    "FunctionName",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.seebaldtart.projectmusicplayer.ui.components.playlist

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.seebaldtart.projectmusicplayer.R
import com.seebaldtart.projectmusicplayer.ui.theme.PROJECTMusicPlayerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlaylistAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBackPressed: () -> Unit
) {
    PROJECTMusicPlayerTheme {
        TopAppBar(
            modifier = Modifier.navigationBarsPadding(),
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                IconButton(onClick = {
                    onBackPressed.invoke()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_options_menu),
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                scrolledContainerColor = MaterialTheme.colorScheme.secondary,
                titleContentColor = Color.White
            ),
            title = {
                stringResource(R.string.app_name)
            }
        )
    }
}