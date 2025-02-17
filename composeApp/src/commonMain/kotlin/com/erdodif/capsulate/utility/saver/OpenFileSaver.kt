package com.erdodif.capsulate.utility.saver

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import com.erdodif.capsulate.project.OpenFile
import dev.zwander.kotlin.file.PlatformFile

object OpenFileSaver: Saver<OpenFile, String> {
    override fun restore(value: String): OpenFile? = OpenFile(PlatformFile(value))

    override fun SaverScope.save(value: OpenFile): String? = value.file?.getPath()
}
