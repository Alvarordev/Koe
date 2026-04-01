package com.hazard.koe.domain.usecase.account

import com.hazard.koe.domain.repository.AccountRepository

class ArchiveAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(id: Long) = repository.archive(id)
}
