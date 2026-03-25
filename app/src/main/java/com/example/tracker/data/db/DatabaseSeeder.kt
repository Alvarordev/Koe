package com.example.tracker.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.SubscriptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseSeeder : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            seedCategories(db)
            seedSubscriptionServices(db)
        }
    }

    private fun seedCategories(db: SupportSQLiteDatabase) {
        val expenseCategories = listOf(
            Category(name = "Food & Dining", emoji = "\uD83C\uDF74", color = "#FF5722", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Groceries", emoji = "\uD83D\uDED2", color = "#4CAF50", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Transport", emoji = "\uD83D\uDE8C", color = "#2196F3", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Fuel", emoji = "\u26FD", color = "#607D8B", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Rent", emoji = "\uD83C\uDFE0", color = "#795548", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Utilities", emoji = "\uD83D\uDCA1", color = "#FFC107", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Internet & Phone", emoji = "\uD83D\uDCE1", color = "#00BCD4", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Health", emoji = "\uD83C\uDFE5", color = "#F44336", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Education", emoji = "\uD83C\uDF93", color = "#3F51B5", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Entertainment", emoji = "\uD83C\uDFBF", color = "#E91E63", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Shopping", emoji = "\uD83D\uDECD\uFE0F", color = "#9C27B0", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Personal Care", emoji = "\u2728", color = "#FF9800", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Gifts & Donations", emoji = "\uD83C\uDF81", color = "#8BC34A", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Travel", emoji = "\u2708\uFE0F", color = "#00BCD4", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Insurance", emoji = "\uD83D\uDEE1\uFE0F", color = "#455A64", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Pets", emoji = "\uD83D\uDC3E", color = "#8D6E63", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Subscriptions", emoji = "\uD83D\uDD01", color = "#673AB7", type = CategoryType.EXPENSE, isSystem = true),
            Category(name = "Other", emoji = "\uD83D\uDCE6", color = "#9E9E9E", type = CategoryType.EXPENSE, isSystem = true)
        )

        val incomeCategories = listOf(
            Category(name = "Salary", emoji = "\uD83D\uDCBC", color = "#4CAF50", type = CategoryType.INCOME, isSystem = true),
            Category(name = "Freelance", emoji = "\uD83D\uDCBB", color = "#2196F3", type = CategoryType.INCOME, isSystem = true),
            Category(name = "Investments", emoji = "\uD83D\uDCC8", color = "#FF9800", type = CategoryType.INCOME, isSystem = true),
            Category(name = "Gifts Received", emoji = "\uD83C\uDF81", color = "#E91E63", type = CategoryType.INCOME, isSystem = true),
            Category(name = "Refunds", emoji = "\u2194\uFE0F", color = "#607D8B", type = CategoryType.INCOME, isSystem = true),
            Category(name = "Rental Income", emoji = "\uD83D\uDD11", color = "#795548", type = CategoryType.INCOME, isSystem = true),
            Category(name = "Other Income", emoji = "\uD83D\uDCB0", color = "#9E9E9E", type = CategoryType.INCOME, isSystem = true)
        )

        val allCategories = expenseCategories + incomeCategories
        allCategories.forEachIndexed { index, category ->
            db.execSQL(
                """INSERT OR IGNORE INTO categories (name, emoji, color, type, isSystem, isArchived, sortOrder, createdAt)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
                arrayOf<Any?>(
                    category.name,
                    category.emoji,
                    category.color,
                    category.type.name,
                    if (category.isSystem) 1 else 0,
                    0,
                    index,
                    System.currentTimeMillis()
                )
            )
        }

        // Seed Transfer system category (archived, hidden from pickers)
        db.execSQL(
            """INSERT OR IGNORE INTO categories (name, emoji, color, type, isSystem, isArchived, sortOrder, createdAt)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
            arrayOf<Any?>(
                "Transfer",
                "\uD83D\uDD04",  // 🔄
                "#6B7280",
                "EXPENSE",
                1,
                1,
                999,
                System.currentTimeMillis()
            )
        )
    }

    private fun seedSubscriptionServices(db: SupportSQLiteDatabase) {
        val services = listOf(
            SubscriptionService(name = "Netflix", iconResName = "ic_sub_netflix", color = "#E50914", isSystem = true),
            SubscriptionService(name = "Spotify", iconResName = "ic_sub_spotify", color = "#1DB954", isSystem = true),
            SubscriptionService(name = "YouTube Premium", iconResName = "ic_sub_youtube", color = "#FF0000", isSystem = true),
            SubscriptionService(name = "Disney+", iconResName = "ic_sub_disney", color = "#113CCF", isSystem = true),
            SubscriptionService(name = "Amazon Prime", iconResName = "ic_sub_amazon", color = "#FF9900", isSystem = true),
            SubscriptionService(name = "HBO Max", iconResName = "ic_sub_hbo", color = "#B535F6", isSystem = true),
            SubscriptionService(name = "Apple Music", iconResName = "ic_sub_apple_music", color = "#FA2D48", isSystem = true),
            SubscriptionService(name = "Apple TV+", iconResName = "ic_sub_apple_tv", color = "#000000", isSystem = true),
            SubscriptionService(name = "Claude (Anthropic)", iconResName = "ic_sub_claude", color = "#D97757", isSystem = true),
            SubscriptionService(name = "ChatGPT Plus", iconResName = "ic_sub_chatgpt", color = "#10A37F", isSystem = true),
            SubscriptionService(name = "GitHub Copilot", iconResName = "ic_sub_github", color = "#24292E", isSystem = true),
            SubscriptionService(name = "Adobe Creative Cloud", iconResName = "ic_sub_adobe", color = "#FF0000", isSystem = true),
            SubscriptionService(name = "Microsoft 365", iconResName = "ic_sub_microsoft", color = "#0078D4", isSystem = true),
            SubscriptionService(name = "Google One", iconResName = "ic_sub_google", color = "#4285F4", isSystem = true),
            SubscriptionService(name = "iCloud+", iconResName = "ic_sub_icloud", color = "#3693F3", isSystem = true),
            SubscriptionService(name = "Dropbox", iconResName = "ic_sub_dropbox", color = "#0061FF", isSystem = true),
            SubscriptionService(name = "Notion", iconResName = "ic_sub_notion", color = "#000000", isSystem = true),
            SubscriptionService(name = "Crunchyroll", iconResName = "ic_sub_crunchyroll", color = "#F47521", isSystem = true),
            SubscriptionService(name = "Xbox Game Pass", iconResName = "ic_sub_xbox", color = "#107C10", isSystem = true),
            SubscriptionService(name = "PlayStation Plus", iconResName = "ic_sub_playstation", color = "#003791", isSystem = true)
        )

        services.forEach { service ->
            db.execSQL(
                """INSERT OR IGNORE INTO subscription_services (name, iconResName, color, isSystem, createdAt)
                   VALUES (?, ?, ?, ?, ?)""",
                arrayOf<Any?>(
                    service.name,
                    service.iconResName,
                    service.color,
                    if (service.isSystem) 1 else 0,
                    System.currentTimeMillis()
                )
            )
        }
    }
}
