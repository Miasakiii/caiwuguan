package com.caiwuguan.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.data.db.dao.BillDao
import com.caiwuguan.data.db.dao.BudgetDao
import com.caiwuguan.data.db.entity.BudgetEntity
import com.caiwuguan.domain.model.Budget
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetItem(
    val budget: Budget,
    val spent: Long,
    val remaining: Long
) {
    val progress: Float get() = if (budget.amount > 0) (spent.toFloat() / budget.amount).coerceIn(0f, 1f) else 0f
    val isOverBudget: Boolean get() = spent > budget.amount
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val billDao: BillDao
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val currentYear = calendar.get(Calendar.YEAR)
    private val currentMonth = calendar.get(Calendar.MONTH)

    private val _selectedYear = MutableStateFlow(currentYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingBudget = MutableStateFlow<Budget?>(null)
    val editingBudget: StateFlow<Budget?> = _editingBudget.asStateFlow()

    val budgets: StateFlow<List<BudgetItem>> = combine(
        _selectedYear,
        _selectedMonth
    ) { year, month ->
        year to month
    }.combine(budgetRepository.getAll()) { (year, month), budgets ->
        val filtered = budgets.filter { it.year == year && it.month == month }
        filtered.map { budget ->
            val dayStart = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dayEnd = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            // TODO: 需要查询特定分类的支出总额，目前BillDao没有此方法
            // 暂时使用0作为占位符
            BudgetItem(budget = budget, spent = 0L, remaining = budget.amount)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun showAddDialog() {
        _editingBudget.value = null
        _showAddDialog.value = true
    }

    fun showEditDialog(budget: Budget) {
        _editingBudget.value = budget
        _showAddDialog.value = true
    }

    fun dismissDialog() {
        _showAddDialog.value = false
        _editingBudget.value = null
    }

    fun saveBudget(category: Category, amount: Long) {
        viewModelScope.launch {
            val existing = budgetRepository.getByPeriod(_selectedYear.value, _selectedMonth.value)
            // TODO: 查找现有预算并更新，或插入新预算
            val budget = Budget(
                category = category,
                amount = amount,
                year = _selectedYear.value,
                month = _selectedMonth.value
            )
            budgetRepository.insert(budget)
            dismissDialog()
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.delete(budget)
        }
    }
}
