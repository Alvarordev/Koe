package com.hazard.koe.domain.usecase.person

import com.hazard.koe.domain.repository.PersonRepository

class SavePersonUseCase(private val repository: PersonRepository) {
    suspend operator fun invoke(
        name: String,
        emoji: String? = null,
        phoneNumber: String? = null
    ): Long {
        val person = com.hazard.koe.data.model.Person(
            name = name,
            emoji = emoji,
            phoneNumber = phoneNumber
        )
        return repository.create(person)
    }
}