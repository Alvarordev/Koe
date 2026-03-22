package com.example.tracker.di

import com.example.tracker.data.repository.AccountRepository
import com.example.tracker.data.repository.BudgetRepository
import com.example.tracker.data.repository.CasualLoanRepository
import com.example.tracker.data.repository.CategoryRepository
import com.example.tracker.data.repository.FormalLoanRepository
import com.example.tracker.data.repository.PersonRepository
import com.example.tracker.data.repository.RecurringRuleRepository
import com.example.tracker.data.repository.SubscriptionServiceRepository
import com.example.tracker.data.repository.TransactionRepository
import com.example.tracker.data.repository.impl.AccountRepositoryImpl
import com.example.tracker.data.repository.impl.BudgetRepositoryImpl
import com.example.tracker.data.repository.impl.CasualLoanRepositoryImpl
import com.example.tracker.data.repository.impl.CategoryRepositoryImpl
import com.example.tracker.data.repository.impl.FormalLoanRepositoryImpl
import com.example.tracker.data.repository.impl.PersonRepositoryImpl
import com.example.tracker.data.repository.impl.RecurringRuleRepositoryImpl
import com.example.tracker.data.repository.impl.SubscriptionServiceRepositoryImpl
import com.example.tracker.data.repository.impl.TransactionRepositoryImpl
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
}
