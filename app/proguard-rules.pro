# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.tranzo.custody.data.remote.dto.** { *; }

# Wallet backend API (Retrofit/Gson models not under dto)
-keep class com.tranzo.custody.data.remote.WalletBackendApi { *; }
-keep class com.tranzo.custody.data.remote.CreateWalletRequest { *; }
-keep class com.tranzo.custody.data.remote.CreateWalletResponse { *; }
-keep class com.tranzo.custody.data.remote.BackendWalletResponse { *; }
-keep class com.tranzo.custody.data.remote.SendUserOpRequest { *; }
-keep class com.tranzo.custody.data.remote.SendUserOpResponse { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Google Play Services & Auth
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

# Google API Client & Drive SDK
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.services.** { *; }
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.**
