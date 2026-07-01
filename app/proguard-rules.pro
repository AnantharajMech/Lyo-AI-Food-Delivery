# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve the line number information for premium Google Play Store Console crash analytics stack traces.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,Deprecated,AnnotationDefault

# Hide the original source file name in obfuscated stack traces.
-renamesourcefileattribute SourceFile

# Ensure our local entities and database models are not optimized or stripped away
-keep class com.example.data.database.** { *; }

# Keep Android Room-specific elements
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Keep Jetpack Compose stable classes and annotations
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.Stable *;
}
