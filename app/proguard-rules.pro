# Add project specific ProGuard rules here.
-keep class com.bibin.opnsense.data.remote.dto.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
