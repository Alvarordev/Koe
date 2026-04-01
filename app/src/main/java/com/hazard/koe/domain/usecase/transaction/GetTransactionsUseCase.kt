package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.domain.repository.TransactionRepository

class GetTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke() = repository.getAll()
}
