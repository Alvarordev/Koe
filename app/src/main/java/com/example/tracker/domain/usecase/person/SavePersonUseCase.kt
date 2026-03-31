package com.example.tracker.domain.usecase.person

import com.example.tracker.domain.repository.PersonRepository

class SavePersonUseCase(private val repository: PersonRepository) {
    suspend operator fun invoke(
        name: String,
        emoji: String? = null,
        phoneNumber: String? = null
    ): Long {
        val person = com.example.tracker.data.model.Person(
            name = name,
            emoji = emoji,
            phoneNumber = phoneNumber
        )
        return repository.create(person)
    }
}