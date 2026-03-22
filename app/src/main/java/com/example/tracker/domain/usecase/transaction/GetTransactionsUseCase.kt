package com.example.tracker.domain.usecase.transaction

import com.example.tracker.domain.repository.TransactionRepository

class GetTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke() = repository.getAll()
}
