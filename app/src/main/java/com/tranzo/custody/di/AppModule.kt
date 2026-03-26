package com.tranzo.custody.di

import com.tranzo.custody.data.repository.CardRepositoryImpl
import com.tranzo.custody.data.repository.TransactionRepositoryImpl
import com.tranzo.custody.data.repository.WalletRepositoryImpl
import com.tranzo.custody.domain.repository.CardRepository
import com.tranzo.custody.domain.repository.TransactionRepository
import com.tranzo.custody.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
}
