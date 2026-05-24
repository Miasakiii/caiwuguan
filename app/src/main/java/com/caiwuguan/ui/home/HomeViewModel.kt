package com.caiwuguan.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {

    private val _todayExpense = MutableStateFlow(0L)
    val todayExpense: StateFlow<Long> = _todayExpense.asStateFlow()

    private val _todayIncome = MutableStateFlow(0L)
    val todayIncome: StateFlow<Long> = _todayIncome.asStateFlow()

    private val _monthExpense = MutableStateFlow(0L)
    val monthExpense: StateFlow<Long> = _monthExpense.asStateFlow()

    private val _monthIncome = MutableStateFlow(0L)
    val monthIncome: StateFlow<Long> = _monthIncome.asStateFlow()

    private val _recentBills = MutableStateFlow<List<Bill>>(emptyList())
    val recentBills: StateFlow<List<Bill>> = _recentBills.asStateFlow()

    init {
        val today = getDayRange()
        val month = getMonthRange()

        viewModelScope.launch {
            repository.getTotalExpense(today.first, today.second).collect {
                _todayExpense.value = it
            }
        }
        viewModelScope.launch {
            repository.getTotalIncome(today.first, today.second).collect {
                _todayIncome.value = it
            }
        }
        viewModelScope.launch {
            repository.getTotalExpense(month.first, month.second).collect {
                _monthExpense.value = it
            }
        }
        viewModelScope.launch {
            repository.getTotalIncome(month.first, month.second).collect {
                _monthIncome.value = it
            }
        }
        viewModelScope.launch {
            repository.getRecentBills().collect {
                _recentBills.value = it
            }
        }
    }

    private fun getDayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }
}
