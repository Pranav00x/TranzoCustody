package com.tranzo.custody.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tranzo.custody.BuildConfig
import com.tranzo.custody.data.remote.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideTokenRefresher(impl: TokenRefresherImpl): TokenRefresher = impl

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ── Public API (api.tranzo.money) ──

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.tranzo.money/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideTranzoApi(retrofit: Retrofit): TranzoApi {
        return retrofit.create(TranzoApi::class.java)
    }

    // ── Wallet Backend (self-hosted) ──

    @Provides
    @Singleton
    @WalletBackendRetrofit
    fun provideWalletBackendRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        val base = BuildConfig.WALLET_BACKEND_URL.trimEnd('/') + "/"
        return Retrofit.Builder()
            .baseUrl(base)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideWalletBackendApi(@WalletBackendRetrofit retrofit: Retrofit): WalletBackendApi {
        return retrofit.create(WalletBackendApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApi(@WalletBackendRetrofit retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCardApi(@WalletBackendRetrofit retrofit: Retrofit): CardApi {
        return retrofit.create(CardApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStreamApi(@WalletBackendRetrofit retrofit: Retrofit): StreamApi {
        return retrofit.create(StreamApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSwapApi(@WalletBackendRetrofit retrofit: Retrofit): SwapApi {
        return retrofit.create(SwapApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBridgeApi(@WalletBackendRetrofit retrofit: Retrofit): BridgeApi {
        return retrofit.create(BridgeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBuyApi(@WalletBackendRetrofit retrofit: Retrofit): BuyApi {
        return retrofit.create(BuyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideKycApi(@WalletBackendRetrofit retrofit: Retrofit): KycApi {
        return retrofit.create(KycApi::class.java)
    }
}
