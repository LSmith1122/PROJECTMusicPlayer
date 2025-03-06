package com.seebaldtart.projectmusicplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.seebaldtart.projectmusicplayer.ui.theme.SMALL_COMPONENT_BLUR_ALPHA
import com.seebaldtart.projectmusicplayer.ui.theme.SMALL_COMPONENT_BLUR_RADIUS

@Composable
fun RowScope.PlaybackControlButton(
    modifier: Modifier = Modifier,
    imageResID: Int,
    contentDescription: String? = null,
    indication: Indication = rememberRipple(bounded = false),
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .height(60.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = indication,
                onClick = onClick
            ).then(modifier)
    ) {
        Box {
            Icon(
                modifier = Modifier
                    .blur(
                        radius = SMALL_COMPONENT_BLUR_RADIUS,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    ).alpha(SMALL_COMPONENT_BLUR_ALPHA),
                tint = Color.Black,
                painter = painterResource(imageResID),
                contentDescription = contentDescription
            )
            Image(
                painter = painterResource(imageResID),
                contentDescription = contentDescription
            )
        }
    }
}