package com.tranzo.custody.domain.repository

import com.tranzo.custody.domain.model.Transaction
import com.tranzo.custody.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    suspend fun getTransactionDetail(id: String): Transaction?
    suspend fun refreshTransactions()
}
