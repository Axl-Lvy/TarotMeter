# --- Ignore IntelliJ & JetBrains annotations ---
-dontwarn org.jetbrains.annotations.**
-dontwarn org.intellij.lang.annotations.**

# --- Keep kotlinx.serialization classes ---
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses

# --- Keep coroutine debug info ---
-keepclassmembers class kotlinx.coroutines.** {
    *;
}
