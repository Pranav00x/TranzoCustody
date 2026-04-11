package com.tranzo.custody.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tranzo.custody.data.local.dao.TransactionDao
import com.tranzo.custody.data.local.dao.UserDao
import com.tranzo.custody.data.local.dao.AssetDao
import com.tranzo.custody.data.local.entity.TransactionEntity
import com.tranzo.custody.data.local.entity.TranzoUserEntity
import com.tranzo.custody.data.local.entity.AssetEntity

@Database(
    entities = [TransactionEntity::class, TranzoUserEntity::class, AssetEntity::class], 
    version = 3, 
    exportSchema = false
)
@TypeConverters(TranzoTypeConverters::class)
abstract class TranzoDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun assetDao(): AssetDao
}
