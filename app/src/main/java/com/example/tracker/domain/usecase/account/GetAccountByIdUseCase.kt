package com.example.tracker.domain.usecase.account

import com.example.tracker.domain.repository.AccountRepository

class GetAccountByIdUseCase(private val repository: AccountRepository) {
    operator fun invoke(id: Long) = repository.getById(id)
}
