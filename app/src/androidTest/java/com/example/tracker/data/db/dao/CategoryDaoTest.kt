package com.example.tracker.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tracker.data.db.Converters
import com.example.tracker.data.db.TrackerDatabase
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: TrackerDatabase
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TrackerDatabase::class.java
        )
            .addTypeConverter(Converters())
            .allowMainThreadQueries()
            .build()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetAll_returnsInsertedCategory() = runBlocking {
        val category = Category(
            name = "Food",
            emoji = "\uD83C\uDF74",
            color = "#FF5722",
            type = CategoryType.EXPENSE
        )
        val id = categoryDao.insert(category)
        val categories = categoryDao.getAll().first()
        assertEquals(1, categories.size)
        assertEquals("Food", categories[0].name)
        assertEquals(id, categories[0].id)
    }

    @Test
    fun getByType_returnsOnlyMatchingType() = runBlocking {
        val expense = Category(
            name = "Transport",
            emoji = "\uD83D\uDE8C",
            color = "#2196F3",
            type = CategoryType.EXPENSE
        )
        val income = Category(
            name = "Salary",
            emoji = "\uD83D\uDCBC",
            color = "#4CAF50",
            type = CategoryType.INCOME
        )
        categoryDao.insert(expense)
        categoryDao.insert(income)
        val expenseCategories = categoryDao.getByType(CategoryType.EXPENSE).first()
        assertEquals(1, expenseCategories.size)
        assertEquals("Transport", expenseCategories[0].name)
        val incomeCategories = categoryDao.getByType(CategoryType.INCOME).first()
        assertEquals(1, incomeCategories.size)
        assertEquals("Salary", incomeCategories[0].name)
    }

    @Test
    fun insertAll_insertsMultipleCategories() = runBlocking {
        val categories = listOf(
            Category(name = "Food", emoji = "\uD83C\uDF74", color = "#FF5722", type = CategoryType.EXPENSE),
            Category(name = "Transport", emoji = "\uD83D\uDE8C", color = "#2196F3", type = CategoryType.EXPENSE),
            Category(name = "Salary", emoji = "\uD83D\uDCBC", color = "#4CAF50", type = CategoryType.INCOME)
        )
        categoryDao.insertAll(categories)
        val all = categoryDao.getAll().first()
        assertEquals(3, all.size)
    }

    @Test
    fun archive_removesFromGetAll() = runBlocking {
        val category = Category(
            name = "Old Category",
            emoji = "\uD83D\uDCE6",
            color = "#9E9E9E",
            type = CategoryType.EXPENSE
        )
        val id = categoryDao.insert(category)
        categoryDao.archive(id)
        val all = categoryDao.getAll().first()
        assertEquals(0, all.size)
    }

    @Test
    fun getById_returnsCorrectCategory() = runBlocking {
        val category = Category(
            name = "Health",
            emoji = "\uD83C\uDFE5",
            color = "#F44336",
            type = CategoryType.EXPENSE
        )
        val id = categoryDao.insert(category)
        val result = categoryDao.getById(id).first()
        assertNotNull(result)
        assertEquals("Health", result!!.name)
    }
}
