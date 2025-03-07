package com.erdodif.capsulate.utility.saver

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import com.erdodif.capsulate.lang.util.Nat.Zero.value
import com.erdodif.capsulate.lang.util.fold
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.valueOrNull
import com.erdodif.capsulate.project.OpenFile
import dev.zwander.kotlin.file.PlatformFile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object OpenFileSaver : Saver<OpenFile, String> {
    override fun restore(value: String): OpenFile? = if (value.isBlank()) null else
        try {
            val id = Uuid.fromByteArray(value.encodeToByteArray())
            OpenFile(id)
        } catch (_: Exception) {
            OpenFile(PlatformFile(value))
        }

    override fun SaverScope.save(value: OpenFile): String? =
        value.file[{ it.getName() }, { it.toString() }]
}
