package com.erdodif.capsulate.project

import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.core.PlatformDirectory
import kotlin.arrayOf

val extensions = arrayOf("txt", "struk")

class Project(val directory: PlatformDirectory? = null) {
    val openFiles: MutableList<OpenFile> = mutableListOf()

    init {
        Napier.d { directory.toString() }
    }

    fun listFiles(): List<IPlatformFile> = (directory?.toKmpFile()?.listFiles() ?: arrayOf()).filter {
        it.isFile() && it !in openFiles.map { it.file } && it.getName().split(".")
            .lastOrNull() in extensions
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
