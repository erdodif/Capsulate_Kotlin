package androidx.compose.desktop.ui.tooling.preview

import androidx.annotation.FloatRange
import androidx.annotation.IntRange

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
expect annotation class Preview(
    val name: String = "",
    val group: String = "",
    @IntRange(from = 1)  val apiLevel: Int = -1,
    val widthDp: Int = -1,
    val heightDp: Int = -1,
    val locale: String = "",
    @FloatRange(from = 0.01) val fontScale: Float = 1f,
    val showSystemUi: Boolean = false,
    val showBackground: Boolean = false,
    val backgroundColor: Long = 0,
)