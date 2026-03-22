package com.example.tracker.domain.usecase.account

import com.example.tracker.domain.repository.AccountRepository

class GetAccountsUseCase(private val repository: AccountRepository) {
    operator fun invoke() = repository.getAll()
}
