package com.tranzo.custody.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val amount: String,
    val fiatAmount: String,
    val timestamp: Long,
    val status: String,
    val chain: String? = null,
    val txHash: String? = null,
    val fee: String? = null,
    val fromAddress: String? = null,
    val toAddress: String? = null,
    val merchantName: String? = null,
    val confirmations: Int = 0,
    val networkName: String? = null,
    val explorerUrl: String? = null
)
