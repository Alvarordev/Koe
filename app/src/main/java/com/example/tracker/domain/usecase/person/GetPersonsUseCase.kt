package com.example.tracker.domain.usecase.person

import com.example.tracker.domain.repository.PersonRepository

class GetPersonsUseCase(private val repository: PersonRepository) {
    operator fun invoke() = repository.getAll()
}
