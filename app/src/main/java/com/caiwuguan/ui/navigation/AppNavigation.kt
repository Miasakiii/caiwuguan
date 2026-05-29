package com.caiwuguan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.caiwuguan.ui.ai.AiChatScreen
import com.caiwuguan.ui.bill.AddBillScreen
import com.caiwuguan.ui.bill.BillEditScreen
import com.caiwuguan.ui.bill.BillListScreen
import com.caiwuguan.ui.budget.BudgetScreen
import com.caiwuguan.ui.export.ExportScreen
import com.caiwuguan.ui.home.HomeScreen
import com.caiwuguan.ui.dataimport.ImportScreen
import com.caiwuguan.ui.ledger.LedgerScreen
import com.caiwuguan.ui.search.SearchScreen
import com.caiwuguan.ui.settings.ApiKeyScreen
import com.caiwuguan.ui.settings.SettingsScreen
import com.caiwuguan.ui.stats.StatsScreen

@Composable
fun AppNavigation(navController: NavHostController, bottomPadding: Dp = 0.dp) {
    NavHost(navController = navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            HomeScreen(navController, bottomPadding)
        }
        composable(NavRoutes.BILL_LIST) {
            BillListScreen(navController, bottomPadding)
        }
        composable(NavRoutes.ADD_BILL) {
            AddBillScreen(navController)
        }
        composable(
            route = NavRoutes.EDIT_BILL,
            arguments = listOf(navArgument("billId") { type = NavType.LongType })
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getLong("billId") ?: return@composable
            BillEditScreen(navController, billId)
        }
        composable(NavRoutes.STATS) {
            StatsScreen(navController, bottomPadding)
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController, bottomPadding)
        }
        composable(NavRoutes.BUDGET) {
            BudgetScreen(bottomPadding)
        }
        composable(NavRoutes.IMPORT) {
            ImportScreen(navController)
        }
        composable(NavRoutes.EXPORT) {
            ExportScreen(navController)
        }
        composable(NavRoutes.SEARCH) {
            SearchScreen(navController)
        }
        composable(NavRoutes.LEDGER) {
            LedgerScreen(navController)
        }
        composable(NavRoutes.API_KEY) {
            ApiKeyScreen(navController)
        }
        composable(NavRoutes.AI_CHAT) {
            AiChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
