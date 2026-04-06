package com.hazard.koe.domain.usecase.database

import androidx.room.withTransaction
import com.hazard.koe.data.db.DatabaseSeeder
import com.hazard.koe.data.db.TrackerDatabase
import com.hazard.koe.data.export.CsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.zip.ZipInputStream

class ImportDataUseCase(private val database: TrackerDatabase) {

    suspend operator fun invoke(inputStream: InputStream): ImportResult =
        withContext(Dispatchers.IO) {
            val csvFiles = readZipEntries(inputStream)

            val requiredFiles = listOf("accounts.csv", "categories.csv")
            for (file in requiredFiles) {
                if (file !in csvFiles) {
                    throw ImportMissingFileException(file)
                }
            }

            val categories = CsvParser.parseCategories(csvFiles["categories.csv"]!!)
            val accounts = CsvParser.parseAccounts(csvFiles["accounts.csv"]!!)
            val persons = csvFiles["persons.csv"]?.let { CsvParser.parsePersons(it) } ?: emptyList()
            val recurringRules = csvFiles["recurring_rules.csv"]?.let { CsvParser.parseRecurringRules(it) } ?: emptyList()
            val userSubscriptions = csvFiles["user_subscriptions.csv"]?.let { CsvParser.parseUserSubscriptions(it) } ?: emptyList()
            val budgets = csvFiles["budgets.csv"]?.let { CsvParser.parseBudgets(it) } ?: emptyList()
            val casualLoans = csvFiles["casual_loans.csv"]?.let { CsvParser.parseCasualLoans(it) } ?: emptyList()
            val transactions = csvFiles["transactions.csv"]?.let { CsvParser.parseTransactions(it) } ?: emptyList()
            val casualLoanTxns = csvFiles["casual_loan_transactions.csv"]?.let { CsvParser.parseCasualLoanTransactions(it) } ?: emptyList()
            val formalLoans = csvFiles["formal_loans.csv"]?.let { CsvParser.parseFormalLoans(it) } ?: emptyList()
            val formalLoanPayments = csvFiles["formal_loan_payments.csv"]?.let { CsvParser.parseFormalLoanPayments(it) } ?: emptyList()

            database.withTransaction {
                DatabaseSeeder.clearAllTables(database.openHelper.writableDatabase)

                database.categoryDao().insertAllReplace(categories)
                database.accountDao().insertAll(accounts)
                database.personDao().insertAll(persons)
                database.recurringRuleDao().insertAll(recurringRules)
                database.userSubscriptionDao().insertAll(userSubscriptions)
                database.budgetDao().insertAll(budgets)
                database.casualLoanDao().insertAll(casualLoans)
                database.transactionDao().insertAll(transactions)
                database.casualLoanTransactionDao().insertAll(casualLoanTxns)
                database.formalLoanDao().insertAll(formalLoans)
                database.formalLoanPaymentDao().insertAll(formalLoanPayments)
            }

            ImportResult(
                accountCount = accounts.size,
                transactionCount = transactions.size,
                categoryCount = categories.size,
                budgetCount = budgets.size,
                personCount = persons.size,
                casualLoanCount = casualLoans.size,
                formalLoanCount = formalLoans.size,
                recurringRuleCount = recurringRules.size,
                userSubscriptionCount = userSubscriptions.size
            )
        }

    private fun readZipEntries(inputStream: InputStream): Map<String, String> {
        val entries = mutableMapOf<String, String>()
        ZipInputStream(inputStream.buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv")) {
                    entries[entry.name] = zis.readBytes().toString(Charsets.UTF_8)
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return entries
    }
}

data class ImportResult(
    val accountCount: Int,
    val transactionCount: Int,
    val categoryCount: Int,
    val budgetCount: Int = 0,
    val personCount: Int = 0,
    val casualLoanCount: Int = 0,
    val formalLoanCount: Int = 0,
    val recurringRuleCount: Int = 0,
    val userSubscriptionCount: Int = 0
)

class ImportMissingFileException(val fileName: String) :
    Exception("Archivo requerido faltante en el respaldo: $fileName")
