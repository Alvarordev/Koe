package com.example.tracker.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.tracker.data.model.Budget
import com.example.tracker.data.model.Category

data class BudgetWithCategory(
    @Embedded val budget: Budget,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val categories: List<Category>
) {
    val category: Category? get() = categories.firstOrNull()
}
