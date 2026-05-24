package com.caiwuguan.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillEditViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {

    private val _bill = MutableStateFlow<Bill?>(null)
    val bill: StateFlow<Bill?> = _bill.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _type = MutableStateFlow(BillType.EXPENSE)
    val type: StateFlow<BillType> = _type.asStateFlow()

    private val _category = MutableStateFlow(Category.OTHER)
    val category: StateFlow<Category> = _category.asStateFlow()

    private val _merchant = MutableStateFlow("")
    val merchant: StateFlow<String> = _merchant.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun loadBill(id: Long) {
        viewModelScope.launch {
            repository.getBillById(id).collect { b ->
                if (b != null && _bill.value == null) {
                    _bill.value = b
                    _amount.value = String.format("%.2f", b.amount / 100.0)
                    _type.value = b.type
                    _category.value = b.category
                    _merchant.value = b.merchant
                    _note.value = b.description
                }
            }
        }
    }

    fun updateAmount(value: String) { _amount.value = value }
    fun toggleType() { _type.value = if (_type.value == BillType.EXPENSE) BillType.INCOME else BillType.EXPENSE }
    fun updateCategory(value: Category) { _category.value = value }
    fun updateMerchant(value: String) { _merchant.value = value }
    fun updateNote(value: String) { _note.value = value }

    fun saveBill() {
        val current = _bill.value ?: return
        val amountValue = (_amount.value.toDoubleOrNull() ?: return) * 100
        if (amountValue <= 0) return

        viewModelScope.launch {
            repository.updateBill(
                current.copy(
                    amount = amountValue.toLong(),
                    type = _type.value,
                    category = _category.value,
                    merchant = _merchant.value,
                    description = _note.value
                )
            )
            _saved.value = true
        }
    }

    fun deleteBill() {
        val current = _bill.value ?: return
        viewModelScope.launch {
            repository.deleteBill(current)
            _saved.value = true
        }
    }
}
