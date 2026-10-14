package com.eta.assistant.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eta.assistant.core.theme.ErrorRed
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.core.theme.OrangePrimaryContainer
import com.eta.assistant.core.theme.SuccessGreen
import com.eta.assistant.core.theme.WarningAmber
import com.eta.assistant.core.util.CurrencyFormatter
import com.eta.assistant.core.util.DateUtils
import com.eta.assistant.data.local.dao.SaleWithItems
import com.eta.assistant.data.local.entity.SaleEntity
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onRecordSaleVoice: () -> Unit,
    onRecordSaleManual: () -> Unit,
    onAddProduct: () -> Unit,
    onViewDues: () -> Unit,
    onReports: () -> Unit,
    onLowStockClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSaleClick: (Long) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedSaleWithItems by remember { mutableStateOf<SaleWithItems?>(null) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with greeting and offline status
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrangePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Store",
                            tint = OrangePrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = viewModel.getGreeting(state.languageCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.ownerName.ifBlank { state.businessName },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Offline badge pill matching the screenshot
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SuccessGreen.copy(alpha = 0.12f))
                        .clickable { onSyncClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = com.eta.assistant.core.util.LocaleStrings.get("offline", state.languageCode),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }

        // 2. Today's Overview
        item {
            Text(
                text = com.eta.assistant.core.util.LocaleStrings.get("todays_overview", state.languageCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OverviewCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = SuccessGreen,
                        iconBg = SuccessGreen.copy(alpha = 0.12f),
                        title = com.eta.assistant.core.util.LocaleStrings.get("sales", state.languageCode),
                        value = CurrencyFormatter.format(state.todaySales, state.currencySymbol)
                    )

                    OverviewCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.People,
                        iconTint = OrangePrimary,
                        iconBg = OrangePrimary.copy(alpha = 0.12f),
                        title = com.eta.assistant.core.util.LocaleStrings.get("dues", state.languageCode),
                        value = CurrencyFormatter.format(state.todayDues, state.currencySymbol),
                        onClick = onViewDues
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OverviewCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Receipt,
                        iconTint = Color(0xFF3B82F6),
                        iconBg = Color(0xFF3B82F6).copy(alpha = 0.12f),
                        title = com.eta.assistant.core.util.LocaleStrings.get("items_sold", state.languageCode),
                        value = state.todayItemsSold.toInt().toString()
                    )

                    OverviewCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Warning,
                        iconTint = WarningAmber,
                        iconBg = WarningAmber.copy(alpha = 0.12f),
                        title = com.eta.assistant.core.util.LocaleStrings.get("low_stock", state.languageCode),
                        value = state.lowStockCount.toString(),
                        onClick = onLowStockClick
                    )
                }
            }
        }

        // 3. Quick Actions
        item {
            Text(
                text = com.eta.assistant.core.util.LocaleStrings.get("quick_actions", state.languageCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Voice Sale & Manual Sale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Mic,
                        title = com.eta.assistant.core.util.LocaleStrings.get("record_sale_voice", state.languageCode),
                        isHighlighted = true,
                        onClick = onRecordSaleVoice
                    )

                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PointOfSale,
                        title = com.eta.assistant.core.util.LocaleStrings.get("new_sale_manual", state.languageCode),
                        isHighlighted = false,
                        onClick = onRecordSaleManual
                    )
                }

                // Row 2: Add Product & View Dues
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        title = com.eta.assistant.core.util.LocaleStrings.get("add_product", state.languageCode),
                        onClick = onAddProduct
                    )

                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.People,
                        title = com.eta.assistant.core.util.LocaleStrings.get("view_dues", state.languageCode),
                        onClick = onViewDues
                    )
                }

                // Row 3: Reports
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Assessment,
                        title = com.eta.assistant.core.util.LocaleStrings.get("reports", state.languageCode),
                        onClick = onReports
                    )
                }
            }
        }

        // 4. Recent Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.eta.assistant.core.util.LocaleStrings.get("recent_transactions", state.languageCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (state.recentSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = com.eta.assistant.core.util.LocaleStrings.get("no_sales_empty", state.languageCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.recentSales) { sale ->
                RecentSaleItem(
                    sale = sale,
                    currency = state.currencySymbol,
                    onClick = {
                        onSaleClick(sale.id)
                        scope.launch {
                            selectedSaleWithItems = viewModel.getSaleWithItems(sale.id)
                            showReceiptDialog = true
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Receipt Modal Dialog
    if (showReceiptDialog && selectedSaleWithItems != null) {
        val saleDetails = selectedSaleWithItems!!
        val isCancelled = saleDetails.sale.status == "CANCELLED"
        val remainingDue = saleDetails.sale.totalAmount - saleDetails.sale.paidAmount

        AlertDialog(
            onDismissRequest = { showReceiptDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Receipt #${saleDetails.sale.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCancelled) ErrorRed.copy(alpha = 0.15f)
                                else if (remainingDue <= 0.0) SuccessGreen.copy(alpha = 0.15f)
                                else WarningAmber.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isCancelled) "CANCELLED" else if (remainingDue <= 0.0) "PAID" else "CREDIT DUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCancelled) ErrorRed else if (remainingDue <= 0.0) SuccessGreen else WarningAmber
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Customer: ${saleDetails.sale.customerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Date: ${DateUtils.formatDateTime(saleDetails.sale.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Payment Mode: ${saleDetails.sale.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Items Sold:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    saleDetails.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.productName} (${item.quantity.toInt()}x)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = CurrencyFormatter.format(item.subtotal, state.currencySymbol),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Amount", fontWeight = FontWeight.Bold)
                        Text(
                            text = CurrencyFormatter.format(saleDetails.sale.totalAmount, state.currencySymbol),
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }

                    if (remainingDue > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Balance Due", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = CurrencyFormatter.format(remainingDue, state.currencySymbol),
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val receiptText = buildString {
                                append("🧾 Receipt from ${state.businessName}\n")
                                append("Receipt #: ${saleDetails.sale.id}\n")
                                append("Date: ${DateUtils.formatDateTime(saleDetails.sale.createdAt)}\n")
                                append("Customer: ${saleDetails.sale.customerName}\n")
                                append("----------------------------\n")
                                saleDetails.items.forEach { item ->
                                    append("${item.productName} x ${item.quantity.toInt()} = ${CurrencyFormatter.format(item.subtotal, state.currencySymbol)}\n")
                                }
                                append("----------------------------\n")
                                append("Total: ${CurrencyFormatter.format(saleDetails.sale.totalAmount, state.currencySymbol)}\n")
                                append("Paid: ${CurrencyFormatter.format(saleDetails.sale.paidAmount, state.currencySymbol)}\n")
                                if (remainingDue > 0) {
                                    append("Due Balance: ${CurrencyFormatter.format(remainingDue, state.currencySymbol)}\n")
                                }
                                append("Payment Mode: ${saleDetails.sale.paymentMethod}\n\n")
                                append("Thank you for shopping with us!")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }

                    if (!isCancelled) {
                        OutlinedButton(
                            onClick = { showCancelConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                        ) {
                            Text("Refund")
                        }
                    }

                    OutlinedButton(onClick = { showReceiptDialog = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    if (showCancelConfirmDialog && selectedSaleWithItems != null) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Confirm Refund / Cancellation") },
            text = { Text("Are you sure you want to cancel Receipt #${selectedSaleWithItems!!.sale.id}? This will restore all item quantities back to your inventory stock.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelSale(selectedSaleWithItems!!.sale.id) {
                            showCancelConfirmDialog = false
                            showReceiptDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Yes, Refund")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("No, Keep")
                }
            }
        )
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) OrangePrimaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isHighlighted) OrangePrimary else OrangePrimary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isHighlighted) Color.White else OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun RecentSaleItem(
    sale: SaleEntity,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Sale",
                        tint = OrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = sale.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateUtils.formatDateTime(sale.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(sale.totalAmount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sale.paymentMethod,
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

