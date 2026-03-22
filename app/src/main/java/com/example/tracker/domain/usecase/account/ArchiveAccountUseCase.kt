package com.example.tracker.domain.usecase.account

import com.example.tracker.domain.repository.AccountRepository

class ArchiveAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(id: Long) = repository.archive(id)
}
