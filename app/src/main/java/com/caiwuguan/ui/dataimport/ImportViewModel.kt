package com.caiwuguan.ui.dataimport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.data.importer.AlipayCsvImporter
import com.caiwuguan.data.importer.ImportResult
import com.caiwuguan.data.importer.WechatCsvImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ImportSource {
    WECHAT,
    ALIPAY
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val wechatCsvImporter: WechatCsvImporter,
    private val alipayCsvImporter: AlipayCsvImporter
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun importFile(context: Context, uri: Uri, source: ImportSource) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _importState.value = ImportState.Error("无法读取文件")
                    return@launch
                }

                val result = when (source) {
                    ImportSource.WECHAT -> wechatCsvImporter.import(inputStream)
                    ImportSource.ALIPAY -> alipayCsvImporter.import(inputStream)
                }

                inputStream.close()

                _importState.value = when (result) {
                    is ImportResult.Success -> ImportState.Success(
                        importedCount = result.importedCount,
                        duplicateCount = result.duplicateCount,
                        failedCount = result.failedCount
                    )
                    is ImportResult.Error -> ImportState.Error(result.message)
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error("导入失败：${e.message}")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }
}

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(
        val importedCount: Int,
        val duplicateCount: Int,
        val failedCount: Int
    ) : ImportState()
    data class Error(val message: String) : ImportState()
}
