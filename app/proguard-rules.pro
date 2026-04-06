# Add project specific ProGuard rules here.
-keep class com.bibin.opnsense.data.remote.dto.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Suppress missing error-prone annotation warnings from Tink (used by EncryptedSharedPreferences)
-dontwarn com.google.errorprone.annotations.**
