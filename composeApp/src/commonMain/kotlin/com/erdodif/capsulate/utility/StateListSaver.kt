package com.erdodif.capsulate.utility

import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

fun <T> stateListSaver() =
    listSaver<SnapshotStateList<T>, T>({ it.toList() }, List<T>::toMutableStateList)