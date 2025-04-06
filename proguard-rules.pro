#-dontobfuscate # In case something breaks
-keepnames class * extends androidx.startup.Initializer
-keep class * extends androidx.startup.Initializer {
    <init>();
}
-dontwarn org.slf4j.impl.StaticLoggerBinder