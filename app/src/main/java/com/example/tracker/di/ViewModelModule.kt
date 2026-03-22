package com.example.tracker.di

import com.example.tracker.presentation.accounts.accountdetail.AccountDetailViewModel
import com.example.tracker.presentation.accounts.AccountsViewModel
import com.example.tracker.presentation.accounts.addaccount.AddAccountViewModel
import com.example.tracker.presentation.addtransaction.AddTransactionViewModel
import com.example.tracker.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { AddTransactionViewModel(get(), get(), get()) }
    viewModel { AccountsViewModel(get()) }
    viewModel { AddAccountViewModel(get()) }
    viewModel { (accountId: Long) -> AccountDetailViewModel(accountId, get(), get()) }
}
