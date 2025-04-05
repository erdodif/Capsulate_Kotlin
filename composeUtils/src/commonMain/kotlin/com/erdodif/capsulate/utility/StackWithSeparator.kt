package com.erdodif.capsulate.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.erdodif.capsulate.utility.theme.commandPlaceHolder

@Composable
fun <T> IndexedStackWithSeparator(
    list: Array<T>,
    scope: @Composable (T, Int) -> Unit,
    placeHolder: @Composable () -> Unit = { commandPlaceHolder() },
    separator: @Composable () -> Unit
) {
    if (list.isEmpty()) {
        placeHolder()
        return
    }
    key(list) { // Report google that an empty list can't be in key
        for (i in 0..<list.size - 1) {
            scope(list[i], i)
            separator()
        }
        scope(list[list.size - 1], 0)
    }
}

@Composable
fun <T> StackWithSeparator(
    list: Array<T>,
    scope: @Composable (T) -> Unit,
    placeHolder: @Composable () -> Unit = { commandPlaceHolder() },
    separator: @Composable () -> Unit
) = StackWithSeparator(list.toList(), scope, placeHolder, separator)

@Composable
fun <T> StackWithSeparator(
    list: List<T>,
    scope: @Composable (T) -> Unit,
    placeHolder: @Composable () -> Unit = { commandPlaceHolder() },
    separator: @Composable () -> Unit
) {
    if (list.isEmpty()) {
        placeHolder()
        return
    }
    key(list) {
        for (i in 0..<list.size - 1) {
            scope(list[i])
            separator()
        }
        scope(list[list.size - 1])
    }
}
