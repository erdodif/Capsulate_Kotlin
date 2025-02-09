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
import kotlin.system.exitProcess

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

object xd : Parceler<IPlatformFile>{
    override fun IPlatformFile.write(
        parcel: Parcel,
        flags: Int
    ) {
        parcel.writeString(this.getPath())
    }

    override fun create(parcel: Parcel): IPlatformFile {
        return PlatformFile(parcel.readString()!!)
    }

}


@Parcelize
class XD(@TypeParceler<IPlatformFile, xd> val d:  IPlatformFile) : Parcelable {

}
