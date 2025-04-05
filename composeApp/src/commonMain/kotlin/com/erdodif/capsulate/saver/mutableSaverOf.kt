package com.erdodif.capsulate.saver

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope

fun <T,R: Any>mutableSaverOf(saver: Saver<T,R>): Saver<MutableState<T>,R> = object : Saver<MutableState<T>,R>{
    override fun restore(value: R): MutableState<T>? = saver.restore(value)?.let(::mutableStateOf)

    override fun SaverScope.save(value: MutableState<T>): R? = with(saver){
        save(value.value)
    }
}
