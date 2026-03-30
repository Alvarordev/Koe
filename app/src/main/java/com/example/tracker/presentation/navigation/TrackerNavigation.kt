package com.example.tracker.presentation.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tracker.R
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tracker.MainActivity
import com.example.tracker.presentation.accounts.AccountsScreen
import com.example.tracker.presentation.accounts.accountdetail.AccountDetailScreen
import com.example.tracker.presentation.accounts.addaccount.AddAccountScreen
import com.example.tracker.presentation.addtransaction.AddTransactionViewModel
import com.example.tracker.presentation.addtransaction.AmountEntryScreen
import com.example.tracker.presentation.addtransaction.CategoryPickerScreen
import com.example.tracker.presentation.categories.CategoriesScreen
import com.example.tracker.presentation.categories.CategoriesViewModel
import com.example.tracker.presentation.categories.addcategory.AddEditCategorySheet
import com.example.tracker.presentation.components.FabMenu
import com.example.tracker.presentation.home.HomeScreen
import com.example.tracker.presentation.settings.SettingsScreen
import com.example.tracker.presentation.settings.yapesetup.screens.YapeSetupAccountScreen
import com.example.tracker.presentation.settings.yapesetup.screens.YapeSetupIntroScreen
import com.example.tracker.presentation.settings.yapesetup.screens.YapeSetupPermissionScreen
import com.example.tracker.presentation.settings.yapesetup.screens.YapeStatusScreen
import com.example.tracker.presentation.transfer.TransferAmountScreen
import com.example.tracker.presentation.transfer.TransferSourceScreen
import com.example.tracker.presentation.transfer.TransferViewModel
import org.koin.androidx.compose.koinViewModel

enum class TrackerTab(
    val route: String,
    val label: String,
    val selectedIcon: Int?,
    val unselectedIcon: Int?
) {
    Home("home", "Home", R.drawable.home_solid, R.drawable.home_simple),
    Accounts("accounts", "Accounts", R.drawable.wallet_solid, R.drawable.wallet),
    Categories("categories", "Categories", R.drawable.view_grid_solid, R.drawable.view_grid),
    Settings("settings", "Ajustes", null, null)
}

private val bottomBarSuppressedRoutes = setOf(
    "add_account",
    "account_detail/{accountId}",
    "yape_setup_intro",
    "yape_setup_account",
    "yape_setup_permission",
    "yape_status",
    "transfer_source",
    "transfer_amount",
    "add_transaction_category",
    "add_transaction_amount"
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackerScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val addViewModel: AddTransactionViewModel = koinViewModel()
    val addUiState by addViewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    val activity = LocalActivity.current as? MainActivity
    val sharedImageUri by activity?.sharedImageUri?.collectAsState() ?: remember { mutableStateOf(null) }

    val transferViewModel: TransferViewModel = koinViewModel()
    val transferUiState by transferViewModel.uiState.collectAsState()
    var showAddCategorySheet by remember { mutableStateOf(false) }
    var editCategoryId by remember { mutableStateOf<Long?>(null) }

    val tabFabActions: Map<String, () -> Unit> = mapOf(
        TrackerTab.Accounts.route to { navController.navigate("add_account") },
        TrackerTab.Categories.route to { showAddCategorySheet = true }
    )

    val showBottomBar = currentRoute !in bottomBarSuppressedRoutes
    val currentTabRoute = TrackerTab.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }?.route ?: TrackerTab.Home.route

    LaunchedEffect(addUiState.submitSuccess) {
        if (addUiState.submitSuccess) {
            navController.popBackStack(TrackerTab.Home.route, inclusive = false)
            addViewModel.reset()
        }
    }

    LaunchedEffect(sharedImageUri) {
        val uri = sharedImageUri
        if (uri != null) {
            addViewModel.processYapeImage(uri)
            navController.navigate("add_transaction_amount")
            activity?.clearSharedImage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (showBottomBar) {
                if (currentTabRoute == TrackerTab.Home.route) {
                    FabMenu(
                        navController = navController,
                        onTransactionPress = { navController.navigate("add_transaction_category") }
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
                            containerColor = MaterialTheme.colorScheme.background,
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
                                        if (tab.selectedIcon != null && tab.unselectedIcon != null) {
                                            Icon(
                                                painter = painterResource(if (selected) tab.selectedIcon else tab.unselectedIcon),
                                                contentDescription = tab.label,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (selected) Icons.Default.Settings else Icons.Outlined.Settings,
                                                contentDescription = tab.label,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }


                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = Color.Transparent
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
                        },
                        onAddAccountClick = { navController.navigate("add_account") }
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
                        },
                        onAddCategoryClick = { showAddCategorySheet = true }
                    )
                }
                composable(TrackerTab.Settings.route) {
                    SettingsScreen(
                        contentPadding = innerPadding,
                        onNavigateToYapeSetup = { navController.navigate("yape_setup_intro") },
                        onNavigateToYapeStatus = { navController.navigate("yape_status") },
                        onDatabaseReset = {
                            navController.navigate(TrackerTab.Home.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
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
                composable("yape_setup_intro") {
                    YapeSetupIntroScreen(
                        onNavigateToAccount = { navController.navigate("yape_setup_account") }
                    )
                }
                composable("yape_setup_account") {
                    YapeSetupAccountScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPermission = { navController.navigate("yape_setup_permission") }
                    )
                }
                composable("yape_setup_permission") {
                    YapeSetupPermissionScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSetupComplete = {
                            navController.navigate("yape_status") {
                                popUpTo("yape_setup_intro") { inclusive = true }
                            }
                        }
                    )
                }
                composable("yape_status") {
                    YapeStatusScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("transfer_source") {
                    TransferSourceScreen(
                        uiState = transferUiState,
                        onAccountSelected = { account ->
                            transferViewModel.selectSourceAccount(account)
                            navController.navigate("transfer_amount")
                        },
                        onNavigateBack = { navController.popBackStack() },
                        contentPadding = innerPadding
                    )
                }
                composable("transfer_amount") {
                    LaunchedEffect(transferUiState.submitSuccess) {
                        if (transferUiState.submitSuccess) {
                            navController.popBackStack(TrackerTab.Home.route, inclusive = false)
                            transferViewModel.reset()
                        }
                    }
                    TransferAmountScreen(
                        uiState = transferUiState,
                        onDestinationSelected = transferViewModel::selectDestinationAccount,
                        onKeyPress = transferViewModel::onKeyPress,
                        onDescriptionChange = transferViewModel::onDescriptionChange,
                        onSubmit = transferViewModel::submit,
                        onNavigateBack = {
                            transferViewModel.goBack()
                            navController.popBackStack()
                        },
                        contentPadding = innerPadding
                    )
                }
                composable("add_transaction_category") {
                    CategoryPickerScreen(
                        uiState = addUiState,
                        onCategorySelected = { category ->
                            addViewModel.selectCategory(category)
                            navController.navigate("add_transaction_amount")
                        },
                        onNavigateBack = {
                            addViewModel.reset()
                            navController.popBackStack()
                        },

                    )
                }
                composable("add_transaction_amount") {
                    AmountEntryScreen(
                        uiState = addUiState,
                        onAccountSelected = addViewModel::selectAccount,
                        onKeyPress = addViewModel::onKeyPress,
                        onDescriptionChange = addViewModel::onDescriptionChange,
                        onSubmit = addViewModel::submit,
                        onClearCategory = {
                            addViewModel.clearCategory()
                            navController.popBackStack()
                        },
                        onDateSelected = addViewModel::onDateSelected,
                        onLocationToggle = addViewModel::onLocationToggle,
                        onNavigateBack = {
                            addViewModel.clearCategory()
                            navController.popBackStack()
                        }
                    )
                }
            }
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
