package com.hazard.koe.domain.usecase.person

import com.hazard.koe.domain.repository.PersonRepository

class GetPersonsUseCase(private val repository: PersonRepository) {
    operator fun invoke() = repository.getAll()
}
