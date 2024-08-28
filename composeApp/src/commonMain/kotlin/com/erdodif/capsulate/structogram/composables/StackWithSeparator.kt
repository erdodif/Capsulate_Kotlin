package com.erdodif.capsulate.structogram.composables

import androidx.compose.runtime.Composable

@Composable
fun <T>StackWithSeparator(list: Array<T>, scope: @Composable (T) -> Unit, separator: @Composable () -> Unit){
    if (list.isEmpty()) {
        commandPlaceHolder()
        return
    }
    for (i in 0..<list.size - 1) {
        scope(list[i])
        separator()
    }
    scope(list[list.size - 1])
}
