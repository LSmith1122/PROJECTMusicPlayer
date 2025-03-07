package com.seebaldtart.projectmusicplayer.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T: Any> debounce(
    value: T,
    delayMillis: Long = 300L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit
): T {
    val state by rememberUpdatedState(value)

    DisposableEffect(state){
        val job = coroutineScope.launch {
            delay(delayMillis)
            onChange(state)
        }
        onDispose {
            job.cancel()
        }
    }
    return state
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun <T: Any> Modifier.pointerSlidingInput(
    listState: LazyListState,
    items: List<T>,
    onHighlightedIndexUpdated: (Int?) -> Unit,
    onIndexSelected: (T) -> Unit
): Modifier {
    var highlightedIndex: Int? = null
    // Helper function to map a pointer position to an item index.
    fun getIndexAtPosition(position: Offset): Int? {
        listState.layoutInfo.visibleItemsInfo.forEach { itemInfo ->
            // itemInfo.offset is the top position of the item relative to the LazyColumn.
            if (position.y in itemInfo.offset.toFloat() .. (itemInfo.offset + itemInfo.size).toFloat()) {
                return itemInfo.index
            }
        }
        return null
    }
    return this.pointerInput(Unit) {
        // Run an infinite pointer event loop.
        awaitPointerEventScope {
            while (true) {
                // Wait for the next pointer event.
                val event = awaitPointerEvent()
                // For simplicity, handle the first pointer only.
                val pointerPosition = event.changes.first().position

                when (event.type) {
                    PointerEventType.Enter,
                    PointerEventType.Press,
                    PointerEventType.Move -> {
                        val indexAtPosition = getIndexAtPosition(pointerPosition)
                        if (indexAtPosition != highlightedIndex) {
                            highlightedIndex = indexAtPosition
                            onHighlightedIndexUpdated(highlightedIndex)
                        }
                    }

                    PointerEventType.Release -> {
                        // On pointer up, if an item is highlighted, select it.
                        highlightedIndex?.let { index ->
                            onIndexSelected(items[index])
                        }
                        // Clear the highlight whether selected or not.
                        highlightedIndex = null
                        onHighlightedIndexUpdated(null)
                    }

                    else -> {
                        // For any other event (like cancel), reset.
                        highlightedIndex = null
                    }
                }
            }
        }
    }
}