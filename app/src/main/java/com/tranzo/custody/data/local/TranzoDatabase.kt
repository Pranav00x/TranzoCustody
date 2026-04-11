package com.tranzo.custody.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tranzo.custody.data.local.dao.TransactionDao
import com.tranzo.custody.data.local.dao.UserDao
import com.tranzo.custody.data.local.entity.TransactionEntity
import com.tranzo.custody.data.local.entity.TranzoUserEntity

@Database(entities = [TransactionEntity::class, TranzoUserEntity::class], version = 2, exportSchema = false)
abstract class TranzoDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
}
