package com.tranzo.custody.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.tranzo.custody.web3.AmoyRpcConfig
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Web3Module {

    @Provides
    @Singleton
    fun provideWeb3j(): Web3j {
        return Web3j.build(
            HttpService(AmoyRpcConfig.primaryUrl(), AmoyRpcConfig.httpClient())
        )
    }
}
