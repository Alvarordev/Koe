package com.example.tracker.domain.usecase.account

import com.example.tracker.data.model.Account
import com.example.tracker.domain.repository.AccountRepository

class CreateAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account) = repository.create(account)
}
