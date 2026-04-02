package com.hazard.koe.domain.usecase.account

import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.model.Account
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTotalAccountBalanceUseCaseTest {

    @Test
    fun withoutCreditAccounts_keepsPreviousBalanceSumBehavior() = runTest {
        val accounts = listOf(
            account(
                id = 1L,
                type = AccountType.DEBIT,
                currencyCode = "PEN",
                currentBalance = 150_00L
            ),
            account(
                id = 2L,
                type = AccountType.SAVINGS,
                currencyCode = "PEN",
                currentBalance = 50_00L
            )
        )

        val result = calculateTotalAccountBalanceInPen(accounts) { _, _ -> 1.0 }

        assertEquals(200_00L, result)
    }

    @Test
    fun withCreditAccounts_includesAvailableCredit() = runTest {
        val accounts = listOf(
            account(
                id = 1L,
                type = AccountType.DEBIT,
                currencyCode = "PEN",
                currentBalance = 100_00L
            ),
            account(
                id = 2L,
                type = AccountType.CREDIT,
                currencyCode = "PEN",
                currentBalance = 999_99L,
                creditLimit = 500_00L,
                creditUsed = 120_00L
            )
        )

        val result = calculateTotalAccountBalanceInPen(accounts) { _, _ -> 1.0 }

        assertEquals(480_00L, result)
    }

    @Test
    fun withOverLimitLegacyCreditData_clampsAvailableCreditToZero() = runTest {
        val accounts = listOf(
            account(
                id = 1L,
                type = AccountType.CREDIT,
                currencyCode = "PEN",
                creditLimit = 100_00L,
                creditUsed = 150_00L
            )
        )

        val result = calculateTotalAccountBalanceInPen(accounts) { _, _ -> 1.0 }

        assertEquals(0L, result)
    }

    @Test
    fun convertsCreditAvailableUsingConfiguredCurrencyRate() = runTest {
        val accounts = listOf(
            account(
                id = 1L,
                type = AccountType.CREDIT,
                currencyCode = "USD",
                creditLimit = 1_000_00L,
                creditUsed = 250_00L
            )
        )

        val result = calculateTotalAccountBalanceInPen(accounts) { from, to ->
            if (from == "USD" && to == "PEN") 3.5 else 1.0
        }

        assertEquals(2_625_00L, result)
    }

    private fun account(
        id: Long,
        type: AccountType,
        currencyCode: String,
        currentBalance: Long = 0L,
        creditLimit: Long? = null,
        creditUsed: Long? = null
    ): Account = Account(
        id = id,
        name = "Account$id",
        type = type,
        color = "#000000",
        currencyCode = currencyCode,
        initialBalance = 0L,
        currentBalance = currentBalance,
        creditLimit = creditLimit,
        creditUsed = creditUsed
    )
}
