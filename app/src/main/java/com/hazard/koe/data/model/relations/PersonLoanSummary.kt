package com.hazard.koe.data.model.relations

import com.hazard.koe.data.enums.LoanDirection

data class PersonLoanSummary(
    val personId: Long,
    val personName: String,
    val direction: LoanDirection,
    val totalOutstanding: Long
)
