package com.caiwuguan.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.ai.deepseek.ApiKeyManager
import com.caiwuguan.ai.deepseek.NlBillParser
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import com.caiwuguan.domain.repository.BillRepository
import com.caiwuguan.ui.addbill.NlBillData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddBillViewModel @Inject constructor(
    private val repository: BillRepository,
    private val nlBillParser: NlBillParser,
    private val apiKeyManager: ApiKeyManager
) : ViewModel() {

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _type = MutableStateFlow(BillType.EXPENSE)
    val type: StateFlow<BillType> = _type.asStateFlow()

    private val _category = MutableStateFlow(Category.FOOD)
    val category: StateFlow<Category> = _category.asStateFlow()

    private val _merchant = MutableStateFlow("")
    val merchant: StateFlow<String> = _merchant.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _timestamp = MutableStateFlow(System.currentTimeMillis())
    val timestamp: StateFlow<Long> = _timestamp.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _nlInput = MutableStateFlow("")
    val nlInput: StateFlow<String> = _nlInput.asStateFlow()

    private val _nlParsing = MutableStateFlow(false)
    val nlParsing: StateFlow<Boolean> = _nlParsing.asStateFlow()

    private val _nlResult = MutableStateFlow<NlBillData?>(null)
    val nlResult: StateFlow<NlBillData?> = _nlResult.asStateFlow()

    private val _nlError = MutableStateFlow<String?>(null)
    val nlError: StateFlow<String?> = _nlError.asStateFlow()

    val hasApiKey: Boolean
        get() = apiKeyManager.hasApiKey()

    fun updateAmount(value: String) { _amount.value = value }
    fun toggleType() { _type.value = if (_type.value == BillType.EXPENSE) BillType.INCOME else BillType.EXPENSE }
    fun updateCategory(value: Category) { _category.value = value }
    fun updateMerchant(value: String) { _merchant.value = value }
    fun updateNote(value: String) { _note.value = value }
    fun updateTimestamp(value: Long) { _timestamp.value = value }
    fun updateNlInput(value: String) { _nlInput.value = value }

    fun parseNaturalLanguage() {
        val input = _nlInput.value.trim()
        if (input.isBlank()) return

        _nlParsing.value = true
        _nlError.value = null

        viewModelScope.launch {
            nlBillParser.parse(input)
                .onSuccess { result ->
                    _nlResult.value = NlBillData(
                        amount = nlBillParser.toAmount(result.amount),
                        type = nlBillParser.toBillType(result.type),
                        category = nlBillParser.toCategory(result.category),
                        merchant = result.merchant,
                        description = result.description
                    )
                }
                .onFailure { e ->
                    _nlError.value = e.message ?: "解析失败"
                }
            _nlParsing.value = false
        }
    }

    fun confirmNlBill() {
        val data = _nlResult.value ?: return

        // 填充表单
        _amount.value = String.format("%.2f", data.amount / 100.0)
        _type.value = data.type
        _category.value = data.category
        _merchant.value = data.merchant
        _note.value = data.description

        // 清除 NL 状态
        _nlResult.value = null
        _nlInput.value = ""
    }

    fun dismissNlResult() {
        _nlResult.value = null
    }

    fun clearNlError() {
        _nlError.value = null
    }

    fun saveBill() {
        val amountStr = _amount.value
        if (amountStr.isBlank()) return
        val amountValue = (amountStr.toDoubleOrNull() ?: return) * 100
        if (amountValue <= 0) return

        _saving.value = true
        viewModelScope.launch {
            repository.insertBill(
                Bill(
                    amount = amountValue.toLong(),
                    type = _type.value,
                    category = _category.value,
                    merchant = _merchant.value,
                    description = _note.value,
                    source = PaymentSource.CASH,
                    isAutoRecorded = false,
                    timestamp = _timestamp.value
                )
            )
            _saved.value = true
        }
    }
}
