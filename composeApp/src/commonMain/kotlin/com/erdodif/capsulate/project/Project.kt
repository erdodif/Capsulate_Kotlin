package com.erdodif.capsulate.project

import co.touchlab.kermit.Logger
import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.valueOrNull
import com.erdodif.capsulate.supportedExtensions
import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.vinceglb.filekit.core.PlatformDirectory
import kotlin.arrayOf
import kotlin.collections.MutableList
import kotlin.uuid.ExperimentalUuidApi

@KParcelize
@OptIn(ExperimentalUuidApi::class)
class Project(
    @KIgnoredOnParcel val directory: PlatformDirectory? = null,
    val openFiles: MutableList<OpenFile> = mutableListOf(OpenFile())
) : KParcelable {
    constructor(openFile: OpenFile) : this(null, mutableListOf(openFile))

    init {
        Logger.d { directory.toString() }
    }

    fun openEmptyFile(): OpenFile = OpenFile().also { openFiles.add(it) }

    fun listFiles(): List<IPlatformFile> =
        (directory?.toKmpFile()?.listFiles() ?: arrayOf()).filter {
            it.isFile() && it !in openFiles.mapNotNull { it.file.valueOrNull } && (
                    supportedExtensions == null || it.getName().split(".")
                        .lastOrNull() in supportedExtensions!!)
        } + openFiles.mapNotNull { it.file.valueOrNull }

    fun getFile(name: String): OpenFile {
        val openedFile: OpenFile? = openFiles.find { it.file.valueOrNull?.getName() == name }
        if (openedFile != null) {
            return openedFile
        }
        val platformFile = directory?.toKmpFile()?.listFiles()?.first { it.getName() == name }
        if (platformFile == null) {
            throw Exception()
        }
        val opened = OpenFile(platformFile)
        openFiles.add(opened)
        return opened
    }

    fun namelessCount(index: Int): Int = openFiles.dropLast(openFiles.size - index)
        .fold(0) { i, file -> if (file.hasFile) i else i + 1 }
}
