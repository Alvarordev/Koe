package com.example.tracker.data.model.relations

data class CategoryTotal(
    val categoryId: Long,
    val categoryName: String,
    val emoji: String,
    val color: String,
    val total: Long
)
