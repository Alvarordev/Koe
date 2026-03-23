package com.example.tracker.domain.exception

class DuplicateTransactionException(val operationNumber: String) :
    Exception("Duplicate transaction: $operationNumber")
