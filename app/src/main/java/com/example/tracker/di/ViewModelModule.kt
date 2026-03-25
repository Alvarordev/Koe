package com.example.tracker.di

import com.example.tracker.presentation.accounts.accountdetail.AccountDetailViewModel
import com.example.tracker.presentation.accounts.AccountsViewModel
import com.example.tracker.presentation.accounts.addaccount.AddAccountViewModel
import com.example.tracker.presentation.addtransaction.AddTransactionViewModel
import com.example.tracker.presentation.categories.CategoriesViewModel
import com.example.tracker.presentation.categories.addcategory.AddEditCategoryViewModel
import com.example.tracker.presentation.home.HomeViewModel
import com.example.tracker.presentation.settings.SettingsViewModel
import com.example.tracker.presentation.settings.yapesetup.YapeSetupViewModel
import com.example.tracker.presentation.transfer.TransferViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { AddTransactionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AccountsViewModel(get()) }
    viewModel { AddAccountViewModel(get()) }
    viewModel { (accountId: Long) -> AccountDetailViewModel(accountId, get(), get()) }
    viewModel { CategoriesViewModel(get(), get(), get()) }
    viewModel { (categoryId: Long?) -> AddEditCategoryViewModel(categoryId, get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), androidContext()) }
    viewModel { YapeSetupViewModel(get(), get(), get()) }
    viewModel { TransferViewModel(get(), get(), get(), get()) }
}
