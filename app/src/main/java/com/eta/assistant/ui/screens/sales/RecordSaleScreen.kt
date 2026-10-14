package com.eta.assistant.ui.screens.sales

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.core.theme.OrangePrimaryContainer
import com.eta.assistant.core.util.CurrencyFormatter
import com.eta.assistant.data.local.entity.ProductEntity

@Composable
fun RecordSaleScreen(
    viewModel: SalesViewModel,
    onNavigateBack: () -> Unit,
    onProceedToConfirm: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showCustomerPicker by remember { mutableStateOf(false) }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = com.eta.assistant.core.util.LocaleStrings.get("record_sale", state.languageCode),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text(com.eta.assistant.core.util.LocaleStrings.get("search_products_hint", state.languageCode)) },
            modifier = Modifier.fillMaxWidth(),
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

        // Customer selection row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCustomerPicker = !showCustomerPicker },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Customer",
                        tint = OrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.selectedCustomer?.let { "${com.eta.assistant.core.util.LocaleStrings.get("customer_prefix", state.languageCode)}: ${it.name}" } ?: com.eta.assistant.core.util.LocaleStrings.get("walk_in_customer", state.languageCode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = if (showCustomerPicker) com.eta.assistant.core.util.LocaleStrings.get("close_picker", state.languageCode) else com.eta.assistant.core.util.LocaleStrings.get("change_customer", state.languageCode),
                    style = MaterialTheme.typography.labelMedium,
                    color = OrangePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showCustomerPicker) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = com.eta.assistant.core.util.LocaleStrings.get("walk_in_customer", state.languageCode),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectCustomer(null)
                                showCustomerPicker = false
                            }
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (state.selectedCustomer == null) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    state.customers.forEach { customer ->
                        Text(
                            text = "${customer.name} (${com.eta.assistant.core.util.LocaleStrings.get("due_label", state.languageCode)}: ${CurrencyFormatter.format(customer.totalDue, state.currencySymbol)})",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectCustomer(customer)
                                    showCustomerPicker = false
                                }
                                .padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (state.selectedCustomer?.id == customer.id) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Product Catalog List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.availableProducts, key = { it.id }) { product ->
                val cartItem = state.cartItems.find { it.product.id == product.id }
                ProductCartRow(
                    product = product,
                    quantityInCart = cartItem?.quantity ?: 0.0,
                    currency = state.currencySymbol,
                    onAdd = { viewModel.addToCart(product) },
                    onIncrement = { viewModel.updateQuantity(product.id, (cartItem?.quantity ?: 0.0) + 1.0) },
                    onDecrement = { viewModel.updateQuantity(product.id, (cartItem?.quantity ?: 0.0) - 1.0) }
                )
            }
        }

        // Bottom Bar with Total & Proceed Button
        if (state.cartItems.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${com.eta.assistant.core.util.LocaleStrings.get("total_amount", state.languageCode)} (${state.cartItems.size} ${com.eta.assistant.core.util.LocaleStrings.get("items_count", state.languageCode)})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(state.totalAmount, state.currencySymbol),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onProceedToConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = com.eta.assistant.core.util.LocaleStrings.get("review_confirm", state.languageCode),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCartRow(
    product: ProductEntity,
    quantityInCart: Double,
    currency: String,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${CurrencyFormatter.format(product.sellingPrice, currency)} • Stock: ${product.stockQuantity.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (quantityInCart == 0.0) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimaryContainer,
                        contentColor = OrangePrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Add", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangePrimaryContainer)
                    ) {
                        Icon(Icons.Default.Remove, "Decrease", tint = OrangePrimary, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${quantityInCart.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangePrimaryContainer)
                    ) {
                        Icon(Icons.Default.Add, "Increase", tint = OrangePrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

