package com.tranzo.custody.data.repository

import com.tranzo.custody.data.local.dao.TransactionDao
import com.tranzo.custody.data.local.entity.TransactionEntity
import com.tranzo.custody.data.remote.StreamApi
import com.tranzo.custody.data.remote.StreamDto
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.Transaction
import com.tranzo.custody.domain.model.TransactionStatus
import com.tranzo.custody.domain.model.TransactionType
import com.tranzo.custody.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val streamApi: StreamApi,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())

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
        try {
            val sent = streamApi.listSentStreams()
            val received = streamApi.listReceivedStreams()

            val sentTx = sent.map { it.toTransaction(TransactionType.SENT) }
            val receivedTx = received.map { it.toTransaction(TransactionType.RECEIVED) }

            val combined = (sentTx + receivedTx).sortedByDescending { it.timestamp }
            _transactions.value = combined

            // Cache to local DB
            transactionDao.deleteAll()
            transactionDao.insertAll(combined.map { it.toEntity() })
        } catch (_: Exception) {
            // Fall back to local cache on failure
            if (_transactions.value.isEmpty()) {
                val cached = transactionDao.observeAllTransactions().first()
                if (cached.isNotEmpty()) {
                    _transactions.value = cached.map { it.toDomain() }
                }
            }
        }
    }

    private fun StreamDto.toTransaction(type: TransactionType): Transaction {
        val timestamp = try {
            Instant.parse(createdAt).toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }

        val status = when (this.status) {
            "ACTIVE", "COMPLETED" -> TransactionStatus.CONFIRMED
            "CANCELLED" -> TransactionStatus.FAILED
            else -> TransactionStatus.PENDING
        }

        val title = if (type == TransactionType.SENT) "Stream to ${recipientAddr.take(8)}…" else "Stream from sender"
        val amount = "${totalAmount} $token"

        return Transaction(
            id = id,
            type = type,
            title = title,
            amount = amount,
            fiatAmount = "",
            timestamp = timestamp,
            status = status,
            chain = Chain.POLYGON,
            txHash = txHash,
            toAddress = recipientAddr
        )
    }

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        type = type.name,
        title = title,
        amount = amount,
        fiatAmount = fiatAmount,
        timestamp = timestamp,
        status = status.name,
        chain = chain?.name,
        txHash = txHash
    )

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        type = TransactionType.valueOf(type),
        title = title,
        amount = amount,
        fiatAmount = fiatAmount,
        timestamp = timestamp,
        status = TransactionStatus.valueOf(status),
        chain = chain?.let { try { Chain.valueOf(it) } catch (_: Exception) { null } },
        txHash = txHash
    )
}
