package com.caiwuguan.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.domain.model.Ledger
import com.caiwuguan.domain.repository.LedgerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val ledgerRepository: LedgerRepository
) : ViewModel() {

    val ledgers: StateFlow<List<Ledger>> = ledgerRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingLedger = MutableStateFlow<Ledger?>(null)
    val editingLedger: StateFlow<Ledger?> = _editingLedger.asStateFlow()

    fun showAddDialog() {
        _editingLedger.value = null
        _showAddDialog.value = true
    }

    fun showEditDialog(ledger: Ledger) {
        _editingLedger.value = ledger
        _showAddDialog.value = true
    }

    fun dismissDialog() {
        _showAddDialog.value = false
        _editingLedger.value = null
    }

    fun saveLedger(name: String, isDefault: Boolean) {
        viewModelScope.launch {
            if (isDefault) {
                ledgerRepository.clearDefaults()
            }

            val ledger = Ledger(
                id = _editingLedger.value?.id ?: 0,
                name = name,
                isDefault = isDefault
            )
            ledgerRepository.insert(ledger)
            dismissDialog()
        }
    }

    fun setDefault(ledger: Ledger) {
        viewModelScope.launch {
            ledgerRepository.clearDefaults()
            ledgerRepository.insert(ledger.copy(isDefault = true))
        }
    }

    fun deleteLedger(ledger: Ledger) {
        viewModelScope.launch {
            ledgerRepository.deleteById(ledger.id)
        }
    }
}
