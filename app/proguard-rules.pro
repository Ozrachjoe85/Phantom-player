# Add project specific ProGuard rules here.
-keep class androidx.media3.** { *; }
-keep class com.phantom.player.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
