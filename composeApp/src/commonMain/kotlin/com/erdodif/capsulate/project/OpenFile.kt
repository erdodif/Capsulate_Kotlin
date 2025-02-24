package com.erdodif.capsulate.project

import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.readString
import kotlinx.serialization.Serializable

@KParcelize
@Serializable
data class OpenFile(@KIgnoredOnParcel var file: IPlatformFile? = null) : KParcelable {
    var content: String? = null

    suspend fun save(): Boolean = withContext(Dispatchers.IO) {
        if (file == null) {
            file = FileKit.saveFile(
                (content ?: "").encodeToByteArray(),
                "program",
                "struk"
            )?.toKmpFile()
            if (file == null) {
                return@withContext false
            }
        }
        val buffer = file?.openOutputStream(false)
        if (buffer == null) {
            Napier.e { "Could not save file ${file?.getName()}" }
            false
        } else {
            buffer.write((content ?: "").encodeToByteArray())
            buffer.close()
            true
        }
    }

    suspend fun load(): String? = withContext(Dispatchers.IO) {
        file?.openInputStream()?.readString()
    }
}