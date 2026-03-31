package com.example.tracker.di

import com.example.tracker.domain.usecase.account.ArchiveAccountUseCase
import com.example.tracker.domain.usecase.account.CreateAccountUseCase
import com.example.tracker.domain.usecase.account.GetAccountByIdUseCase
import com.example.tracker.domain.usecase.account.GetAccountsUseCase
import com.example.tracker.domain.usecase.account.GetTotalAccountBalanceUseCase
import com.example.tracker.domain.usecase.budget.CreateBudgetUseCase
import com.example.tracker.domain.usecase.budget.GetBudgetsUseCase
import com.example.tracker.domain.usecase.category.ArchiveCategoryUseCase
import com.example.tracker.domain.usecase.category.CreateCategoryUseCase
import com.example.tracker.domain.usecase.category.GetCategoriesByTypeUseCase
import com.example.tracker.domain.usecase.category.GetCategoriesUseCase
import com.example.tracker.domain.usecase.category.GetCategoryByIdUseCase
import com.example.tracker.domain.usecase.category.GetTransferCategoryUseCase
import com.example.tracker.domain.usecase.category.UpdateCategoryUseCase
import com.example.tracker.domain.usecase.loan.GetCasualLoansUseCase
import com.example.tracker.domain.usecase.loan.GetFormalLoansUseCase
import com.example.tracker.domain.usecase.person.GetPersonsUseCase
import com.example.tracker.domain.usecase.recurring.GetRecurringRulesUseCase
import com.example.tracker.domain.usecase.recurring.GetSubscriptionRulesUseCase
import com.example.tracker.domain.usecase.recurring.ProcessDueRulesUseCase
import com.example.tracker.domain.usecase.transaction.CreateTransactionUseCase
import com.example.tracker.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.tracker.domain.usecase.transaction.UpdateTransactionUseCase
import com.example.tracker.domain.usecase.transaction.GetCategorySummaryUseCase
import com.example.tracker.domain.usecase.transaction.GetAllCategorySummariesUseCase
import com.example.tracker.domain.usecase.transaction.GetExpensesByCategoryUseCase
import com.example.tracker.domain.usecase.transaction.GetTransactionsByAccountUseCase
import com.example.tracker.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import com.example.tracker.domain.usecase.subscription.DeleteSubscriptionUseCase
import com.example.tracker.domain.usecase.subscription.GetActiveSubscriptionsUseCase
import com.example.tracker.domain.usecase.subscription.GetAllSubscriptionsUseCase
import com.example.tracker.domain.usecase.subscription.GetSubscriptionServicesUseCase
import com.example.tracker.domain.usecase.subscription.ProcessSubscriptionBillingUseCase
import com.example.tracker.domain.usecase.subscription.SaveSubscriptionUseCase
import com.example.tracker.domain.usecase.transaction.GetTotalByTypeInPeriodUseCase
import com.example.tracker.domain.usecase.transaction.GetTransactionsUseCase
import com.example.tracker.domain.usecase.database.ResetDatabaseUseCase
import com.example.tracker.domain.usecase.loan.SaveCasualLoanUseCase
import com.example.tracker.domain.usecase.loan.SaveFormalLoanUseCase
import com.example.tracker.domain.usecase.person.SavePersonUseCase
import com.example.tracker.domain.usecase.yape.ProcessYapeShareImageUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Transaction
    factory { GetTransactionsUseCase(get()) }
    factory { GetTransactionsByAccountUseCase(get()) }
    factory { GetTransactionsByDateRangeUseCase(get()) }
    factory { CreateTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { GetCategorySummaryUseCase(get()) }
    factory { GetExpensesByCategoryUseCase(get()) }
    factory { GetAllCategorySummariesUseCase(get()) }
    factory { GetTotalByTypeInPeriodUseCase(get()) }

    // Account
    factory { GetAccountsUseCase(get()) }
    factory { GetAccountByIdUseCase(get()) }
    factory { CreateAccountUseCase(get()) }
    factory { ArchiveAccountUseCase(get()) }
    factory { GetTotalAccountBalanceUseCase(get(), get()) }

    // Category
    factory { GetCategoriesUseCase(get()) }
    factory { GetCategoriesByTypeUseCase(get()) }
    factory { CreateCategoryUseCase(get()) }
    factory { UpdateCategoryUseCase(get()) }
    factory { GetCategoryByIdUseCase(get()) }
    factory { ArchiveCategoryUseCase(get()) }
    factory { GetTransferCategoryUseCase(get()) }

    // Subscription
    factory { GetSubscriptionServicesUseCase(get()) }
    factory { GetAllSubscriptionsUseCase(get()) }
    factory { GetActiveSubscriptionsUseCase(get()) }
    factory { SaveSubscriptionUseCase(get()) }
    factory { DeleteSubscriptionUseCase(get()) }
    factory { ProcessSubscriptionBillingUseCase(get(), get(), get()) }

    // Budget
    factory { GetBudgetsUseCase(get()) }
    factory { CreateBudgetUseCase(get()) }

    // Recurring
    factory { GetRecurringRulesUseCase(get()) }
    factory { GetSubscriptionRulesUseCase(get()) }
    factory { ProcessDueRulesUseCase(get()) }

    // Loans
    factory { GetFormalLoansUseCase(get()) }
    factory { GetCasualLoansUseCase(get()) }
    factory { SaveCasualLoanUseCase(get()) }
    factory { SaveFormalLoanUseCase(get()) }

    // Person
    factory { GetPersonsUseCase(get()) }
    factory { SavePersonUseCase(get()) }

    // Yape
    factory { ProcessYapeShareImageUseCase(get(), get()) }

    // Database
    factory { ResetDatabaseUseCase(get()) }
}
