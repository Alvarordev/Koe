package com.hazard.koe.data.export

import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.BudgetPeriodType
import com.hazard.koe.data.enums.CardNetwork
import com.hazard.koe.data.enums.CasualLoanTxnType
import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.enums.FrequencyType
import com.hazard.koe.data.enums.LoanDirection
import com.hazard.koe.data.enums.PaymentStatus
import com.hazard.koe.data.enums.RecurringType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Budget
import com.hazard.koe.data.model.CasualLoan
import com.hazard.koe.data.model.CasualLoanTransaction
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.FormalLoan
import com.hazard.koe.data.model.FormalLoanPayment
import com.hazard.koe.data.model.Person
import com.hazard.koe.data.model.RecurringRule
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.UserSubscription
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvParser {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    fun parseMoney(value: String): Long {
        if (value.isEmpty()) return 0L
        val negative = value.startsWith('-')
        val abs = if (negative) value.substring(1) else value
        val dotIndex = abs.indexOf('.')
        return if (dotIndex == -1) {
            val result = abs.toLong() * 100
            if (negative) -result else result
        } else {
            val intPart = abs.substring(0, dotIndex).toLong()
            val decStr = abs.substring(dotIndex + 1).padEnd(2, '0').substring(0, 2)
            val decPart = decStr.toLong()
            val result = intPart * 100 + decPart
            if (negative) -result else result
        }
    }

    fun parseDate(value: String): Long {
        val ldt = LocalDateTime.parse(value, dateFormatter)
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun parseOptionalDate(value: String): Long? = if (value.isEmpty()) null else parseDate(value)

    fun parseOptionalLong(value: String): Long? = if (value.isEmpty()) null else value.toLong()

    fun parseOptionalInt(value: String): Int? = if (value.isEmpty()) null else value.toInt()

    fun parseOptionalDouble(value: String): Double? = if (value.isEmpty()) null else value.toDouble()

    fun parseOptionalMoney(value: String): Long? = if (value.isEmpty()) null else parseMoney(value)

    fun parseOptionalString(value: String): String? = if (value.isEmpty()) null else value

    fun parseCsvLines(content: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < content.length) {
            val ch = content[i]
            when {
                inQuotes && ch == '"' -> {
                    if (i + 1 < content.length && content[i + 1] == '"') {
                        currentField.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                !inQuotes && ch == '"' -> {
                    inQuotes = true
                }
                !inQuotes && ch == ',' -> {
                    currentRow.add(currentField.toString())
                    currentField.clear()
                }
                !inQuotes && ch == '\n' -> {
                    currentRow.add(currentField.toString())
                    currentField.clear()
                    result.add(currentRow.toList())
                    currentRow.clear()
                }
                !inQuotes && ch == '\r' -> {
                    // skip carriage return
                }
                else -> {
                    currentField.append(ch)
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            result.add(currentRow.toList())
        }

        return result
    }

    private fun buildIndex(headers: List<String>): Map<String, Int> =
        headers.mapIndexed { i, name -> name to i }.toMap()

    fun parseAccounts(csv: String): List<Account> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            Account(
                id = row[idx["id"]!!].toLong(),
                name = row[idx["name"]!!],
                type = AccountType.valueOf(row[idx["type"]!!]),
                color = row[idx["color"]!!],
                currencyCode = row[idx["currencyCode"]!!],
                initialBalance = parseMoney(row[idx["initialBalance"]!!]),
                currentBalance = parseMoney(row[idx["currentBalance"]!!]),
                cardNetwork = parseOptionalString(row[idx["cardNetwork"]!!])?.let { CardNetwork.valueOf(it) },
                lastFourDigits = parseOptionalString(row[idx["lastFourDigits"]!!]),
                expirationDate = parseOptionalString(row[idx["expirationDate"]!!]),
                creditLimit = parseOptionalMoney(row[idx["creditLimit"]!!]),
                creditUsed = parseOptionalMoney(row[idx["creditUsed"]!!]),
                paymentDay = parseOptionalInt(row[idx["paymentDay"]!!]),
                closingDay = parseOptionalInt(row[idx["closingDay"]!!]),
                interestRate = parseOptionalDouble(row[idx["interestRate"]!!]),
                sortOrder = row[idx["sortOrder"]!!].toInt(),
                isArchived = row[idx["isArchived"]!!].toBoolean(),
                createdAt = parseDate(row[idx["createdAt"]!!]),
                updatedAt = parseDate(row[idx["updatedAt"]!!])
            )
        }
    }

    fun parseTransactions(csv: String): List<Transaction> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            Transaction(
                id = row[idx["id"]!!].toLong(),
                type = TransactionType.valueOf(row[idx["type"]!!]),
                amount = parseMoney(row[idx["amount"]!!]),
                description = parseOptionalString(row[idx["description"]!!]),
                accountId = row[idx["accountId"]!!].toLong(),
                transferToAccountId = parseOptionalLong(row[idx["transferToAccountId"]!!]),
                categoryId = row[idx["categoryId"]!!].toLong(),
                subscriptionId = parseOptionalLong(row[idx["subscriptionId"]!!]),
                exchangeRate = parseOptionalDouble(row[idx["exchangeRate"]!!]),
                convertedAmount = parseOptionalMoney(row[idx["convertedAmount"]!!]),
                date = parseDate(row[idx["date"]!!]),
                isRecurring = row[idx["isRecurring"]!!].toBoolean(),
                recurringRuleId = parseOptionalLong(row[idx["recurringRuleId"]!!]),
                latitude = parseOptionalDouble(row[idx["latitude"]!!]),
                longitude = parseOptionalDouble(row[idx["longitude"]!!]),
                createdAt = parseDate(row[idx["createdAt"]!!]),
                updatedAt = parseDate(row[idx["updatedAt"]!!])
            )
        }
    }

    fun parseCategories(csv: String): List<Category> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            Category(
                id = row[idx["id"]!!].toLong(),
                name = row[idx["name"]!!],
                emoji = row[idx["emoji"]!!],
                color = row[idx["color"]!!],
                type = CategoryType.valueOf(row[idx["type"]!!]),
                isSystem = row[idx["isSystem"]!!].toBoolean(),
                isArchived = row[idx["isArchived"]!!].toBoolean(),
                sortOrder = row[idx["sortOrder"]!!].toInt(),
                createdAt = parseDate(row[idx["createdAt"]!!])
            )
        }
    }

    fun parseBudgets(csv: String): List<Budget> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            Budget(
                id = row[idx["id"]!!].toLong(),
                name = row[idx["name"]!!],
                categoryId = parseOptionalLong(row[idx["categoryId"]!!]),
                amountLimit = parseMoney(row[idx["amountLimit"]!!]),
                periodType = BudgetPeriodType.valueOf(row[idx["periodType"]!!]),
                customStartDate = parseOptionalDate(row[idx["customStartDate"]!!]),
                customEndDate = parseOptionalDate(row[idx["customEndDate"]!!]),
                alertThreshold = row[idx["alertThreshold"]!!].toDouble(),
                isActive = row[idx["isActive"]!!].toBoolean(),
                createdAt = parseDate(row[idx["createdAt"]!!]),
                updatedAt = parseDate(row[idx["updatedAt"]!!])
            )
        }
    }

    fun parsePersons(csv: String): List<Person> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            Person(
                id = row[idx["id"]!!].toLong(),
                name = row[idx["name"]!!],
                emoji = parseOptionalString(row[idx["emoji"]!!]),
                phoneNumber = parseOptionalString(row[idx["phoneNumber"]!!]),
                createdAt = parseDate(row[idx["createdAt"]!!])
            )
        }
    }

    fun parseCasualLoans(csv: String): List<CasualLoan> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            CasualLoan(
                id = row[idx["id"]!!].toLong(),
                personId = row[idx["personId"]!!].toLong(),
                direction = LoanDirection.valueOf(row[idx["direction"]!!]),
                originalAmount = parseMoney(row[idx["originalAmount"]!!]),
                outstandingBalance = parseMoney(row[idx["outstandingBalance"]!!]),
                currencyCode = row[idx["currencyCode"]!!],
                description = parseOptionalString(row[idx["description"]!!]),
                dueDate = parseOptionalDate(row[idx["dueDate"]!!]),
                isPaidOff = row[idx["isPaidOff"]!!].toBoolean(),
                createdAt = parseDate(row[idx["createdAt"]!!]),
                updatedAt = parseDate(row[idx["updatedAt"]!!])
            )
        }
    }

    fun parseCasualLoanTransactions(csv: String): List<CasualLoanTransaction> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            CasualLoanTransaction(
                id = row[idx["id"]!!].toLong(),
                casualLoanId = row[idx["casualLoanId"]!!].toLong(),
                transactionId = row[idx["transactionId"]!!].toLong(),
                amount = parseMoney(row[idx["amount"]!!]),
                type = CasualLoanTxnType.valueOf(row[idx["type"]!!]),
                note = parseOptionalString(row[idx["note"]!!]),
                date = parseDate(row[idx["date"]!!]),
                createdAt = parseDate(row[idx["createdAt"]!!])
            )
        }
    }

    fun parseFormalLoans(csv: String): List<FormalLoan> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            FormalLoan(
                id = row[idx["id"]!!].toLong(),
                name = row[idx["name"]!!],
                lenderName = row[idx["lenderName"]!!],
                principalAmount = parseMoney(row[idx["principalAmount"]!!]),
                outstandingBalance = parseMoney(row[idx["outstandingBalance"]!!]),
                currencyCode = row[idx["currencyCode"]!!],
                annualRate = row[idx["annualRate"]!!].toDouble(),
                monthlyRate = row[idx["monthlyRate"]!!].toDouble(),
                termMonths = row[idx["termMonths"]!!].toInt(),
                monthlyPayment = parseMoney(row[idx["monthlyPayment"]!!]),
                accountId = row[idx["accountId"]!!].toLong(),
                startDate = parseDate(row[idx["startDate"]!!]),
                paymentDayOfMonth = row[idx["paymentDayOfMonth"]!!].toInt(),
                isActive = row[idx["isActive"]!!].toBoolean(),
                createdAt = parseDate(row[idx["createdAt"]!!]),
                updatedAt = parseDate(row[idx["updatedAt"]!!])
            )
        }
    }

    fun parseFormalLoanPayments(csv: String): List<FormalLoanPayment> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            FormalLoanPayment(
                id = row[idx["id"]!!].toLong(),
                formalLoanId = row[idx["formalLoanId"]!!].toLong(),
                transactionId = parseOptionalLong(row[idx["transactionId"]!!]),
                paymentNumber = row[idx["paymentNumber"]!!].toInt(),
                dueDate = parseDate(row[idx["dueDate"]!!]),
                principalPortion = parseMoney(row[idx["principalPortion"]!!]),
                interestPortion = parseMoney(row[idx["interestPortion"]!!]),
                totalAmount = parseMoney(row[idx["totalAmount"]!!]),
                status = PaymentStatus.valueOf(row[idx["status"]!!]),
                paidDate = parseOptionalDate(row[idx["paidDate"]!!]),
                createdAt = parseDate(row[idx["createdAt"]!!])
            )
        }
    }

    fun parseRecurringRules(csv: String): List<RecurringRule> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            RecurringRule(
                id = row[idx["id"]!!].toLong(),
                name = row[idx["name"]!!],
                type = RecurringType.valueOf(row[idx["type"]!!]),
                amount = parseMoney(row[idx["amount"]!!]),
                accountId = row[idx["accountId"]!!].toLong(),
                categoryId = row[idx["categoryId"]!!].toLong(),
                subscriptionServiceId = null,
                frequencyType = FrequencyType.valueOf(row[idx["frequencyType"]!!]),
                frequencyInterval = row[idx["frequencyInterval"]!!].toInt(),
                dayOfMonth = parseOptionalInt(row[idx["dayOfMonth"]!!]),
                dayOfWeek = parseOptionalInt(row[idx["dayOfWeek"]!!]),
                startDate = parseDate(row[idx["startDate"]!!]),
                endDate = parseOptionalDate(row[idx["endDate"]!!]),
                nextOccurrence = parseDate(row[idx["nextOccurrence"]!!]),
                isActive = row[idx["isActive"]!!].toBoolean(),
                createdAt = parseDate(row[idx["createdAt"]!!]),
                updatedAt = parseDate(row[idx["updatedAt"]!!])
            )
        }
    }

    fun parseUserSubscriptions(csv: String): List<UserSubscription> {
        val rows = parseCsvLines(csv)
        if (rows.size < 2) return emptyList()
        val idx = buildIndex(rows[0])
        return rows.drop(1).map { row ->
            UserSubscription(
                id = row[idx["id"]!!].toLong(),
                serviceId = parseOptionalLong(row[idx["serviceId"]!!]),
                accountId = row[idx["accountId"]!!].toLong(),
                amount = parseMoney(row[idx["amount"]!!]),
                billingDay = row[idx["billingDay"]!!].toInt(),
                currencyCode = row[idx["currencyCode"]!!],
                customName = parseOptionalString(row[idx["customName"]!!]),
                customEmoji = parseOptionalString(row[idx["customEmoji"]!!]),
                iconResName = parseOptionalString(row[idx["iconResName"]!!]),
                isArchived = row[idx["isArchived"]!!].toBoolean(),
                createdAt = parseDate(row[idx["createdAt"]!!]),
                updatedAt = parseDate(row[idx["updatedAt"]!!])
            )
        }
    }
}
