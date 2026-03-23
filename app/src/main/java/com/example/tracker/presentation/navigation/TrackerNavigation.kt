package com.example.tracker.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tracker.presentation.accounts.accountdetail.AccountDetailScreen
import com.example.tracker.presentation.accounts.AccountsScreen
import com.example.tracker.presentation.accounts.addaccount.AddAccountScreen
import com.example.tracker.presentation.addtransaction.AddTransactionSheet
import com.example.tracker.presentation.addtransaction.AddTransactionViewModel
import com.example.tracker.presentation.categories.CategoriesScreen
import com.example.tracker.presentation.categories.CategoriesViewModel
import com.example.tracker.presentation.categories.addcategory.AddEditCategorySheet
import com.example.tracker.presentation.home.HomeScreen
import com.example.tracker.presentation.settings.SettingsScreen
import org.koin.androidx.compose.koinViewModel

enum class TrackerTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    Accounts("accounts", "Accounts", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    Categories("categories", "Categories", Icons.AutoMirrored.Filled.Label, Icons.AutoMirrored.Outlined.Label),
    Settings("settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
}

private val bottomBarSuppressedRoutes = setOf("add_account", "account_detail/{accountId}")

@Composable
fun TrackerScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val addViewModel: AddTransactionViewModel = koinViewModel()
    val addUiState by addViewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    var showAddCategorySheet by remember { mutableStateOf(false) }
    var editCategoryId by remember { mutableStateOf<Long?>(null) }

    val tabFabActions: Map<String, () -> Unit> = mapOf(
        TrackerTab.Home.route to { showSheet = true },
        TrackerTab.Accounts.route to { navController.navigate("add_account") },
        TrackerTab.Categories.route to { showAddCategorySheet = true }
    )

    val showBottomBar = currentRoute !in bottomBarSuppressedRoutes
    val currentTabRoute = TrackerTab.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }?.route ?: TrackerTab.Home.route

    LaunchedEffect(addUiState.submitSuccess) {
        if (addUiState.submitSuccess) {
            showSheet = false
            addViewModel.reset()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (showBottomBar && tabFabActions.containsKey(currentTabRoute)) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        tabFabActions[currentTabRoute]?.invoke()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        TrackerTab.entries.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(text = tab.label, style = MaterialTheme.typography.labelMedium)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TrackerTab.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(TrackerTab.Home.route) {
                HomeScreen(contentPadding = innerPadding)
            }
            composable(TrackerTab.Accounts.route) {
                AccountsScreen(
                    contentPadding = innerPadding,
                    onAccountClick = { accountId ->
                        navController.navigate("account_detail/$accountId")
                    }
                )
            }
            composable(TrackerTab.Categories.route) {
                val categoriesViewModel: CategoriesViewModel = koinViewModel()
                val categoriesUiState by categoriesViewModel.uiState.collectAsState()
                CategoriesScreen(
                    contentPadding = innerPadding,
                    uiState = categoriesUiState,
                    onCategoryClick = { categoryId ->
                        editCategoryId = categoryId
                    }
                )
            }
            composable(TrackerTab.Settings.route) {
                SettingsScreen(contentPadding = innerPadding)
            }
            composable("add_account") {
                AddAccountScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("account_detail/{accountId}") { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: 0L
                AccountDetailScreen(
                    accountId = accountId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showSheet) {
        AddTransactionSheet(
            uiState = addUiState,
            onCategorySelected = addViewModel::selectCategory,
            onClearCategory = addViewModel::clearCategory,
            onAccountSelected = addViewModel::selectAccount,
            onKeyPress = addViewModel::onKeyPress,
            onDescriptionChange = addViewModel::onDescriptionChange,
            onSubmit = addViewModel::submit,
            onLocationToggle = addViewModel::onLocationToggle,
            onDismiss = {
                showSheet = false
                addViewModel.reset()
            }
        )
    }

    if (showAddCategorySheet || editCategoryId != null) {
        AddEditCategorySheet(
            categoryId = editCategoryId,
            onDismiss = {
                showAddCategorySheet = false
                editCategoryId = null
            }
        )
    }
}
