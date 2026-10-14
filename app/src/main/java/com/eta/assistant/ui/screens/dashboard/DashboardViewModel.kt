package com.eta.assistant.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eta.assistant.core.util.DateUtils
import com.eta.assistant.data.local.entity.SaleEntity
import com.eta.assistant.data.preferences.UserPreferences
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.EtaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val ownerName: String = "Ravi Kumar",
    val businessName: String = "Ravi Store",
    val currencySymbol: String = "₹",
    val todaySales: Double = 0.0,
    val todayDues: Double = 0.0,
    val todayItemsSold: Double = 0.0,
    val lowStockCount: Int = 0,
    val recentSales: List<SaleEntity> = emptyList(),
    val isOffline: Boolean = true,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val languageCode: String = "en"
)

private data class TodayMetrics(
    val sales: Double,
    val dues: Double,
    val items: Double,
    val lowStock: Int
)

class DashboardViewModel(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val startOfDay = DateUtils.getStartOfDay()
    private val endOfDay = System.currentTimeMillis() + 86400000L

    private val todayMetricsFlow = combine(
        repository.getSalesAmountBetween(startOfDay, endOfDay),
        repository.getTotalOutstandingDues(),
        repository.getItemsSoldBetween(startOfDay, endOfDay),
        repository.getLowStockCount()
    ) { sales, dues, items, lowStock ->
        TodayMetrics(sales, dues, items, lowStock)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        preferencesRepository.userPreferencesFlow,
        todayMetricsFlow,
        repository.getRecentSales(5)
    ) { prefs, metrics, recentSales ->
        DashboardUiState(
            ownerName = prefs.ownerName,
            businessName = prefs.businessName,
            currencySymbol = prefs.currencySymbol,
            todaySales = metrics.sales,
            todayDues = metrics.dues,
            todayItemsSold = metrics.items,
            lowStockCount = metrics.lowStock,
            recentSales = recentSales,
            isOffline = true,
            lastSyncTime = prefs.lastSyncTimestamp,
            languageCode = prefs.languageCode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun getGreeting(lang: String = "en"): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val key = when {
            hour < 12 -> "good_morning"
            hour < 17 -> "good_afternoon"
            else -> "good_evening"
        }
        return com.eta.assistant.core.util.LocaleStrings.get(key, lang)
    }

    suspend fun getSaleWithItems(saleId: Long): com.eta.assistant.data.local.dao.SaleWithItems? {
        return repository.getSaleWithItems(saleId)
    }

    fun cancelSale(saleId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.cancelSale(saleId)
            onDone()
        }
    }
}

class DashboardViewModelFactory(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository, preferencesRepository) as T
    }
}
