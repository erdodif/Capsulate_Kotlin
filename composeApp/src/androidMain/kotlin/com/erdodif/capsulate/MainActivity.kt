package com.erdodif.capsulate

import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.core.view.WindowCompat
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.utility.preview.ParserTester
import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.PlatformFile
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MainActivity : ComponentActivity() {
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        FileKit.init(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Napier.base(DebugAntilog())
        hideSystemUI()
        scope.launch {
            applicationExitJob.join()
            exitProcess(0)
        }
        setContent {
            App()
        }
    }

    fun hideSystemUI() {
        actionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATED")
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


@Preview(wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE)
@PreviewScreenSizes
@Composable
fun AppAndroidPreview() {
    App()
}

@Preview
@Composable
private fun Tester() {
    ParserTester()
}

/**
 * Needed for Parcelling [OpenFile]
 */
@Suppress("UNUSED")
@OptIn(ExperimentalUuidApi::class)
object OpenFileParceler : Parceler<OpenFile> {
    override fun OpenFile.write(
        parcel: Parcel,
        flags: Int
    ) = file[{
        parcel.writeString(it.getName())
        parcel.writeString(content)
    }, {
        parcel.writeString(Json.encodeToString(it.toByteArray()))
        parcel.writeString(content)
    }]


    override fun create(parcel: Parcel): OpenFile {
        val content = parcel.readString()
        return try {
            val id = Json.decodeFromString<ByteArray>(content ?: "")
            OpenFile(Uuid.fromByteArray(id))
        } catch (_: Exception) {
            OpenFile(PlatformFile(content ?: ""))
        }
    }

}
