package com.caiwuguan.data.importer

import com.caiwuguan.domain.model.Bill

/**
 * 账单导入接口
 */
interface Importer {
    /**
     * 导入账单
     * @param inputStream 输入流
     * @return 导入结果
     */
    suspend fun import(inputStream: java.io.InputStream): ImportResult
}

/**
 * 导入结果
 */
sealed class ImportResult {
    data class Success(
        val importedCount: Int,
        val duplicateCount: Int,
        val failedCount: Int
    ) : ImportResult()

    data class Error(val message: String) : ImportResult()
}
