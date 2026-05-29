package com.caiwuguan.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val BILL_LIST = "bill_list"
    const val ADD_BILL = "add_bill"
    const val EDIT_BILL = "edit_bill/{billId}"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val BUDGET = "budget"
    const val IMPORT = "import"
    const val EXPORT = "export"
    const val SEARCH = "search"
    const val LEDGER = "ledger"
    const val API_KEY = "api_key"
    const val AI_CHAT = "ai_chat"

    fun editBill(billId: Long) = "edit_bill/$billId"
}
