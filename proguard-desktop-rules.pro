# Don't fail if possible
-ignorewarnings

# FilePickers and dbus
-keep class org.freedesktop.dbus.** { *; }
-keep class * implements org.freedesktop.dbus.** { *; }

# coroutines
-keep class * implements kotlinx.coroutines.internal.MainDispatcherFactory
# Needed by filekit
-keep class com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.** { public *; }
-keep class * implements com.sun.jna.** { *; }
-dontnote com.sun.**
# SLF4J
-keep class org.slf4j.** { *; }

-keepclasseswithmembers public class com.erdodif.capsulate.MainKt {
    public static void main(java.lang.String[]);
}
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

# Compose
-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
    void sourceInformation(androidx.compose.runtime.Composer,java.lang.String);
    void sourceInformationMarkerStart(androidx.compose.runtime.Composer,int,java.lang.String);
    void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}

# SnapshotStateProblemts
-keep class androidx.compose.runtime.State { *; }
-keep class androidx.compose.runtime.DerivedSnapshotState { *; }
-keep class androidx.compose.runtime.SnapshotStateKt { *; }
-keep class androidx.compose.runtime.SnapshotStateKt__DerivedStateKt { *; }

-keep class * implements androidx.compose.runtime.State { *; }

-keepclassmembers class androidx.compose.runtime.** {
    ** derivedStateOf(...);
}

# TextRanges
-keep class * implements androidx.compose.foundation.text.TextRangeScopeMeasurePolicy { *; }
-keep class androidx.compose.foundation.text.TextLinkScope$$Lambda$* { *; }
-keep interface androidx.compose.foundation.text.TextRangeScopeMeasurePolicy { *; }
-keep class androidx.compose.foundation.text.TextRangeScopeMeasurePolicy$DefaultImpls { *; }

# kotlinx.serialization
# https://github.com/Kotlin/kotlinx.serialization/blob/master/rules/common.pro

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Don't print notes about potential mistakes or omissions in the configuration for kotlinx-serialization classes
# See also https://github.com/Kotlin/kotlinx.serialization/issues/1900
-dontnote kotlinx.serialization.**

# Serialization core uses `java.lang.ClassValue` for caching inside these specified classes.
# If there is no `java.lang.ClassValue` (for example, in Android), then R8/ProGuard will print a warning.
# However, since in this case they will not be used, we can disable these warnings
-dontwarn kotlinx.serialization.internal.ClassValueReferences

# disable optimisation for descriptor field because in some versions of ProGuard, optimization generates incorrect bytecode that causes a verification error
# see https://github.com/Kotlin/kotlinx.serialization/issues/2719
-keepclassmembers public class **$$serializer {
    private ** descriptor;
}

-keep,includedescriptorclasses class com.hypergonial.chat.**$$serializer { *; }
-keepclassmembers class com.hypergonial.chat.** {
    *** Companion;
}
-keepclasseswithmembers class com.hypergonial.chat.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
