-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class com.wipeproof.app.core.model.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** implements kotlinx.serialization.KSerializer { *; }
