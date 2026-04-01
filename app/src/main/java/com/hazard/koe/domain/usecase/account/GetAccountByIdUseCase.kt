package com.hazard.koe.domain.usecase.account

import com.hazard.koe.domain.repository.AccountRepository

class GetAccountByIdUseCase(private val repository: AccountRepository) {
    operator fun invoke(id: Long) = repository.getById(id)
}
