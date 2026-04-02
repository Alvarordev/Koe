package com.hazard.koe.di

import com.hazard.koe.presentation.accounts.accountdetail.AccountDetailViewModel
import com.hazard.koe.presentation.accounts.AccountsViewModel
import com.hazard.koe.presentation.accounts.addaccount.AddAccountViewModel
import com.hazard.koe.presentation.addtransaction.AddTransactionViewModel
import com.hazard.koe.presentation.categories.CategoriesViewModel
import com.hazard.koe.presentation.categories.addcategory.AddEditCategoryViewModel
import com.hazard.koe.presentation.home.HomeViewModel
import com.hazard.koe.presentation.loans.AddLoanViewModel
import com.hazard.koe.presentation.loans.detail.CasualLoanDetailViewModel
import com.hazard.koe.presentation.loans.detail.FormalLoanDetailViewModel
import com.hazard.koe.presentation.settings.SettingsViewModel
import com.hazard.koe.presentation.settings.yapesetup.YapeSetupViewModel
import com.hazard.koe.presentation.subscriptions.SubscriptionViewModel
import com.hazard.koe.presentation.transfer.TransferViewModel
import com.hazard.koe.presentation.transactionmap.TransactionMapViewModel
import com.hazard.koe.presentation.voice.voicetransaction.VoiceTransactionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AddTransactionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AccountsViewModel(get(), get(), get(), get(), get()) }
    viewModel { AddAccountViewModel(get(), get(), get()) }
    viewModel { (accountId: Long) -> AccountDetailViewModel(accountId, get(), get(), get(), get()) }
    viewModel { CategoriesViewModel(get(), get(), get(), get()) }
    viewModel { (categoryId: Long?) -> AddEditCategoryViewModel(categoryId, get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), androidContext()) }
    viewModel { YapeSetupViewModel(get(), get(), get()) }
    viewModel { TransferViewModel(get(), get(), get(), get()) }
    viewModel { SubscriptionViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { AddLoanViewModel(get(), get(), get(), get(), get()) }
    viewModel { (personId: Long) -> CasualLoanDetailViewModel(personId, get(), get()) }
    viewModel { (loanId: Long) -> FormalLoanDetailViewModel(loanId, get()) }
    viewModel { VoiceTransactionViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { TransactionMapViewModel(get()) }
}
