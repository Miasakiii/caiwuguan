package com.caiwuguan.ui.bill

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
class BillListViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    private val _lastDeletedBill = MutableStateFlow<Bill?>(null)
    val lastDeletedBill: StateFlow<Bill?> = _lastDeletedBill.asStateFlow()

    init {
        loadMonth()
    }

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

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            _lastDeletedBill.value = bill
            repository.deleteBill(bill)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            _lastDeletedBill.value?.let { bill ->
                repository.insertBill(bill)
                _lastDeletedBill.value = null
            }
        }
    }

    fun clearLastDeleted() {
        _lastDeletedBill.value = null
    }

    private fun loadMonth() {
        val cal = Calendar.getInstance()
        cal.set(_currentYear.value, _currentMonth.value - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis

        viewModelScope.launch {
            repository.getBillsByDateRange(start, end).collect {
                _bills.value = it
            }
        }
    }
}
