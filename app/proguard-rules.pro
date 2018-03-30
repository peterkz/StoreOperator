# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/bruce/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep Retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-keep class com.wetoop.kangaroowallet.api.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.widget.SearchView { *; }
-keepattributes *Annotation*
