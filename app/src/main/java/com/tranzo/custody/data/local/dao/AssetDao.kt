package com.tranzo.custody.data.local.dao

import androidx.room.*
import com.tranzo.custody.data.local.entity.AssetEntity
import com.tranzo.custody.domain.model.Chain
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY balance * price DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE chain = :chain ORDER BY balance * price DESC")
    fun getAssetsByChain(chain: Chain): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE symbol = :symbol AND chain = :chain")
    suspend fun deleteAsset(symbol: String, chain: Chain)

    @Query("DELETE FROM assets")
    suspend fun deleteAll()
}
