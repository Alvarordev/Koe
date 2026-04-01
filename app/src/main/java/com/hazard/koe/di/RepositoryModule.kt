package com.hazard.koe.di

import com.hazard.koe.domain.repository.AccountRepository
import com.hazard.koe.domain.repository.BudgetRepository
import com.hazard.koe.domain.repository.CasualLoanRepository
import com.hazard.koe.domain.repository.CategoryRepository
import com.hazard.koe.domain.repository.FormalLoanRepository
import com.hazard.koe.domain.repository.PersonRepository
import com.hazard.koe.domain.repository.RecurringRuleRepository
import com.hazard.koe.domain.repository.SubscriptionServiceRepository
import com.hazard.koe.domain.repository.TransactionRepository
import com.hazard.koe.domain.repository.UserSubscriptionRepository
import com.hazard.koe.data.repository.impl.AccountRepositoryImpl
import com.hazard.koe.data.repository.impl.BudgetRepositoryImpl
import com.hazard.koe.data.repository.impl.CasualLoanRepositoryImpl
import com.hazard.koe.data.repository.impl.CategoryRepositoryImpl
import com.hazard.koe.data.repository.impl.FormalLoanRepositoryImpl
import com.hazard.koe.data.repository.impl.PersonRepositoryImpl
import com.hazard.koe.data.repository.impl.RecurringRuleRepositoryImpl
import com.hazard.koe.data.repository.impl.SubscriptionServiceRepositoryImpl
import com.hazard.koe.data.repository.impl.TransactionRepositoryImpl
import com.hazard.koe.data.repository.impl.UserSubscriptionRepositoryImpl
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
