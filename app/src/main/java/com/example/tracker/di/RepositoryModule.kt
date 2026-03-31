package com.example.tracker.di

import com.example.tracker.domain.repository.AccountRepository
import com.example.tracker.domain.repository.BudgetRepository
import com.example.tracker.domain.repository.CasualLoanRepository
import com.example.tracker.domain.repository.CategoryRepository
import com.example.tracker.domain.repository.FormalLoanRepository
import com.example.tracker.domain.repository.PersonRepository
import com.example.tracker.domain.repository.RecurringRuleRepository
import com.example.tracker.domain.repository.SubscriptionServiceRepository
import com.example.tracker.domain.repository.TransactionRepository
import com.example.tracker.domain.repository.UserSubscriptionRepository
import com.example.tracker.data.repository.impl.AccountRepositoryImpl
import com.example.tracker.data.repository.impl.BudgetRepositoryImpl
import com.example.tracker.data.repository.impl.CasualLoanRepositoryImpl
import com.example.tracker.data.repository.impl.CategoryRepositoryImpl
import com.example.tracker.data.repository.impl.FormalLoanRepositoryImpl
import com.example.tracker.data.repository.impl.PersonRepositoryImpl
import com.example.tracker.data.repository.impl.RecurringRuleRepositoryImpl
import com.example.tracker.data.repository.impl.SubscriptionServiceRepositoryImpl
import com.example.tracker.data.repository.impl.TransactionRepositoryImpl
import com.example.tracker.data.repository.impl.UserSubscriptionRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
    single<RecurringRuleRepository> { RecurringRuleRepositoryImpl(get(), get(), get()) }
    single<SubscriptionServiceRepository> { SubscriptionServiceRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<CasualLoanRepository> { CasualLoanRepositoryImpl(get(), get(), get()) }
    single<PersonRepository> { PersonRepositoryImpl(get()) }
    single<FormalLoanRepository> { FormalLoanRepositoryImpl(get(), get(), get()) }
    single<UserSubscriptionRepository> { UserSubscriptionRepositoryImpl(get()) }
}
