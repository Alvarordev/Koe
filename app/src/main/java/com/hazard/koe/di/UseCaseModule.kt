package com.hazard.koe.di

import com.hazard.koe.domain.usecase.account.ArchiveAccountUseCase
import com.hazard.koe.domain.usecase.account.CreateAccountUseCase
import com.hazard.koe.domain.usecase.account.GetAccountByIdUseCase
import com.hazard.koe.domain.usecase.account.GetAccountsUseCase
import com.hazard.koe.domain.usecase.account.GetBalanceHistoryUseCase
import com.hazard.koe.domain.usecase.account.GetTotalAccountBalanceUseCase
import com.hazard.koe.domain.usecase.account.UpdateAccountUseCase
import com.hazard.koe.domain.usecase.budget.CreateBudgetUseCase
import com.hazard.koe.domain.usecase.budget.GetBudgetsUseCase
import com.hazard.koe.domain.usecase.category.ArchiveCategoryUseCase
import com.hazard.koe.domain.usecase.category.CreateCategoryUseCase
import com.hazard.koe.domain.usecase.category.GetCategoriesByTypeUseCase
import com.hazard.koe.domain.usecase.category.GetCategoriesUseCase
import com.hazard.koe.domain.usecase.category.GetCategoryByIdUseCase
import com.hazard.koe.domain.usecase.category.GetTransferCategoryUseCase
import com.hazard.koe.domain.usecase.category.UpdateCategoryUseCase
import com.hazard.koe.domain.usecase.loan.GetCasualLoansUseCase
import com.hazard.koe.domain.usecase.loan.GetFormalLoansUseCase
import com.hazard.koe.domain.usecase.home.ObserveHomeDateFilterPresetUseCase
import com.hazard.koe.domain.usecase.home.SaveHomeDateFilterPresetUseCase
import com.hazard.koe.domain.usecase.person.GetPersonsUseCase
import com.hazard.koe.domain.usecase.recurring.GetRecurringRulesUseCase
import com.hazard.koe.domain.usecase.recurring.GetSubscriptionRulesUseCase
import com.hazard.koe.domain.usecase.recurring.ProcessDueRulesUseCase
import com.hazard.koe.domain.usecase.transaction.CreateTransactionUseCase
import com.hazard.koe.domain.usecase.transaction.DeleteTransactionUseCase
import com.hazard.koe.domain.usecase.transaction.UpdateTransactionUseCase
import com.hazard.koe.domain.usecase.transaction.GetCategorySummaryUseCase
import com.hazard.koe.domain.usecase.transaction.GetAllCategorySummariesUseCase
import com.hazard.koe.domain.usecase.transaction.GetExpensesByCategoryUseCase
import com.hazard.koe.domain.usecase.transaction.GetTransactionsByAccountUseCase
import com.hazard.koe.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import com.hazard.koe.domain.usecase.subscription.DeleteSubscriptionUseCase
import com.hazard.koe.domain.usecase.subscription.GetActiveSubscriptionsUseCase
import com.hazard.koe.domain.usecase.subscription.GetAllSubscriptionsUseCase
import com.hazard.koe.domain.usecase.subscription.GetSubscriptionServicesUseCase
import com.hazard.koe.domain.usecase.subscription.ProcessSubscriptionBillingUseCase
import com.hazard.koe.domain.usecase.subscription.SaveSubscriptionUseCase
import com.hazard.koe.domain.usecase.transaction.GetTotalByTypeInPeriodUseCase
import com.hazard.koe.domain.usecase.transaction.GetTransactionsUseCase
import com.hazard.koe.domain.usecase.transaction.GetTransactionsWithCoordinatesByMonthUseCase
import com.hazard.koe.domain.usecase.database.ResetDatabaseUseCase
import com.hazard.koe.domain.usecase.loan.SaveCasualLoanUseCase
import com.hazard.koe.domain.usecase.loan.SaveFormalLoanUseCase
import com.hazard.koe.domain.usecase.person.SavePersonUseCase
import com.hazard.koe.domain.usecase.yape.ProcessYapeShareImageUseCase
import com.hazard.koe.domain.usecase.voice.InferTransactionFromVoiceUseCase
import com.hazard.koe.domain.usecase.voice.ObserveVoiceLocationSettingUseCase
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
    factory { GetTransactionsWithCoordinatesByMonthUseCase(get()) }

    // Account
    factory { GetAccountsUseCase(get()) }
    factory { GetAccountByIdUseCase(get()) }
    factory { CreateAccountUseCase(get()) }
    factory { ArchiveAccountUseCase(get()) }
    factory { UpdateAccountUseCase(get()) }
    factory { GetBalanceHistoryUseCase(get(), get()) }
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

    // Voice
    factory { InferTransactionFromVoiceUseCase(get()) }
    factory { ObserveVoiceLocationSettingUseCase(get()) }

    // Home
    factory { ObserveHomeDateFilterPresetUseCase(get()) }
    factory { SaveHomeDateFilterPresetUseCase(get()) }

    // Database
    factory { ResetDatabaseUseCase(get()) }
}
