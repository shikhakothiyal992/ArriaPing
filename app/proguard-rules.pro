# Add project specific ProGuard rules here.

-assumenosideeffects class android.util.Log {
   public static *** d(...);
   public static *** v(...);
   public static *** e(...);
}
-dontpreverify
    -repackageclasses ''
    -allowaccessmodification
    -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep class org.apache.http.** { *; }
-keep class org.apache.commons.codec.** { *; }
-keep class org.apache.commons.logging.** { *; }
-keep class android.net.compatibility.** { *; }
-keep class android.net.http.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.webkit.**

 -dontwarn com.google.android.gms.*
 -dontwarn org.mockito.**
 -dontwarn sun.reflect.**
 -dontwarn android.test.**
 -ignorewarnings


#Retrofit
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn retrofit2.Platform$Java8
-dontnote retrofit2.Platform

#Gson specific classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.stream.** { *; }
-keep class sun.misc.Unsafe { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-dontwarn sun.misc.**

#OKHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontnote okhttp3.**
-dontwarn com.squareup.okhttp.**

#Firebase
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

# If you are using custom exceptions, add this line so that custom exception types are skipped during obfuscation:
-keep public class * extends java.lang.Exception
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
# in order to provide the most meaningful crash reports, add the following line:
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation @interface com.google.gson.annotations.SerializedName

#Data Model classes
-keep class com.arria.ping.model.forgotpassword.** { *; }
-keepclassmembers class com.arria.ping.model.forgotpassword.** { *; }
-keep class com.arria.ping.model.successLogin.** { *; }
-keepclassmembers class com.arria.ping.model.successLogin.** { *; }
-keep class com.arria.ping.model.** { *; }
-keepclassmembers class com.arria.ping.model.** { *; }

#AWS
-keep class org.apache.commons.logging.**               { *; }
-keep class com.amazonaws.org.apache.commons.logging.** { *; }
-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }
-keep class com.amazonaws.internal.**
-keep class org.codehaus.**                             { *; }
-keep class org.joda.time.tz.Provider                   { *; }
-keep class org.joda.time.tz.NameProvider               { *; }
-keepattributes Signature,*Annotation*,EnclosingMethod
-keepnames class com.fasterxml.jackson.** { *; }
-keepattributes Signature,*Annotation*
-keepnames class com.amazonaws.** { *; }
-keep class com.amazonaws.services.cognitoidentityprovider.** { *; }
-keep class * extends com.amazonaws.AmazonClientException { *; }

-dontwarn com.fasterxml.jackson.databind.**
-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn org.ietf.jgss.**
-dontwarn org.joda.convert.**
-dontwarn com.amazonaws.org.joda.convert.**
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.**
-dontwarn com.amazonaws.mobile.auth.**

#KotPref
-keep class * extends com.chibatching.kotpref.KotprefModel

#Room DB
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

 -keepclassmembers enum * {
     public static **[] values();
     public static ** valueOf(java.lang.String);
 }
 -keepclassmembers enum * { *; }
-keep public class com.arria.ping.ui.kpi.**{public protected *;}
 -keepclassmembers public class com.arria.ping.ui.kpi.** {<fields>; }