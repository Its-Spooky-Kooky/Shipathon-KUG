package com.eta.assistant.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eta.assistant.data.local.entity.ProductEntity
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.EtaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryUiState(
    val products: List<ProductEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val currencySymbol: String = "₹",
    val defaultThreshold: Double = 5.0
)

class InventoryViewModel(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")

    val uiState: StateFlow<InventoryUiState> = combine(
        repository.getAllProducts(),
        preferencesRepository.userPreferencesFlow,
        _searchQuery,
        _selectedCategory
    ) { products, prefs, query, category ->
        val filtered = products.filter { product ->
            val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true) || product.category.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
        InventoryUiState(
            products = filtered,
            searchQuery = query,
            selectedCategory = category,
            currencySymbol = prefs.currencySymbol,
            defaultThreshold = prefs.defaultLowStockThreshold
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun addProduct(
        name: String,
        category: String,
        unit: String,
        stock: Double,
        sellingPrice: Double,
        costPrice: Double = 0.0,
        threshold: Double = 5.0,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addProduct(
                ProductEntity(
                    name = name.trim(),
                    category = category.trim().ifBlank { "Groceries" },
                    unit = unit.trim().ifBlank { "pcs" },
                    stockQuantity = stock,
                    sellingPrice = sellingPrice,
                    costPrice = costPrice,
                    lowStockThreshold = threshold
                )
            )
            onSuccess()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adjustStock(productId: Long, change: Double, reason: String = "RESTOCK") {
        viewModelScope.launch {
            repository.adjustProductStock(productId, change, reason)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }
}

class InventoryViewModelFactory(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InventoryViewModel(repository, preferencesRepository) as T
    }
}

