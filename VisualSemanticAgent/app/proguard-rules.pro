# ProGuard rules for Visual Semantic Agent

# Preserve the Compose runtime classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Preserve Jetpack Compose generated code
-keep @androidx.compose.runtime.Composable class * { *; }
-keep class androidx.compose.runtime.Applier { *; }

# Preserve OkHttp
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Preserve Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** { *; }
-keepclasseswithmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve CameraX
-keep class androidx.camera.** { *; }
-keepclassmembers class androidx.camera.** { *; }

# Preserve Lifecycle
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# Preserve Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# Preserve Timber
-keep class timber.log.Timber { *; }
-keepclassmembers class timber.log.Timber { *; }

# Preserve Lottie
-keep class com.airbnb.lottie.** { *; }
-keepclassmembers class com.airbnb.lottie.** { *; }

# Preserve our project classes
-keep class com.vsa.visualsemanticagent.** { *; }
-keepclassmembers class com.vsa.visualsemanticagent.** { *; }

# Preserve data classes
-keepclassmembers class com.vsa.visualsemanticagent.*.* {
    *** get*();
    void set*(***);
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep line numbers for crash reporting
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
