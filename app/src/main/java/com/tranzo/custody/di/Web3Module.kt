package com.tranzo.custody.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Web3Module {

    @Provides
    @Singleton
    fun provideWeb3j(): Web3j {
        // Default to Polygon Amoy for testing
        return Web3j.build(HttpService("https://rpc.ankr.com/polygon_amoy"))
    }
}
