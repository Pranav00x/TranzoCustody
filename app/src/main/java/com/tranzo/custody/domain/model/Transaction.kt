package com.tranzo.custody.domain.model

data class Transaction(
    val id: String,
    val type: TransactionType,
    val title: String,
    val amount: String,
    val fiatAmount: String,
    val timestamp: Long,
    val status: TransactionStatus,
    val chain: Chain? = null,
    val txHash: String? = null,
    val fee: String? = null,
    val fromAddress: String? = null,
    val toAddress: String? = null,
    val merchantName: String? = null,
    val confirmations: Int = 0,
    val networkName: String? = null,
    val explorerUrl: String? = null
)

enum class TransactionType {
    SENT, RECEIVED, SWAPPED, CARD_SPEND, BOUGHT
}

enum class TransactionStatus {
    PENDING, CONFIRMED, FAILED
}
