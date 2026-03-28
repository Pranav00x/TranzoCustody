package com.tranzo.custody.di

import android.content.Context
import androidx.room.Room
import com.tranzo.custody.data.local.TranzoDatabase
import com.tranzo.custody.data.local.ThemePreferencesManager
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TranzoDatabase {
        return Room.databaseBuilder(
            context,
            TranzoDatabase::class.java,
            "tranzo_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTransactionDao(database: TranzoDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideUserSessionManager(@ApplicationContext context: Context): UserSessionManager {
        return UserSessionManager(context)
    }

    @Provides
    @Singleton
    fun provideThemePreferencesManager(@ApplicationContext context: Context): ThemePreferencesManager {
        return ThemePreferencesManager(context)
    }
}
