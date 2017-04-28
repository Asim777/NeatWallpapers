# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Asim\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

#-keep class android.support.v7.widget.SearchView { *; }
#-keepnames class us.asimgasimzade.android.neatwallpapers.FavoritesFragment { *; }
#-keep class us.asimgasimzade.android.neatwallpapers.FavoritesFragment { *; }
#-keepnames class us.asimgasimzade.android.neatwallpapers.SingleImageFragment { *; }
#-keep class us.asimgasimzade.android.neatwallpapers.SingleImageFragment { *; }
#-keepnames class us.asimgasimzade.android.neatwallpapers.LoginActivity { *; }
#-keep class us.asimgasimzade.android.neatwallpapers.LoginActivity { *; }
#-keep class com.google.android.gms.** { *; }
#-keepnames class com.google.android.gms.** { *; }
#-dontwarn com.google.android.gms.**
#-keep class com.google.firebase.auth.** { *; }
#-keepnames class com.google.firebase.auth.** { *; }
#-dontwarn com.google.firebase.auth.**
#-keep class com.firebase.** { *; }
#-keep class org.apache.** { *; }
#-keep class us.asimgasimzade.android.neatwallpapers.data.** { *; }
#-keepnames class us.asimgasimzade.android.neatwallpapers.data.** { *; }
#-keepnames class com.fasterxml.jackson.** { *; }
#-keepnames class javax.servlet.** { *; }
#-keepnames class org.ietf.jgss.** { *; }
#-dontwarn org.w3c.dom.**
#-dontwarn org.apache.**
#-dontwarn org.joda.time.**
#-dontwarn org.shaded.apache.**
#-dontwarn org.ietf.jgss.**

-keep class android.support.v7.widget.SearchView { *; }
-keepnames class us.asimgasimzade.android.neatwallpapers.FavoritesFragment { *; }
-keep class us.asimgasimzade.android.neatwallpapers.FavoritesFragment { *; }
-keepnames class us.asimgasimzade.android.neatwallpapers.SingleImageFragment { *; }
-keep class us.asimgasimzade.android.neatwallpapers.SingleImageFragment { *; }
-keep class us.asimgasimzade.android.neatwallpapers.data.** { *; }
-keepnames class us.asimgasimzade.android.neatwallpapers.data.** { *; }