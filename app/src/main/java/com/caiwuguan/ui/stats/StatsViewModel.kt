package com.caiwuguan.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategoryStat(
    val category: Category,
    val total: Long,
    val percentage: Float
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {

    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    private val _totalExpense = MutableStateFlow(0L)
    val totalExpense: StateFlow<Long> = _totalExpense.asStateFlow()

    private val _totalIncome = MutableStateFlow(0L)
    val totalIncome: StateFlow<Long> = _totalIncome.asStateFlow()

    private val _categoryStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStat>> = _categoryStats.asStateFlow()

    private val _dailyTrend = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val dailyTrend: StateFlow<Map<Int, Long>> = _dailyTrend.asStateFlow()

    init { loadMonth() }

    fun previousMonth() {
        val totalMonths = _currentYear.value * 12 + _currentMonth.value - 2
        _currentYear.value = totalMonths / 12
        _currentMonth.value = totalMonths % 12 + 1
        loadMonth()
    }

    fun nextMonth() {
        val totalMonths = _currentYear.value * 12 + _currentMonth.value
        _currentYear.value = totalMonths / 12
        _currentMonth.value = totalMonths % 12 + 1
        loadMonth()
    }

    private fun loadMonth() {
        val cal = Calendar.getInstance()
        cal.set(_currentYear.value, _currentMonth.value - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis

        viewModelScope.launch {
            repository.getTotalExpense(start, end).collect { _totalExpense.value = it }
        }
        viewModelScope.launch {
            repository.getTotalIncome(start, end).collect { _totalIncome.value = it }
        }
        viewModelScope.launch {
            repository.getCategoryBreakdown(start, end).collect { breakdown ->
                val total = breakdown.values.sum().toFloat()
                _categoryStats.value = breakdown.map { (cat, amount) ->
                    CategoryStat(cat, amount, if (total > 0) amount / total else 0f)
                }.sortedByDescending { it.total }
            }
        }
        viewModelScope.launch {
            repository.getBillsByDateRange(start, end).collect { bills ->
                _dailyTrend.value = bills
                    .filter { it.type == com.caiwuguan.domain.model.BillType.EXPENSE }
                    .groupBy {
                        val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                        c.get(Calendar.DAY_OF_MONTH)
                    }.mapValues { (_, list) -> list.sumOf { it.amount } }
            }
        }
    }
}
