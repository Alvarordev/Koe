package com.example.tracker.di

import com.example.tracker.domain.usecase.account.ArchiveAccountUseCase
import com.example.tracker.domain.usecase.account.CreateAccountUseCase
import com.example.tracker.domain.usecase.account.GetAccountsUseCase
import com.example.tracker.domain.usecase.budget.CreateBudgetUseCase
import com.example.tracker.domain.usecase.budget.GetBudgetsUseCase
import com.example.tracker.domain.usecase.category.GetCategoriesByTypeUseCase
import com.example.tracker.domain.usecase.category.GetCategoriesUseCase
import com.example.tracker.domain.usecase.loan.GetCasualLoansUseCase
import com.example.tracker.domain.usecase.loan.GetFormalLoansUseCase
import com.example.tracker.domain.usecase.person.GetPersonsUseCase
import com.example.tracker.domain.usecase.recurring.GetRecurringRulesUseCase
import com.example.tracker.domain.usecase.recurring.ProcessDueRulesUseCase
import com.example.tracker.domain.usecase.transaction.CreateTransactionUseCase
import com.example.tracker.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.tracker.domain.usecase.transaction.GetExpensesByCategoryUseCase
import com.example.tracker.domain.usecase.transaction.GetTransactionsByAccountUseCase
import com.example.tracker.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import com.example.tracker.domain.usecase.transaction.GetTransactionsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Transaction
    factory { GetTransactionsUseCase(get()) }
    factory { GetTransactionsByAccountUseCase(get()) }
    factory { GetTransactionsByDateRangeUseCase(get()) }
    factory { CreateTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { GetExpensesByCategoryUseCase(get()) }

    // Account
    factory { GetAccountsUseCase(get()) }
    factory { CreateAccountUseCase(get()) }
    factory { ArchiveAccountUseCase(get()) }

    // Category
    factory { GetCategoriesUseCase(get()) }
    factory { GetCategoriesByTypeUseCase(get()) }

    // Budget
    factory { GetBudgetsUseCase(get()) }
    factory { CreateBudgetUseCase(get()) }

    // Recurring
    factory { GetRecurringRulesUseCase(get()) }
    factory { ProcessDueRulesUseCase(get()) }

    // Loans
    factory { GetFormalLoansUseCase(get()) }
    factory { GetCasualLoansUseCase(get()) }

    // Person
    factory { GetPersonsUseCase(get()) }
}
