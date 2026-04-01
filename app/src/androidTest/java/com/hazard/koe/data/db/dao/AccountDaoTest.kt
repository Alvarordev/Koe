package com.hazard.koe.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hazard.koe.data.db.Converters
import com.hazard.koe.data.db.TrackerDatabase
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.model.Account
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountDaoTest {

    private lateinit var database: TrackerDatabase
    private lateinit var accountDao: AccountDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TrackerDatabase::class.java
        )
            .addTypeConverter(Converters())
            .allowMainThreadQueries()
            .build()
        accountDao = database.accountDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetAll_returnsInsertedAccount() = runBlocking {
        val account = Account(
            name = "Checking",
            type = AccountType.DEBIT,
            color = "#2196F3",
            currencyCode = "USD",
            initialBalance = 100000L,
            currentBalance = 100000L
        )
        val id = accountDao.insert(account)
        val accounts = accountDao.getAll().first()
        assertEquals(1, accounts.size)
        assertEquals("Checking", accounts[0].name)
        assertEquals(id, accounts[0].id)
    }

    @Test
    fun archive_removesAccountFromGetAll() = runBlocking {
        val account = Account(
            name = "Old Account",
            type = AccountType.SAVINGS,
            color = "#4CAF50",
            currencyCode = "USD",
            initialBalance = 0L,
            currentBalance = 0L
        )
        val id = accountDao.insert(account)
        accountDao.archive(id)
        val accounts = accountDao.getAll().first()
        assertTrue(accounts.none { it.id == id })
    }

    @Test
    fun updateBalance_updatesCurrentBalance() = runBlocking {
        val account = Account(
            name = "Wallet",
            type = AccountType.CASH,
            color = "#FF9800",
            currencyCode = "USD",
            initialBalance = 50000L,
            currentBalance = 50000L
        )
        val id = accountDao.insert(account)
        accountDao.updateBalance(id, 75000L)
        val updated = accountDao.getById(id).first()
        assertNotNull(updated)
        assertEquals(75000L, updated!!.currentBalance)
    }

    @Test
    fun getById_returnsNullForNonExistentId() = runBlocking {
        val result = accountDao.getById(9999L).first()
        assertNull(result)
    }

    @Test
    fun getByType_filtersCorrectly() = runBlocking {
        val debitAccount = Account(
            name = "Debit Card",
            type = AccountType.DEBIT,
            color = "#2196F3",
            currencyCode = "USD",
            initialBalance = 0L,
            currentBalance = 0L
        )
        val creditAccount = Account(
            name = "Credit Card",
            type = AccountType.CREDIT,
            color = "#F44336",
            currencyCode = "USD",
            initialBalance = 0L,
            currentBalance = 0L,
            creditLimit = 500000L
        )
        accountDao.insert(debitAccount)
        accountDao.insert(creditAccount)
        val debitAccounts = accountDao.getByType(AccountType.DEBIT).first()
        assertEquals(1, debitAccounts.size)
        assertEquals("Debit Card", debitAccounts[0].name)
    }
}
