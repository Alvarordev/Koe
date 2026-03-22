package com.example.tracker.data.model.relations

import com.example.tracker.data.enums.LoanDirection

data class PersonLoanSummary(
    val personId: Long,
    val personName: String,
    val direction: LoanDirection,
    val totalOutstanding: Long
)
