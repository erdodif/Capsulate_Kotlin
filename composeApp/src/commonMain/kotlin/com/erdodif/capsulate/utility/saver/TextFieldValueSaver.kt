package com.erdodif.capsulate.utility.saver

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object TextFieldValueSaver : Saver<TextFieldValue, Triple<String, Int, Int>> {
    override fun restore(value: Triple<String, Int, Int>): TextFieldValue? =
        TextFieldValue(value.first, TextRange(value.second, value.third))

    override fun SaverScope.save(value: TextFieldValue): Triple<String, Int, Int>? =
        Triple(value.text, value.selection.start, value.selection.end)
}
