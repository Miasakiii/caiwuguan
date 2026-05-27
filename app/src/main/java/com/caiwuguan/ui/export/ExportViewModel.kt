package com.caiwuguan.ui.export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.data.exporter.CsvExporter
import com.caiwuguan.domain.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val csvExporter: CsvExporter
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    fun selectMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading

            try {
                // 获取指定月份的账单
                val cal = Calendar.getInstance()
                cal.set(_selectedYear.value, _selectedMonth.value - 1, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val end = cal.timeInMillis

                billRepository.getBillsByDateRange(start, end).collect { bills ->
                    if (bills.isEmpty()) {
                        _exportState.value = ExportState.Error("该月没有账单记录")
                        return@collect
                    }

                    val uri = csvExporter.exportToCsv(context, bills)
                    if (uri != null) {
                        _exportState.value = ExportState.Success(uri)
                    } else {
                        _exportState.value = ExportState.Error("导出失败")
                    }
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error("导出失败：${e.message}")
            }
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val uri: Uri) : ExportState()
    data class Error(val message: String) : ExportState()
}
