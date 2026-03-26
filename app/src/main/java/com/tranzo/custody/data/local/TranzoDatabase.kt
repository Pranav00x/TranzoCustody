package com.tranzo.custody.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tranzo.custody.data.local.dao.TransactionDao
import com.tranzo.custody.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TranzoDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
