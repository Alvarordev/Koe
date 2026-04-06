package com.hazard.koe.domain.usecase.database

import com.hazard.koe.data.db.TrackerDatabase
import com.hazard.koe.data.export.CsvExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportDataUseCase(private val database: TrackerDatabase) {

    suspend operator fun invoke(cacheDir: File): File = withContext(Dispatchers.IO) {
        val accounts = database.accountDao().getAll().first()
        val categories = database.categoryDao().getAll().first()
        val persons = database.personDao().getAll().first()
        val formalLoans = database.formalLoanDao().getAll().first()
        val transactions = database.transactionDao().getAllRaw()
        val budgets = database.budgetDao().getAllRaw()
        val casualLoans = database.casualLoanDao().getAllRaw()
        val casualLoanTxns = database.casualLoanTransactionDao().getAllRaw()
        val formalLoanPayments = database.formalLoanPaymentDao().getAllRaw()
        val recurringRules = database.recurringRuleDao().getAllRaw()
        val userSubscriptions = database.userSubscriptionDao().getAllRaw()

        val csvFiles = mapOf(
            "accounts.csv" to CsvExporter.accountsToCsv(accounts),
            "transactions.csv" to CsvExporter.transactionsToCsv(transactions),
            "categories.csv" to CsvExporter.categoriesToCsv(categories),
            "budgets.csv" to CsvExporter.budgetsToCsv(budgets),
            "persons.csv" to CsvExporter.personsToCsv(persons),
            "casual_loans.csv" to CsvExporter.casualLoansToCsv(casualLoans),
            "casual_loan_transactions.csv" to CsvExporter.casualLoanTransactionsToCsv(casualLoanTxns),
            "formal_loans.csv" to CsvExporter.formalLoansToCsv(formalLoans),
            "formal_loan_payments.csv" to CsvExporter.formalLoanPaymentsToCsv(formalLoanPayments),
            "recurring_rules.csv" to CsvExporter.recurringRulesToCsv(recurringRules),
            "user_subscriptions.csv" to CsvExporter.userSubscriptionsToCsv(userSubscriptions),
        )

        val exportDir = File(cacheDir, "exports").apply { mkdirs() }
        val dateStr = LocalDate.now().toString()
        val zipFile = File(exportDir, "koe-export-$dateStr.zip")

        ZipOutputStream(zipFile.outputStream().buffered()).use { zos ->
            csvFiles.forEach { (name, content) ->
                zos.putNextEntry(ZipEntry(name))
                zos.write(content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }

        zipFile
    }
}
