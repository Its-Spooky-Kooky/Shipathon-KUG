package com.eta.assistant.ui.navigation

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eta.assistant.EtaApplication
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.ui.screens.customers.CustomerDetailScreen
import com.eta.assistant.ui.screens.customers.CustomerListScreen
import com.eta.assistant.ui.screens.customers.CustomersViewModel
import com.eta.assistant.ui.screens.customers.CustomersViewModelFactory
import com.eta.assistant.ui.screens.dashboard.DashboardScreen
import com.eta.assistant.ui.screens.dashboard.DashboardViewModel
import com.eta.assistant.ui.screens.dashboard.DashboardViewModelFactory
import com.eta.assistant.ui.screens.inventory.AddProductScreen
import com.eta.assistant.ui.screens.inventory.InventoryScreen
import com.eta.assistant.ui.screens.inventory.InventoryViewModel
import com.eta.assistant.ui.screens.inventory.InventoryViewModelFactory
import com.eta.assistant.ui.screens.lowstock.LowStockScreen
import com.eta.assistant.ui.screens.onboarding.OnboardingScreen
import com.eta.assistant.ui.screens.pricing.PricingScreen
import com.eta.assistant.ui.screens.reports.ReportsScreen
import com.eta.assistant.ui.screens.reports.ReportsViewModel
import com.eta.assistant.ui.screens.reports.ReportsViewModelFactory
import com.eta.assistant.ui.screens.sales.RecordSaleScreen
import com.eta.assistant.ui.screens.sales.SaleConfirmationScreen
import com.eta.assistant.ui.screens.sales.SalesViewModel
import com.eta.assistant.ui.screens.sales.SalesViewModelFactory
import com.eta.assistant.ui.screens.settings.SettingsScreen
import com.eta.assistant.ui.screens.splash.SplashScreen
import com.eta.assistant.ui.screens.sync.SyncScreen
import com.eta.assistant.ui.screens.voice.VoiceSaleScreen
import com.eta.assistant.ui.screens.voice.VoiceViewModel
import com.eta.assistant.ui.screens.voice.VoiceViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun EtaNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as EtaApplication
    val repository = app.repository
    val preferencesRepository = app.preferencesRepository
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Inventory,
        BottomNavItem.Customers,
        BottomNavItem.Reports,
        BottomNavItem.More
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    // Shared ViewModels
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository, preferencesRepository)
    )
    val inventoryViewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModelFactory(repository, preferencesRepository)
    )
    val salesViewModel: SalesViewModel = viewModel(
        factory = SalesViewModelFactory(repository, preferencesRepository)
    )
    val customersViewModel: CustomersViewModel = viewModel(
        factory = CustomersViewModelFactory(repository, preferencesRepository)
    )
    val reportsViewModel: ReportsViewModel = viewModel(
        factory = ReportsViewModelFactory(repository, preferencesRepository)
    )
    val voiceViewModel: VoiceViewModel = viewModel(
        factory = VoiceViewModelFactory(context.applicationContext as Application, repository, preferencesRepository)
    )

    val prefs by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)
    val lang = prefs?.languageCode ?: "en"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        val itemTitle = when (item) {
                            BottomNavItem.Home -> com.eta.assistant.core.util.LocaleStrings.get("home", lang)
                            BottomNavItem.Inventory -> com.eta.assistant.core.util.LocaleStrings.get("inventory", lang)
                            BottomNavItem.Customers -> com.eta.assistant.core.util.LocaleStrings.get("customers", lang)
                            BottomNavItem.Reports -> com.eta.assistant.core.util.LocaleStrings.get("reports", lang)
                            BottomNavItem.More -> com.eta.assistant.core.util.LocaleStrings.get("more", lang)
                        }
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = itemTitle) },
                            label = { Text(itemTitle) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OrangePrimary,
                                selectedTextColor = OrangePrimary,
                                indicatorColor = OrangePrimary.copy(alpha = 0.15f)
                            ),
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onGetStarted = {
                            navController.navigate(Screen.Onboarding.route)
                        },
                        onSignIn = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onComplete = { businessName, ownerName, category, currency, threshold ->
                            scope.launch {
                                preferencesRepository.updateOnboarding(businessName, ownerName, category, currency, threshold)
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        },
                        onSkip = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onRecordSaleVoice = { navController.navigate(Screen.VoiceSale.route) },
                        onRecordSaleManual = { navController.navigate(Screen.RecordSale.route) },
                        onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                        onViewDues = { navController.navigate(Screen.Customers.route) },
                        onReports = { navController.navigate(Screen.Reports.route) },
                        onLowStockClick = { navController.navigate(Screen.LowStock.route) },
                        onSyncClick = { navController.navigate(Screen.Sync.route) }
                    )
                }

                composable(Screen.Inventory.route) {
                    InventoryScreen(
                        viewModel = inventoryViewModel,
                        onAddProductClick = { navController.navigate(Screen.AddProduct.route) }
                    )
                }

                composable(Screen.AddProduct.route) {
                    AddProductScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSaveProduct = { name, category, unit, stock, price ->
                            inventoryViewModel.addProduct(
                                name = name,
                                category = category,
                                unit = unit,
                                stock = stock,
                                sellingPrice = price,
                                onSuccess = { navController.popBackStack() }
                            )
                        }
                    )
                }

                composable(Screen.Customers.route) {
                    CustomerListScreen(
                        viewModel = customersViewModel,
                        onCustomerClick = { customerId ->
                            navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                        }
                    )
                }

                composable(
                    route = Screen.CustomerDetail.route,
                    arguments = listOf(navArgument("customerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                    CustomerDetailScreen(
                        customerId = customerId,
                        viewModel = customersViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Reports.route) {
                    ReportsScreen(
                        viewModel = reportsViewModel,
                        onViewDuesClick = { navController.navigate(Screen.Customers.route) }
                    )
                }

                composable(Screen.RecordSale.route) {
                    RecordSaleScreen(
                        viewModel = salesViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onProceedToConfirm = {
                            navController.navigate("sale_confirmation/0")
                        }
                    )
                }

                composable("sale_confirmation/{saleId}") {
                    SaleConfirmationScreen(
                        viewModel = salesViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onSaleCompleted = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.VoiceSale.route) {
                    VoiceSaleScreen(
                        viewModel = voiceViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCustomers = { navController.navigate(Screen.Customers.route) },
                        onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                        onNavigateToManualSale = { navController.navigate(Screen.RecordSale.route) },
                        onSaleRecorded = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.LowStock.route) {
                    LowStockScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() },
                        onViewInventory = {
                            navController.navigate(Screen.Inventory.route) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    )
                }

                composable(Screen.Sync.route) {
                    SyncScreen(
                        preferencesRepository = preferencesRepository,
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        preferencesRepository = preferencesRepository,
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSync = { navController.navigate(Screen.Sync.route) },
                        onNavigateToPricing = { navController.navigate(Screen.Pricing.route) }
                    )
                }

                composable(Screen.Pricing.route) {
                    PricingScreen(
                        preferencesRepository = preferencesRepository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

