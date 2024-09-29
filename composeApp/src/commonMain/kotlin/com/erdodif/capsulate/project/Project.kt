package com.erdodif.capsulate.project

import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.core.PlatformDirectory

class Project(val directory: PlatformDirectory) {
    init {
        Napier.d { directory.toString() }
    }

    fun listFiles(): List<String> = directory.toKmpFile().list()?.toList() ?: emptyList()
}
