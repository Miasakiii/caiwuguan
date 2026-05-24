package com.caiwuguan.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val BILL_LIST = "bill_list"
    const val ADD_BILL = "add_bill"
    const val EDIT_BILL = "edit_bill/{billId}"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    fun editBill(billId: Long) = "edit_bill/$billId"
}
