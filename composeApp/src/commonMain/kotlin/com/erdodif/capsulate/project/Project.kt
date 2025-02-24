package com.erdodif.capsulate.project

import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.supportedExtensions
import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.core.PlatformDirectory
import kotlin.arrayOf
import kotlin.collections.MutableList

@KParcelize
class Project(
    @KIgnoredOnParcel val directory: PlatformDirectory? = null,
    val openFiles: MutableList<OpenFile> = mutableListOf()
) : KParcelable {
    constructor(openFile: OpenFile) : this(null, mutableListOf(openFile))

    init {
        Napier.d { directory.toString() }
    }

    fun listFiles(): List<IPlatformFile> =
        (directory?.toKmpFile()?.listFiles() ?: arrayOf()).filter {
            it.isFile() && it !in openFiles.map { it.file } && (
                    supportedExtensions == null || it.getName().split(".")
                        .lastOrNull() in supportedExtensions!!)
        }.toList() + openFiles.map { it.file }.filterNotNull()

    fun getFile(name: String): OpenFile {
        val openedFile: OpenFile? = openFiles.find { it.file?.getName() == name }
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
}
