package com.eta.assistant.ui.screens.inventory

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eta.assistant.core.theme.ErrorRed
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.core.theme.OrangePrimaryContainer
import com.eta.assistant.core.theme.SuccessGreen
import com.eta.assistant.core.util.CurrencyFormatter
import com.eta.assistant.data.local.entity.ProductEntity

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onAddProductClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedProductForStock by remember { mutableStateOf<ProductEntity?>(null) }
    val categories = listOf("All", "Groceries", "Snacks", "Beverages", "Others")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = com.eta.assistant.core.util.LocaleStrings.get("inventory", state.languageCode),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // (+) Add button with Orange accent
            IconButton(
                onClick = onAddProductClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangePrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = com.eta.assistant.core.util.LocaleStrings.get("add_product", state.languageCode),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(com.eta.assistant.core.util.LocaleStrings.get("search_products_hint", state.languageCode)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = state.selectedCategory == cat
                val catLabel = when (cat.lowercase()) {
                    "all" -> com.eta.assistant.core.util.LocaleStrings.get("category_all", state.languageCode)
                    "groceries" -> com.eta.assistant.core.util.LocaleStrings.get("cat_groceries", state.languageCode)
                    "snacks" -> com.eta.assistant.core.util.LocaleStrings.get("cat_snacks", state.languageCode)
                    "beverages" -> com.eta.assistant.core.util.LocaleStrings.get("cat_beverages", state.languageCode)
                    "others" -> com.eta.assistant.core.util.LocaleStrings.get("cat_others", state.languageCode)
                    else -> cat
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onCategorySelect(cat) },
                    label = { Text(catLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangePrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Products List
        if (state.products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocalMall,
                        contentDescription = "No products",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = com.eta.assistant.core.util.LocaleStrings.get("no_products_found", state.languageCode),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.products, key = { it.id }) { product ->
                    ProductListItem(
                        product = product,
                        currency = state.currencySymbol,
                        onClick = { selectedProductForStock = product },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (selectedProductForStock != null) {
        val prod = selectedProductForStock!!
        AdjustStockDialog(
            product = prod,
            currency = state.currencySymbol,
            onDismiss = { selectedProductForStock = null },
            onAdjustStock = { change, reason ->
                viewModel.adjustStock(prod.id, change, reason)
                selectedProductForStock = null
            },
            onUpdatePrice = { newPrice ->
                viewModel.updateProduct(prod.copy(sellingPrice = newPrice))
                selectedProductForStock = null
            }
        )
    }
}

@Composable
private fun ProductListItem(
    product: ProductEntity,
    currency: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.lowStockThreshold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Product Icon Thumbnail
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangePrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (product.category.equals("Snacks", true)) Icons.Default.Fastfood else Icons.Default.LocalMall,
                        contentDescription = product.name,
                        tint = OrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${product.stockQuantity.toInt()} ${product.unit} in stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLowStock) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isLowStock) FontWeight.SemiBold else FontWeight.Normal
                        )

                        if (isLowStock) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Low Stock",
                                tint = ErrorRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.format(product.sellingPrice, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustStockDialog(
    product: ProductEntity,
    currency: String,
    onDismiss: () -> Unit,
    onAdjustStock: (Double, String) -> Unit,
    onUpdatePrice: (Double) -> Unit
) {
    var customAdjustment by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf(product.sellingPrice.toString()) }
    var isEditingPrice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${product.category} • In Stock: ${product.stockQuantity.toInt()} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Quick Restock (+)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5.0, 10.0, 25.0, 50.0).forEach { amount ->
                        Button(
                            onClick = { onAdjustStock(amount, "RESTOCK_QUICK") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen.copy(alpha = 0.15f),
                                contentColor = SuccessGreen
                            )
                        ) {
                            Text("+${amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Custom Stock Adjustment (+ / -)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customAdjustment,
                        onValueChange = { customAdjustment = it },
                        placeholder = { Text("e.g. 15 or -2") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            val qty = customAdjustment.toDoubleOrNull()
                            if (qty != null && qty != 0.0) {
                                val reason = if (qty > 0) "RESTOCK" else "DAMAGED_CORRECTION"
                                onAdjustStock(qty, reason)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = customAdjustment.toDoubleOrNull() != null
                    ) {
                        Text("Apply")
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Selling Price",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = CurrencyFormatter.format(product.sellingPrice, currency),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isEditingPrice) {
                        OutlinedButton(onClick = { isEditingPrice = true }) {
                            Text("Edit Price")
                        }
                    }
                }

                if (isEditingPrice) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            placeholder = { Text("New Price") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                val newP = priceInput.toDoubleOrNull()
                                if (newP != null && newP >= 0.0) {
                                    onUpdatePrice(newP)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(10.dp),
                            enabled = priceInput.toDoubleOrNull() != null
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

