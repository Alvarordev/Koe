package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.data.model.Transaction
import com.hazard.koe.domain.repository.TransactionRepository

class UpdateTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.update(transaction)
}
