package com.example.tracker.domain.usecase.transaction

import com.example.tracker.domain.repository.TransactionRepository

class GetTransactionsByAccountUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: Long) = repository.getByAccount(accountId)
}
