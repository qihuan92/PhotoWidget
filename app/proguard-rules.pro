# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.qihuan.photowidget.bean** { *; }
-keep class com.qihuan.photowidget.common** { *; }

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

-keep class com.bumptech.glide.load.resource.gif** { *; }
-keep interface com.bumptech.glide.load.resource.gif** { *; }
-keep class com.bumptech.glide.gifdecoder** { *; }

# DeviceInfoActivity list Device properties dynamically like a bean
-keepclasseswithmembers class com.microsoft.appcenter.ingestion.models.Device {
   public ** get*();
}
-keepclasseswithmembers class com.microsoft.appcenter.analytics.EventProperties {
   ** getProperties();
}
-keepclasseswithmembers class com.microsoft.appcenter.analytics.PropertyConfigurator {
   private ** get*();
   private ** mEventProperties;
}
-keepclasseswithmembers class * extends com.microsoft.appcenter.ingestion.models.properties.TypedProperty {
   ** getValue();
}

# For some reason the previous rule doesn't work with primitive getValue return type
-keepclasseswithmembers class com.microsoft.appcenter.ingestion.models.properties.BooleanTypedProperty {
   public boolean getValue();
}
-keepclasseswithmembers class com.microsoft.appcenter.ingestion.models.properties.LongTypedProperty {
   public long getValue();
}
-keepclasseswithmembers class com.microsoft.appcenter.ingestion.models.properties.DoubleTypedProperty {
   public double getValue();
}