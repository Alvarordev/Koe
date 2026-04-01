package com.hazard.koe.domain.usecase.account

import com.hazard.koe.domain.repository.AccountRepository

class GetAccountsUseCase(private val repository: AccountRepository) {
    operator fun invoke() = repository.getAll()
}
