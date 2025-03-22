@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.project

import co.touchlab.kermit.Logger
import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.valueOrNull
import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.readString
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
@Serializable
data class OpenFile(
    @KIgnoredOnParcel var file: Either<IPlatformFile, Uuid>,
    var content: String? = null
) :
    KParcelable {
    constructor() : this(Right(Uuid.random()))
    constructor(file: IPlatformFile) : this(Left(file))
    constructor(id: Uuid) : this(Right(id))

    val hasFile: Boolean
        get() = file is Left

    suspend fun save(): Boolean = withContext(Dispatchers.IO) {
        if (file is Right) {
            val tmp = FileKit.saveFile(
                (content ?: "").encodeToByteArray(),
                "program",
                "struk"
            )?.toKmpFile()
            if (tmp == null) {
                return@withContext false
            } else {
                file = Left(tmp)
            }
        }
        val buffer = file.valueOrNull?.openOutputStream(false)
        if (buffer == null) {
            Logger.e { "Could not save file ${file[{ it.getName() }, { "TMP:$it" }]}" }
            false
        } else {
            buffer.write((content ?: "").encodeToByteArray())
            buffer.close()
            true
        }
    }

    suspend fun load(): String? = withContext(Dispatchers.IO) {
        content = file.valueOrNull?.openInputStream()?.readString()
        content
    }
}