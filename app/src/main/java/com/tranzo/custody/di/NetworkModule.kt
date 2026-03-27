package com.tranzo.custody.di

import com.tranzo.custody.BuildConfig
import com.tranzo.custody.data.remote.TranzoApi
import com.tranzo.custody.data.remote.WalletBackendApi
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
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.tranzo.money/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTranzoApi(retrofit: Retrofit): TranzoApi {
        return retrofit.create(TranzoApi::class.java)
    }

    @Provides
    @Singleton
    @WalletBackendRetrofit
    fun provideWalletBackendRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val base = BuildConfig.WALLET_BACKEND_URL.trimEnd('/') + "/"
        return Retrofit.Builder()
            .baseUrl(base)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWalletBackendApi(@WalletBackendRetrofit retrofit: Retrofit): WalletBackendApi {
        return retrofit.create(WalletBackendApi::class.java)
    }
}
