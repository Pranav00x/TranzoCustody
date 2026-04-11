package com.tranzo.custody.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tranzo.custody.domain.model.Chain

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val chain: Chain,
    val balance: Double = 0.0,
    val fiatBalance: Double = 0.0,
    val price: Double = 0.0,
    val priceChange24h: Double = 0.0,
    val contractAddress: String? = null,
    val decimals: Int = 18,
    val lastUpdated: Long = System.currentTimeMillis()
)
