# ========================================
# LocalPDF - ProGuard / R8 Rules
# ========================================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========================================
# PDFBox Android
# ========================================
-keep class com.tom_roush.pdfbox.** { *; }
-keep class org.apache.fontbox.** { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.apache.fontbox.**
-dontwarn org.bouncycastle.**

# ========================================
# Apache POI (Word ↔ PDF conversion)
# ========================================
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class schemaorg_apache_xmlbeans.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.**
-dontwarn javax.xml.**
-dontwarn org.w3c.**
-dontwarn java.awt.**
-dontwarn org.osgi.**
-dontwarn org.apache.logging.**
-dontwarn com.graphbuilder.**

# ========================================
# Room Database
# ========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ========================================
# Hilt / Dagger
# ========================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-dontwarn dagger.hilt.**

# ========================================
# Kotlin Serialization
# ========================================
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.omerakpul.localpdf.**$$serializer { *; }
-keepclassmembers class com.omerakpul.localpdf.** {
    *** Companion;
}
-keepclasseswithmembers class com.omerakpul.localpdf.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn kotlinx.serialization.**

# ========================================
# Compose Navigation (Serializable routes)
# ========================================
-keep class com.omerakpul.localpdf.presentation.navigation.** { *; }

# ========================================
# Coil (Image Loading)
# ========================================
-dontwarn coil.**

# ========================================
# General Android / Kotlin
# ========================================
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-keep class kotlin.Metadata { *; }

# Kotlin unsigned types (prevents ART profile_saver crash)
-keep class kotlin.ranges.ULongProgression { *; }
-keep class kotlin.ranges.UIntProgression { *; }
-keep class kotlin.ranges.ULongRange { *; }
-keep class kotlin.ranges.UIntRange { *; }
-keep class kotlin.ULong { *; }
-keep class kotlin.UInt { *; }
-keep class kotlin.UByte { *; }
-keep class kotlin.UShort { *; }

# Kotlin IO types (prevents ART profile_saver crash on ByteStreamsKt)
-keep class kotlin.io.** { *; }