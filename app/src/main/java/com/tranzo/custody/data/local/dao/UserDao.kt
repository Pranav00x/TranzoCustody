package com.tranzo.custody.data.local.dao

import androidx.room.*
import com.tranzo.custody.data.local.entity.TranzoUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun observeCurrentUser(): Flow<TranzoUserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun fetchCurrentUser(): TranzoUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: TranzoUserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteUser()
}
