package com.eta.assistant.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Inventory : Screen("inventory")
    object AddProduct : Screen("add_product")
    object Customers : Screen("customers")
    object CustomerDetail : Screen("customer_detail/{customerId}") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }
    object Reports : Screen("reports")
    object RecordSale : Screen("record_sale")
    object SaleConfirmation : Screen("sale_confirmation/{saleId}") {
        fun createRoute(saleId: Long) = "sale_confirmation/$saleId"
    }
    object VoiceSale : Screen("voice_sale")
    object LowStock : Screen("low_stock")
    object Sync : Screen("sync")
    object Settings : Screen("settings")
    object Pricing : Screen("pricing")
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Dashboard.route, "Home", Icons.Default.Home)
    object Inventory : BottomNavItem(Screen.Inventory.route, "Inventory", Icons.Default.Inventory2)
    object Customers : BottomNavItem(Screen.Customers.route, "Customers", Icons.Default.People)
    object Reports : BottomNavItem(Screen.Reports.route, "Reports", Icons.Default.Assessment)
    object More : BottomNavItem(Screen.Settings.route, "More", Icons.Default.MoreHoriz)
}

