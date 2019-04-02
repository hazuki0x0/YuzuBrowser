# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/kento/android/sdk/tools/proguard/proguard-android.txt
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

-keep class jp.hazuki.yuzubrowser.legacy.utils.view.behavior.** { *; }

-keepattributes *Annotation*,EnclosingMethod,Signature
-keep @com.fasterxml.jackson.annotation.JsonIgnoreProperties class * { *; }
-keep @com.fasterxml.jackson.annotation.JsonCreator class * { *; }
-keep @com.fasterxml.jackson.annotation.JsonValue class * { *; }
-keep class com.fasterxml.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
    public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }
-keep class android.support.v7.widget.SearchView { *; }
-keep class android.support.v7.widget.** { *; }
-keepattributes SourceFile,LineNumberTable
-keeppackagenames org.jsoup.nodes
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Javascript interface
-keepattributes JavascriptInterface
-keepclasseswithmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Moshi
-keepclassmembers class ** {
  @com.squareup.moshi.FromJson *;
  @com.squareup.moshi.ToJson *;
}
-dontwarn okio.**
# JSR 305 annotations
-dontwarn javax.annotation.**

#Okhttp3/Okio
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform