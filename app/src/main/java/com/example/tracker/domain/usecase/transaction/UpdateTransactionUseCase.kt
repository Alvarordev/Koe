package com.example.tracker.domain.usecase.transaction

import com.example.tracker.data.model.Transaction
import com.example.tracker.domain.repository.TransactionRepository

class UpdateTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.update(transaction)
}
