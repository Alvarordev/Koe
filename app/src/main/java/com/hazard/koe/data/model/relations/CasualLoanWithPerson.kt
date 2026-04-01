package com.hazard.koe.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.hazard.koe.data.model.CasualLoan
import com.hazard.koe.data.model.Person

data class CasualLoanWithPerson(
    @Embedded val loan: CasualLoan,
    @Relation(parentColumn = "personId", entityColumn = "id")
    val persons: List<Person>
) {
    val person: Person? get() = persons.firstOrNull()
}
