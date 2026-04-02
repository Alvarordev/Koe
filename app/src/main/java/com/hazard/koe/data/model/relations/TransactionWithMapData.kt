package com.hazard.koe.data.model.relations

data class TransactionWithMapData(
    val id: Long,
    val amount: Long,
    val date: Long,
    val latitude: Double,
    val longitude: Double,
    val categoryEmoji: String,
    val categoryColor: String,
    val categoryName: String
)
