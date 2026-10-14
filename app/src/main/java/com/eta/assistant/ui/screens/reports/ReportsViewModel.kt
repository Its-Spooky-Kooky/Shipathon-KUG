package com.eta.assistant.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eta.assistant.core.util.DateUtils
import com.eta.assistant.data.local.dao.TopProductSummary
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.EtaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

import com.eta.assistant.data.local.entity.ExpenseEntity
import kotlinx.coroutines.launch

enum class ReportPeriod(val title: String, val days: Int) {
    TODAY("Today", 0),
    SEVEN_DAYS("7 Days", 7),
    THIRTY_DAYS("30 Days", 30)
}

data class ReportsUiState(
    val selectedPeriod: ReportPeriod = ReportPeriod.TODAY,
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalDues: Double = 0.0,
    val transactionCount: Int = 0,
    val topSellingItems: List<TopProductSummary> = emptyList(),
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val currencySymbol: String = "₹",
    val salesTrendBars: List<Float> = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 1.0f),
    val languageCode: String = "en"
)

private data class ReportFinancials(
    val sales: Double,
    val expenses: Double,
    val dues: Double,
    val txCount: Int
)

class ReportsViewModel(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.TODAY)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReportsUiState> = _selectedPeriod.flatMapLatest { period ->
        val startTime = if (period == ReportPeriod.TODAY) DateUtils.getStartOfDay() else DateUtils.getStartOfDaysAgo(period.days)
        val endTime = System.currentTimeMillis() + 86400000L

        val financialsFlow = combine(
            repository.getSalesAmountBetween(startTime, endTime),
            repository.getExpensesAmountBetween(startTime, endTime),
            repository.getTotalOutstandingDues(),
            repository.getTransactionCountBetween(startTime, endTime)
        ) { sales, expenses, dues, txCount ->
            ReportFinancials(sales, expenses, dues, txCount)
        }

        combine(
            financialsFlow,
            repository.getTopSellingProducts(startTime, endTime, 5),
            repository.getAllExpenses(),
            preferencesRepository.userPreferencesFlow
        ) { fin, topItems, expenses, prefs ->
            ReportsUiState(
                selectedPeriod = period,
                totalSales = fin.sales,
                totalExpenses = fin.expenses,
                totalDues = fin.dues,
                transactionCount = fin.txCount,
                topSellingItems = topItems,
                recentExpenses = expenses.take(10),
                currencySymbol = prefs.currencySymbol,
                languageCode = prefs.languageCode
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState()
    )

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun addExpense(title: String, category: String, amount: Double, notes: String = "") {
        viewModelScope.launch {
            repository.addExpense(title, category, amount, notes)
        }
    }
}

class ReportsViewModelFactory(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReportsViewModel(repository, preferencesRepository) as T
    }
}
