package com.eta.assistant.ui.screens.customers

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eta.assistant.data.local.dao.CustomerWithBalance
import com.eta.assistant.data.local.entity.CustomerEntity
import com.eta.assistant.data.local.entity.PaymentEntity
import com.eta.assistant.data.local.entity.SaleEntity
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.EtaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomersUiState(
    val customers: List<CustomerWithBalance> = emptyList(),
    val searchQuery: String = "",
    val currencySymbol: String = "₹",
    val businessName: String = "My Store"
)

class CustomersViewModel(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<CustomersUiState> = combine(
        repository.getCustomersWithBalances(),
        preferencesRepository.userPreferencesFlow,
        _searchQuery
    ) { customers, prefs, query ->
        val filtered = if (query.isBlank()) customers else {
            customers.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query) }
        }
        CustomersUiState(
            customers = filtered,
            searchQuery = query,
            currencySymbol = prefs.currencySymbol,
            businessName = prefs.businessName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomersUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addCustomer(name: String, phone: String, notes: String = "") {
        viewModelScope.launch {
            repository.addCustomer(
                CustomerEntity(
                    name = name.trim(),
                    phone = phone.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun recordPayment(customerId: Long, amount: Double, method: String, notes: String = "") {
        viewModelScope.launch {
            repository.recordCustomerPayment(
                customerId = customerId,
                amount = amount,
                paymentMethod = method,
                notes = notes
            )
        }
    }

    fun getCustomerSales(customerId: Long) = repository.getSalesForCustomer(customerId)
    fun getCustomerPayments(customerId: Long) = repository.getPaymentsForCustomer(customerId)

    fun sharePaymentReminder(context: Context, customerName: String, dueAmount: Double, currency: String, storeName: String) {
        val message = "Hello $customerName, your pending balance at $storeName is $currency $dueAmount. Kindly settle at your earliest convenience. Thank you!"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Send Payment Reminder")
        context.startActivity(shareIntent)
    }
}

class CustomersViewModelFactory(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomersViewModel(repository, preferencesRepository) as T
    }
}

