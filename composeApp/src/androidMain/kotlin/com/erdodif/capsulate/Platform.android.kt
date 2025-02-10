@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.erdodif.capsulate

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.PlatformFile
import io.github.vinceglb.filekit.core.PlatformDirectory
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.TypeParceler

@Composable
actual fun resolveColors(): ColorScheme {
    val context = LocalContext.current
    return when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        else -> MaterialTheme.colorScheme
    }
}

actual typealias KParcelable = android.os.Parcelable
actual typealias KIgnoredOnParcel = kotlinx.parcelize.IgnoredOnParcel
actual typealias KParceler<T> = Parceler<T>
actual typealias KTypeParceler<T, R> = TypeParceler<T, R>

@Composable
actual fun locateSetting() {
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_SHOW_APP_INFO)
    context.startActivity(intent)
}

actual val onMobile: Boolean = true

actual class FileParceler : KParceler<IPlatformFile?> {
    override fun IPlatformFile?.write(
        parcel: Parcel,
        flags: Int
    ) {
        if (this != null) {
            parcel.writeString(this.getPath())
        }
    }

    override fun create(parcel: Parcel): IPlatformFile? {
        val path: String? = parcel.readString()
        if (path != null) {
            return PlatformFile(path)
        }
        return null
    }
}

class DirectoryParceler : KParceler<PlatformDirectory?> {
    override fun PlatformDirectory?.write(
        parcel: Parcel,
        flags: Int
    ) {
        if (this != null) {
            parcel.writeString(this.uri.toString())
        }
    }

    override fun create(parcel: Parcel): PlatformDirectory? {
        val path = parcel.readString()
        if (path == null) {
            return null
        }
        return PlatformDirectory(Uri.parse(path))
    }

}
