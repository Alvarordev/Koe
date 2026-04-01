package com.example.tracker.domain.usecase.account

import com.example.tracker.data.model.Account
import com.example.tracker.domain.repository.AccountRepository

class UpdateAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account) = repository.update(account)
}
