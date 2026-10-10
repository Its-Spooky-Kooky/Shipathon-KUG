package com.eta.assistant.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eta.assistant.data.local.dao.CustomerWithBalance
import com.eta.assistant.data.local.dao.SaleWithItems
import com.eta.assistant.data.local.entity.CustomerEntity
import com.eta.assistant.data.local.entity.ProductEntity
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.CartItem
import com.eta.assistant.data.repository.EtaRepository
import com.eta.assistant.data.repository.SaleResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SalesUiState(
    val availableProducts: List<ProductEntity> = emptyList(),
    val customers: List<CustomerWithBalance> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val selectedCustomer: CustomerWithBalance? = null,
    val paymentMode: String = "CASH", // CASH, UPI, CREDIT
    val currencySymbol: String = "₹",
    val searchQuery: String = "",
    val error: String? = null
) {
    val totalAmount: Double get() = cartItems.sumOf { it.subtotal }
}

class SalesViewModel(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _selectedCustomer = MutableStateFlow<CustomerWithBalance?>(null)
    private val _paymentMode = MutableStateFlow("CASH")
    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    private data class CheckoutFormState(
        val cart: List<CartItem>,
        val customer: CustomerWithBalance?,
        val mode: String,
        val error: String?
    )

    private val checkoutFormFlow = combine(
        _cartItems,
        _selectedCustomer,
        _paymentMode,
        _error
    ) { cart, customer, mode, err ->
        CheckoutFormState(cart, customer, mode, err)
    }

    private val filteredProductsFlow = combine(
        repository.getAllProducts(),
        _searchQuery
    ) { products, query ->
        val filtered = if (query.isBlank()) products else {
            products.filter { it.name.contains(query, ignoreCase = true) }
        }
        Pair(filtered, query)
    }

    val uiState: StateFlow<SalesUiState> = combine(
        filteredProductsFlow,
        repository.getCustomersWithBalances(),
        preferencesRepository.userPreferencesFlow,
        checkoutFormFlow
    ) { filteredPair, customers, prefs, form ->
        SalesUiState(
            availableProducts = filteredPair.first,
            customers = customers,
            cartItems = form.cart,
            selectedCustomer = form.customer,
            paymentMode = form.mode,
            currencySymbol = prefs.currencySymbol,
            searchQuery = filteredPair.second,
            error = form.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SalesUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: ProductEntity) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = currentList[index]
            if (item.quantity + 1.0 <= product.stockQuantity) {
                currentList[index] = item.copy(quantity = item.quantity + 1.0)
            }
        } else {
            if (product.stockQuantity >= 1.0) {
                currentList.add(CartItem(product = product, quantity = 1.0))
            }
        }
        _cartItems.value = currentList
        _error.value = null
    }

    fun updateQuantity(productId: Long, newQuantity: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (newQuantity <= 0) {
                currentList.removeAt(index)
            } else {
                val item = currentList[index]
                if (newQuantity <= item.product.stockQuantity) {
                    currentList[index] = item.copy(quantity = newQuantity)
                }
            }
            _cartItems.value = currentList
        }
    }

    fun selectCustomer(customer: CustomerWithBalance?) {
        _selectedCustomer.value = customer
    }

    fun setPaymentMode(mode: String) {
        _paymentMode.value = mode
    }

    fun confirmSale(onSuccess: (Long) -> Unit) {
        val state = uiState.value
        if (state.cartItems.isEmpty()) {
            _error.value = "Please select at least one product"
            return
        }

        viewModelScope.launch {
            val paidAmount = if (state.paymentMode == "CREDIT") 0.0 else state.totalAmount
            val customerName = state.selectedCustomer?.name ?: "Walk-in Customer"
            val customerId = state.selectedCustomer?.id

            val result = repository.recordSale(
                customerId = customerId,
                customerName = customerName,
                items = state.cartItems,
                paidAmount = paidAmount,
                paymentMethod = state.paymentMode
            )

            when (result) {
                is SaleResult.Success -> {
                    _cartItems.value = emptyList()
                    _selectedCustomer.value = null
                    _error.value = null
                    onSuccess(result.saleId)
                }
                is SaleResult.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    suspend fun getSaleDetails(saleId: Long): SaleWithItems? {
        return repository.getSaleWithItems(saleId)
    }
}

class SalesViewModelFactory(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SalesViewModel(repository, preferencesRepository) as T
    }
}
