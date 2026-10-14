package com.eta.assistant.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eta.assistant.core.util.DateUtils
import com.eta.assistant.data.local.dao.TopProductSummary
import com.eta.assistant.data.local.entity.ExpenseEntity
import com.eta.assistant.data.local.entity.SaleEntity
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.EtaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val trendPercentageText: String = "+0% vs last period",
    val transactionCount: Int = 0,
    val topSellingItems: List<TopProductSummary> = emptyList(),
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val currencySymbol: String = "₹",
    val salesTrendBars: List<Float> = List(7) { 0.1f },
    val languageCode: String = "en"
)

private data class FinancialPartA(
    val sales: Double,
    val expenses: Double,
    val dues: Double,
    val txCount: Int
)

private data class FinancialPartB(
    val cogs: Double,
    val prevSales: Double,
    val periodSales: List<SaleEntity>
)

private data class ReportFinancials(
    val sales: Double,
    val expenses: Double,
    val dues: Double,
    val txCount: Int,
    val cogs: Double,
    val prevSales: Double,
    val periodSales: List<SaleEntity>
)

class ReportsViewModel(
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.TODAY)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReportsUiState> = _selectedPeriod.flatMapLatest { period ->
        val now = System.currentTimeMillis()
        val (startTime, endTime, prevStartTime, prevEndTime) = when (period) {
            ReportPeriod.TODAY -> {
                val startToday = DateUtils.getStartOfDay()
                val endToday = startToday + 86400000L - 1
                val startYesterday = startToday - 86400000L
                val endYesterday = startToday - 1
                listOf(startToday, endToday, startYesterday, endYesterday)
            }
            ReportPeriod.SEVEN_DAYS -> {
                val start7 = DateUtils.getStartOfDaysAgo(7)
                val end7 = now
                val duration = end7 - start7
                listOf(start7, end7, start7 - duration, start7)
            }
            ReportPeriod.THIRTY_DAYS -> {
                val start30 = DateUtils.getStartOfDaysAgo(30)
                val end30 = now
                val duration = end30 - start30
                listOf(start30, end30, start30 - duration, start30)
            }
        }

        val flowA = combine(
            repository.getSalesAmountBetween(startTime, endTime),
            repository.getExpensesAmountBetween(startTime, endTime),
            repository.getTotalOutstandingDues(),
            repository.getTransactionCountBetween(startTime, endTime)
        ) { sales, expenses, dues, txCount ->
            FinancialPartA(sales, expenses, dues, txCount)
        }

        val flowB = combine(
            repository.getCostOfGoodsSold(startTime, endTime),
            repository.getSalesAmountBetween(prevStartTime, prevEndTime),
            repository.getSalesBetween(startTime, endTime)
        ) { cogs, prevSales, periodSales ->
            FinancialPartB(cogs, prevSales, periodSales)
        }

        val financialsFlow = combine(flowA, flowB) { a, b ->
            ReportFinancials(a.sales, a.expenses, a.dues, a.txCount, b.cogs, b.prevSales, b.periodSales)
        }

        combine(
            financialsFlow,
            repository.getTopSellingProducts(startTime, endTime, 5),
            repository.getAllExpenses(),
            preferencesRepository.userPreferencesFlow
        ) { fin, topItems, expenses, prefs ->
            val bucketDuration = (endTime - startTime).toDouble() / 7.0
            val bucketTotals = (0 until 7).map { i ->
                val bStart = (startTime + i * bucketDuration).toLong()
                val bEnd = (startTime + (i + 1) * bucketDuration).toLong()
                fin.periodSales
                    .filter { it.status == "COMPLETED" && it.createdAt in bStart until bEnd }
                    .sumOf { it.totalAmount }
            }
            val maxBucket = bucketTotals.maxOrNull() ?: 0.0
            val trendBars = if (maxBucket > 0) {
                bucketTotals.map { ((it / maxBucket).toFloat()).coerceIn(0.1f, 1.0f) }
            } else {
                List(7) { 0.1f }
            }

            val grossProfit = fin.sales - fin.cogs
            val netProfit = grossProfit - fin.expenses

            val diff = fin.sales - fin.prevSales
            val percent = if (fin.prevSales > 0) {
                (diff / fin.prevSales) * 100
            } else if (fin.sales > 0) {
                100.0
            } else {
                0.0
            }
            val periodKey = when (period) {
                ReportPeriod.TODAY -> "vs_yesterday"
                ReportPeriod.SEVEN_DAYS -> "vs_last_week"
                ReportPeriod.THIRTY_DAYS -> "vs_last_month"
            }
            val periodLabel = com.eta.assistant.core.util.LocaleStrings.get(periodKey, prefs.languageCode)
            val sign = if (percent >= 0) "+" else ""
            val trendPercentageText = "$sign${percent.toInt()}% $periodLabel"

            ReportsUiState(
                selectedPeriod = period,
                totalSales = fin.sales,
                totalExpenses = fin.expenses,
                totalDues = fin.dues,
                grossProfit = grossProfit,
                netProfit = netProfit,
                trendPercentageText = trendPercentageText,
                transactionCount = fin.txCount,
                topSellingItems = topItems,
                recentExpenses = expenses.take(10),
                currencySymbol = prefs.currencySymbol,
                salesTrendBars = trendBars,
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
