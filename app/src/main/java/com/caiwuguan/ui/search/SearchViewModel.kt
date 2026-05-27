package com.caiwuguan.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.data.db.dao.BillDao
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val billDao: BillDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Bill>>(emptyList())
    val searchResults: StateFlow<List<Bill>> = _searchResults.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedType = MutableStateFlow<BillType?>(null)
    val selectedType: StateFlow<BillType?> = _selectedType.asStateFlow()

    private val _minAmount = MutableStateFlow<Long?>(null)
    val minAmount: StateFlow<Long?> = _minAmount.asStateFlow()

    private val _maxAmount = MutableStateFlow<Long?>(null)
    val maxAmount: StateFlow<Long?> = _maxAmount.asStateFlow()

    init {
        // 监听搜索查询变化
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // 防抖
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        flowOf(emptyList())
                    } else {
                        billDao.search(query)
                    }
                }
                .collect { results ->
                    _searchResults.value = results.map { entity ->
                        Bill(
                            id = entity.id,
                            amount = entity.amount,
                            type = BillType.valueOf(entity.type),
                            category = Category.valueOf(entity.category),
                            merchant = entity.merchant,
                            description = entity.description,
                            source = com.caiwuguan.domain.model.PaymentSource.valueOf(entity.source),
                            transactionId = entity.transactionId,
                            notificationText = entity.notificationText,
                            isAutoRecorded = entity.isAutoRecorded,
                            timestamp = entity.timestamp
                        )
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun selectType(type: BillType?) {
        _selectedType.value = type
    }

    fun updateMinAmount(amount: Long?) {
        _minAmount.value = amount
    }

    fun updateMaxAmount(amount: Long?) {
        _maxAmount.value = amount
    }

    fun clearFilters() {
        _selectedCategory.value = null
        _selectedType.value = null
        _minAmount.value = null
        _maxAmount.value = null
    }
}
