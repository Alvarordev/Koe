package com.hazard.koe.domain.usecase.account

import com.hazard.koe.data.model.Account
import com.hazard.koe.domain.repository.AccountRepository

class CreateAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account) = repository.create(account)
}
