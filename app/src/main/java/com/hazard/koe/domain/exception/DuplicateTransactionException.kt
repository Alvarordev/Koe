package com.hazard.koe.domain.exception

class DuplicateTransactionException(val operationNumber: String) :
    Exception("Duplicate transaction: $operationNumber")
