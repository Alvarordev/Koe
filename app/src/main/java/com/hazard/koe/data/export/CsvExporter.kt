package com.hazard.koe.data.export

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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    fun formatMoney(cents: Long): String {
        val abs = kotlin.math.abs(cents)
        val sign = if (cents < 0) "-" else ""
        return "$sign${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
    }

    fun formatDate(epochMillis: Long): String = dateFormatter.format(Instant.ofEpochMilli(epochMillis))

    private fun formatOptionalDate(epochMillis: Long?): String = epochMillis?.let { formatDate(it) } ?: ""

    private fun escape(value: String?): String {
        if (value == null) return ""
        return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }

    private fun row(vararg fields: String): String = fields.joinToString(",")

    fun accountsToCsv(accounts: List<Account>): String {
        val header = row(
            "id", "name", "type", "color", "currencyCode", "initialBalance", "currentBalance",
            "cardNetwork", "lastFourDigits", "expirationDate", "creditLimit", "creditUsed",
            "paymentDay", "closingDay", "interestRate", "sortOrder", "isArchived", "createdAt", "updatedAt"
        )
        val lines = accounts.map { a ->
            row(
                a.id.toString(), escape(a.name), a.type.name, escape(a.color), escape(a.currencyCode),
                formatMoney(a.initialBalance), formatMoney(a.currentBalance),
                a.cardNetwork?.name ?: "", escape(a.lastFourDigits), escape(a.expirationDate),
                a.creditLimit?.let { formatMoney(it) } ?: "",
                a.creditUsed?.let { formatMoney(it) } ?: "",
                a.paymentDay?.toString() ?: "", a.closingDay?.toString() ?: "",
                a.interestRate?.toString() ?: "",
                a.sortOrder.toString(), a.isArchived.toString(),
                formatDate(a.createdAt), formatDate(a.updatedAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun transactionsToCsv(transactions: List<Transaction>): String {
        val header = row(
            "id", "type", "amount", "description", "accountId", "transferToAccountId",
            "categoryId", "subscriptionId", "exchangeRate", "convertedAmount", "date",
            "isRecurring", "recurringRuleId", "latitude", "longitude", "createdAt", "updatedAt"
        )
        val lines = transactions.map { t ->
            row(
                t.id.toString(), t.type.name, formatMoney(t.amount), escape(t.description),
                t.accountId.toString(), t.transferToAccountId?.toString() ?: "",
                t.categoryId.toString(), t.subscriptionId?.toString() ?: "",
                t.exchangeRate?.toString() ?: "",
                t.convertedAmount?.let { formatMoney(it) } ?: "",
                formatDate(t.date),
                t.isRecurring.toString(), t.recurringRuleId?.toString() ?: "",
                t.latitude?.toString() ?: "", t.longitude?.toString() ?: "",
                formatDate(t.createdAt), formatDate(t.updatedAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun categoriesToCsv(categories: List<Category>): String {
        val header = row("id", "name", "emoji", "color", "type", "isSystem", "isArchived", "sortOrder", "createdAt")
        val lines = categories.map { c ->
            row(
                c.id.toString(), escape(c.name), escape(c.emoji), escape(c.color),
                c.type.name, c.isSystem.toString(), c.isArchived.toString(),
                c.sortOrder.toString(), formatDate(c.createdAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun budgetsToCsv(budgets: List<Budget>): String {
        val header = row(
            "id", "name", "categoryId", "amountLimit", "periodType",
            "customStartDate", "customEndDate", "alertThreshold", "isActive", "createdAt", "updatedAt"
        )
        val lines = budgets.map { b ->
            row(
                b.id.toString(), escape(b.name), b.categoryId?.toString() ?: "",
                formatMoney(b.amountLimit), b.periodType.name,
                formatOptionalDate(b.customStartDate), formatOptionalDate(b.customEndDate),
                b.alertThreshold.toString(), b.isActive.toString(),
                formatDate(b.createdAt), formatDate(b.updatedAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun personsToCsv(persons: List<Person>): String {
        val header = row("id", "name", "emoji", "phoneNumber", "createdAt")
        val lines = persons.map { p ->
            row(p.id.toString(), escape(p.name), escape(p.emoji), escape(p.phoneNumber), formatDate(p.createdAt))
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun casualLoansToCsv(loans: List<CasualLoan>): String {
        val header = row(
            "id", "personId", "direction", "originalAmount", "outstandingBalance",
            "currencyCode", "description", "dueDate", "isPaidOff", "createdAt", "updatedAt"
        )
        val lines = loans.map { l ->
            row(
                l.id.toString(), l.personId.toString(), l.direction.name,
                formatMoney(l.originalAmount), formatMoney(l.outstandingBalance),
                escape(l.currencyCode), escape(l.description),
                formatOptionalDate(l.dueDate), l.isPaidOff.toString(),
                formatDate(l.createdAt), formatDate(l.updatedAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun casualLoanTransactionsToCsv(txns: List<CasualLoanTransaction>): String {
        val header = row("id", "casualLoanId", "transactionId", "amount", "type", "note", "date", "createdAt")
        val lines = txns.map { t ->
            row(
                t.id.toString(), t.casualLoanId.toString(), t.transactionId.toString(),
                formatMoney(t.amount), t.type.name, escape(t.note),
                formatDate(t.date), formatDate(t.createdAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun formalLoansToCsv(loans: List<FormalLoan>): String {
        val header = row(
            "id", "name", "lenderName", "principalAmount", "outstandingBalance", "currencyCode",
            "annualRate", "monthlyRate", "termMonths", "monthlyPayment", "accountId",
            "startDate", "paymentDayOfMonth", "isActive", "createdAt", "updatedAt"
        )
        val lines = loans.map { l ->
            row(
                l.id.toString(), escape(l.name), escape(l.lenderName),
                formatMoney(l.principalAmount), formatMoney(l.outstandingBalance),
                escape(l.currencyCode), l.annualRate.toString(), l.monthlyRate.toString(),
                l.termMonths.toString(), formatMoney(l.monthlyPayment),
                l.accountId.toString(), formatDate(l.startDate),
                l.paymentDayOfMonth.toString(), l.isActive.toString(),
                formatDate(l.createdAt), formatDate(l.updatedAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun formalLoanPaymentsToCsv(payments: List<FormalLoanPayment>): String {
        val header = row(
            "id", "formalLoanId", "transactionId", "paymentNumber", "dueDate",
            "principalPortion", "interestPortion", "totalAmount", "status", "paidDate", "createdAt"
        )
        val lines = payments.map { p ->
            row(
                p.id.toString(), p.formalLoanId.toString(), p.transactionId?.toString() ?: "",
                p.paymentNumber.toString(), formatDate(p.dueDate),
                formatMoney(p.principalPortion), formatMoney(p.interestPortion),
                formatMoney(p.totalAmount), p.status.name,
                formatOptionalDate(p.paidDate), formatDate(p.createdAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun recurringRulesToCsv(rules: List<RecurringRule>): String {
        val header = row(
            "id", "name", "type", "amount", "accountId", "categoryId", "subscriptionServiceId",
            "frequencyType", "frequencyInterval", "dayOfMonth", "dayOfWeek",
            "startDate", "endDate", "nextOccurrence", "isActive", "createdAt", "updatedAt"
        )
        val lines = rules.map { r ->
            row(
                r.id.toString(), escape(r.name), r.type.name, formatMoney(r.amount),
                r.accountId.toString(), r.categoryId.toString(),
                r.subscriptionServiceId?.toString() ?: "",
                r.frequencyType.name, r.frequencyInterval.toString(),
                r.dayOfMonth?.toString() ?: "", r.dayOfWeek?.toString() ?: "",
                formatDate(r.startDate), formatOptionalDate(r.endDate),
                formatDate(r.nextOccurrence), r.isActive.toString(),
                formatDate(r.createdAt), formatDate(r.updatedAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    fun userSubscriptionsToCsv(subscriptions: List<UserSubscription>): String {
        val header = row(
            "id", "serviceId", "accountId", "amount", "billingDay", "currencyCode",
            "customName", "customEmoji", "iconResName", "isArchived", "createdAt", "updatedAt"
        )
        val lines = subscriptions.map { s ->
            row(
                s.id.toString(), s.serviceId?.toString() ?: "", s.accountId.toString(),
                formatMoney(s.amount), s.billingDay.toString(), escape(s.currencyCode),
                escape(s.customName), escape(s.customEmoji), escape(s.iconResName),
                s.isArchived.toString(), formatDate(s.createdAt), formatDate(s.updatedAt)
            )
        }
        return (listOf(header) + lines).joinToString("\n")
    }
}
