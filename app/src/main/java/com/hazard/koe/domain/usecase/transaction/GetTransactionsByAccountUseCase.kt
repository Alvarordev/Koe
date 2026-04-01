package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.domain.repository.TransactionRepository

class GetTransactionsByAccountUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: Long) = repository.getByAccount(accountId)
}
