package com.hazard.koe.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.hazard.koe.data.model.Budget
import com.hazard.koe.data.model.Category

data class BudgetWithCategory(
    @Embedded val budget: Budget,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val categories: List<Category>
) {
    val category: Category? get() = categories.firstOrNull()
}
