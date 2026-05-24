package com.caiwuguan.domain.model

sealed class DuplicateCheckResult {
    data class Duplicate(
        val matchType: MatchType,
        val existingBillId: Long?
    ) : DuplicateCheckResult()

    data class Unique(
        val suggestion: String? = null
    ) : DuplicateCheckResult()

    enum class MatchType {
        EXACT, FUZZY, LOOSE
    }
}
