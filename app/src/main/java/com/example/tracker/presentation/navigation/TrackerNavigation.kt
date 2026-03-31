package com.example.tracker.presentation.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.tracker.presentation.subscriptions.SubscriptionViewModel
import com.example.tracker.presentation.subscriptions.detail.SubscriptionDetailScreen
import com.example.tracker.presentation.subscriptions.picker.SubscriptionPickerScreen
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
    "add_transaction_amount",
    "subscription_picker",
    "subscription_detail/{iconResName}",
    "subscription_detail_edit/{subscriptionId}"
)

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackerScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val addViewModel: AddTransactionViewModel = koinViewModel()
    val addUiState by addViewModel.uiState.collectAsState()

    val activity = LocalActivity.current as? MainActivity
    val sharedImageUri by activity?.sharedImageUri?.collectAsState() ?: remember { mutableStateOf(null) }

    val transferViewModel: TransferViewModel = koinViewModel()
    val transferUiState by transferViewModel.uiState.collectAsState()

    val subscriptionViewModel: SubscriptionViewModel = koinViewModel()
    val pickerState by subscriptionViewModel.pickerState.collectAsState()
    val detailState by subscriptionViewModel.detailState.collectAsState()
    var showAddCategorySheet by remember { mutableStateOf(false) }
    var editCategoryId by remember { mutableStateOf<Long?>(null) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        fabMenuExpanded = false
    }

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

    SharedTransitionLayout {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                if (showBottomBar && currentTabRoute == TrackerTab.Home.route) {
                    FabMenu(
                        expanded = fabMenuExpanded,
                        onExpandedChange = { fabMenuExpanded = it },
                        navController = navController,
                        onTransactionPress = { navController.navigate("add_transaction_amount") }
                    )
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
                                val selected =
                                    currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
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

            Box(modifier = Modifier.fillMaxSize()) {



            NavHost(
                navController = navController,
                startDestination = TrackerTab.Home.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300,
                            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                        )
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            200,
                            easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
                        )
                    )
                },
            ) {
                composable(TrackerTab.Home.route) {
                    HomeScreen(
                        contentPadding = innerPadding,
                        onEditTransaction = { transactionId ->
                            addViewModel.loadTransactionById(transactionId)
                            navController.navigate("add_transaction_amount")
                        }
                    )
                }
                composable(TrackerTab.Accounts.route) {
                    AccountsScreen(
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
                        onAddCategoryClick = { showAddCategorySheet = true },
                        onAddSubscription = { navController.navigate("subscription_picker") },
                        onSubscriptionClick = { subId -> navController.navigate("subscription_detail_edit/$subId") }
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
                    val accountId =
                        backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: 0L
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
                composable("add_transaction_amount") {
                    AmountEntryScreen(
                        uiState = addUiState,
                        onAccountSelected = addViewModel::selectAccount,
                        onKeyPress = addViewModel::onKeyPress,
                        onDescriptionChange = addViewModel::onDescriptionChange,
                        onSubmit = addViewModel::submit,
                        onCategorySelected = addViewModel::selectCategory,
                        onDateSelected = addViewModel::onDateSelected,
                        onLocationToggle = addViewModel::onLocationToggle,
                        onNavigateBack = {
                            addViewModel.reset()
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    "subscription_picker",
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it / 2 }, // 👈 desde abajo
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                            )
                        ) + fadeIn()
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it / 2 }, // 👈 hacia abajo
                            animationSpec = tween(250)
                        ) + fadeOut()
                    },
                    popEnterTransition = {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(300)
                        ) + fadeIn()
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(250)
                        ) + fadeOut()
                    }
                ) {
                    SubscriptionPickerScreen(
                        uiState = pickerState,
                        onQueryChange = subscriptionViewModel::onQueryChange,
                        onServiceSelected = { sub ->
                            navController.navigate("subscription_detail/${sub.iconResName}")
                        },
                        onCreateCustom = { _ ->
                            navController.navigate("subscription_detail/custom")
                        },
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
                composable("subscription_detail/{iconResName}") { backStackEntry ->
                    val iconResName = backStackEntry.arguments?.getString("iconResName")
                    val resolvedIconResName = if (iconResName == "custom") null else iconResName
                    LaunchedEffect(iconResName) {
                        subscriptionViewModel.initDetail(resolvedIconResName)
                    }
                    LaunchedEffect(detailState.submitSuccess) {
                        if (detailState.submitSuccess) {
                            navController.popBackStack(
                                TrackerTab.Categories.route,
                                inclusive = false
                            )
                            subscriptionViewModel.resetDetail()
                        }
                    }
                    SubscriptionDetailScreen(
                        uiState = detailState,
                        onAmountChange = subscriptionViewModel::onAmountChange,
                        onBillingDayChange = subscriptionViewModel::onBillingDayChange,
                        onSelectAccount = subscriptionViewModel::onSelectAccount,
                        onCustomNameChange = subscriptionViewModel::onCustomNameChange,
                        onEmojiChange = subscriptionViewModel::onEmojiChange,
                        onSubmit = subscriptionViewModel::submit,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("subscription_detail_edit/{subscriptionId}") { backStackEntry ->
                    val subscriptionId =
                        backStackEntry.arguments?.getString("subscriptionId")?.toLongOrNull()
                            ?: return@composable
                    LaunchedEffect(subscriptionId) {
                        subscriptionViewModel.initDetailForEdit(subscriptionId)
                    }
                    LaunchedEffect(detailState.submitSuccess) {
                        if (detailState.submitSuccess) {
                            navController.popBackStack(
                                TrackerTab.Categories.route,
                                inclusive = false
                            )
                            subscriptionViewModel.resetDetail()
                        }
                    }
                    LaunchedEffect(detailState.deleteSuccess) {
                        if (detailState.deleteSuccess) {
                            navController.popBackStack(
                                TrackerTab.Categories.route,
                                inclusive = false
                            )
                            subscriptionViewModel.resetDetail()
                        }
                    }
                    SubscriptionDetailScreen(
                        uiState = detailState,
                        onAmountChange = subscriptionViewModel::onAmountChange,
                        onBillingDayChange = subscriptionViewModel::onBillingDayChange,
                        onSelectAccount = subscriptionViewModel::onSelectAccount,
                        onCustomNameChange = subscriptionViewModel::onCustomNameChange,
                        onEmojiChange = subscriptionViewModel::onEmojiChange,
                        onSubmit = subscriptionViewModel::submit,
                        onDelete = subscriptionViewModel::deleteCurrentSubscription,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            AnimatedVisibility(
                visible = fabMenuExpanded,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { fabMenuExpanded = false }
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
}
