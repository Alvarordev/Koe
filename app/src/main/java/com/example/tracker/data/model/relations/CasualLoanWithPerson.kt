package com.example.tracker.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.tracker.data.model.CasualLoan
import com.example.tracker.data.model.Person

data class CasualLoanWithPerson(
    @Embedded val loan: CasualLoan,
    @Relation(parentColumn = "personId", entityColumn = "id")
    val persons: List<Person>
) {
    val person: Person? get() = persons.firstOrNull()
}
