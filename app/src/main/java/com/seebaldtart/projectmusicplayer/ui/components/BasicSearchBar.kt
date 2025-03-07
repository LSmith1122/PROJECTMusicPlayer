package com.seebaldtart.projectmusicplayer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seebaldtart.projectmusicplayer.ui.theme.DefaultBorderSize
import com.seebaldtart.projectmusicplayer.ui.theme.SearchBarHeight
import com.seebaldtart.projectmusicplayer.ui.theme.SearchBarRoundedCorner
import com.seebaldtart.projectmusicplayer.utils.debounce

private const val EMPTY_STRING = ""
private val illegalCharacterRegex = Regex("[\r\n\t]+")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BasicSearchBar(
    modifier: Modifier = Modifier,
    focusManager: FocusManager = LocalFocusManager.current,
    query: String = EMPTY_STRING,
    hint: String? = null,
    enabled: Boolean = true,
    delay: Long = 1000L,
    hasSearchButton: Boolean = true,
    hasClearButton: Boolean = true,
    onValueChanged: (String) -> Unit,
    onSearchButtonClick: (() -> Unit)? = null,
    onClearButtonClick: (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        focusedBorderColor = Color.White,
        unfocusedTextColor = Color.White,
        unfocusedBorderColor = Color.White,
        disabledTextColor = Color.LightGray,
        disabledBorderColor = Color.LightGray,
        focusedPlaceholderColor = Color.LightGray,
        unfocusedPlaceholderColor = Color.LightGray,
        disabledPlaceholderColor = Color.LightGray,
        cursorColor = Color.White
    )
) {
    var queryText by remember { mutableStateOf(query) }
    var debounceQueryText by remember { mutableStateOf(query) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scope = rememberCoroutineScope()

    val textFieldFocus = remember { FocusRequester.Default }
    val keyboardController = LocalSoftwareKeyboardController.current

    key(debounceQueryText) {
        debounce(debounceQueryText, delay, scope) {
            onValueChanged(it)
        }
    }

    Box(
        modifier = modifier then Modifier
            .height(SearchBarHeight)
            .border(
                width = DefaultBorderSize,
                color = Color.White,
                shape = RoundedCornerShape(size = SearchBarRoundedCorner)
            )
    ) {
        Row(modifier = Modifier.align(Alignment.Center)) {
            var leadingWidth by remember { mutableStateOf(0.dp) }
            var trailingWidth by remember { mutableStateOf(0.dp) }

            // Leading Icon
            if (hasSearchButton) {
                IconButton(
                    modifier = Modifier.onGloballyPositioned { leadingWidth = it.size.width.dp },
                    onClick = {
                        onSearchButtonClick?.invoke()
                        textFieldFocus.freeFocus()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = EMPTY_STRING,
                        tint = Color.White
                    )
                }
            }

            val paddingStart = if (leadingWidth > 0.dp) 0.dp else 16.dp
            val paddingEnd = if (trailingWidth > 0.dp) 0.dp else 16.dp
            Box (
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = paddingStart, end = paddingEnd)
            ) {
                BasicTextField(
                    value = queryText,
                    onValueChange = { text ->
                        queryText = text
                        debounceQueryText = text
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .focusRequester(textFieldFocus),
                    enabled = true,
                    readOnly = false,
                    textStyle = TextStyle(
                        color = if (isFocused) colors.focusedTextColor else colors.unfocusedTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            textFieldFocus.freeFocus()
                            focusManager.clearFocus(true)
                        }
                    ),
                    singleLine = true,
                    maxLines = 1,
                    minLines = 1,
                    visualTransformation = VisualTransformation.None,
                    onTextLayout = {},
                    interactionSource = interactionSource,
                    cursorBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.cursorColor
                        )
                    )
                )
                if (!hint.isNullOrBlank() && !isFocused && queryText.isBlank()) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = hint,
                        color = colors.unfocusedPlaceholderColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            if (hasClearButton) {
                IconButton(
                    modifier = Modifier.onGloballyPositioned { trailingWidth = it.size.width.dp },
                    onClick = {
                        queryText = EMPTY_STRING
                        onValueChanged.invoke(queryText)
                        onClearButtonClick?.invoke()
                        textFieldFocus.freeFocus()
                        focusManager.clearFocus(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = EMPTY_STRING,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xFF162b4d, showBackground = false)
@Composable
private fun Preview_BasicSearchBar() {
    val interactionSource = remember { MutableInteractionSource() }
    interactionSource.tryEmit(FocusInteraction.Focus())
    BasicSearchBar(
        query = "Test",
        hint = "Hint",
        hasClearButton = false,
        onValueChanged = {}
    )
}
