package com.hazard.koe.domain.usecase.account

import com.hazard.koe.data.model.Account
import com.hazard.koe.domain.repository.AccountRepository

class UpdateAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account) = repository.update(account)
}
