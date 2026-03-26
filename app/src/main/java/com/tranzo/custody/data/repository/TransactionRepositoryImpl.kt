package com.tranzo.custody.data.repository

import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.Transaction
import com.tranzo.custody.domain.model.TransactionStatus
import com.tranzo.custody.domain.model.TransactionType
import com.tranzo.custody.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor() : TransactionRepository {

    private val _transactions = MutableStateFlow(getMockTransactions())

    override fun getAllTransactions(): Flow<List<Transaction>> = _transactions.asStateFlow()

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return _transactions.map { list -> list.filter { it.type == type } }
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return _transactions.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                    (it.merchantName?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    override suspend fun getTransactionDetail(id: String): Transaction? {
        return _transactions.value.find { it.id == id }
    }

    override suspend fun refreshTransactions() {
        _transactions.value = getMockTransactions()
    }

    private fun getMockTransactions(): List<Transaction> {
        val now = System.currentTimeMillis()
        return listOf(
            Transaction("tx_1", TransactionType.RECEIVED, "Received ETH", "+0.5 ETH", "+$1,598.50", now - 1800000, TransactionStatus.CONFIRMED, Chain.ETHEREUM, "0xabc123...", "$2.40", null, "0x742d...bD18", networkName = "Ethereum", confirmations = 47),
            Transaction("tx_2", TransactionType.CARD_SPEND, "Amazon", "-$89.99", "-$89.99", now - 3600000, TransactionStatus.CONFIRMED, merchantName = "Amazon"),
            Transaction("tx_3", TransactionType.SENT, "Sent BTC", "-0.025 BTC", "-$2,587.50", now - 7200000, TransactionStatus.CONFIRMED, Chain.BITCOIN, "0xdef456...", "$1.20", "0x742d...bD18", "bc1q...wlh", networkName = "Bitcoin", confirmations = 6),
            Transaction("tx_4", TransactionType.SWAPPED, "ETH → USDC", "0.3 ETH → 959.1 USDC", "$959.10", now - 14400000, TransactionStatus.CONFIRMED, Chain.ETHEREUM, "0xghi789...", "$8.50", networkName = "Ethereum", confirmations = 35),
            Transaction("tx_5", TransactionType.CARD_SPEND, "Starbucks", "-$6.45", "-$6.45", now - 21600000, TransactionStatus.CONFIRMED, merchantName = "Starbucks"),
            Transaction("tx_6", TransactionType.BOUGHT, "Bought SOL", "+10 SOL", "$1,360.00", now - 43200000, TransactionStatus.CONFIRMED, Chain.SOLANA),
            Transaction("tx_7", TransactionType.SENT, "Sent USDC", "-500 USDC", "-$500.00", now - 86400000, TransactionStatus.PENDING, Chain.ETHEREUM, "0xjkl012...", "$3.10", "0x742d...bD18", "0x9f8e...3d2a", networkName = "Ethereum", confirmations = 0),
            Transaction("tx_8", TransactionType.CARD_SPEND, "Shell Gas", "-$52.30", "-$52.30", now - 129600000, TransactionStatus.CONFIRMED, merchantName = "Shell Gas"),
            Transaction("tx_9", TransactionType.RECEIVED, "Received MATIC", "+500 MATIC", "+$225.00", now - 172800000, TransactionStatus.CONFIRMED, Chain.POLYGON, "0xmno345...", "$0.05", null, "0x742d...bD18", networkName = "Polygon", confirmations = 250),
            Transaction("tx_10", TransactionType.CARD_SPEND, "Spotify", "-$9.99", "-$9.99", now - 259200000, TransactionStatus.CONFIRMED, merchantName = "Spotify")
        )
    }
}
