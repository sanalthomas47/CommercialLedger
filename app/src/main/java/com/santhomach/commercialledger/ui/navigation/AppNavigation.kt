package com.santhomach.commercialledger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.santhomach.commercialledger.ui.screens.complex.ComplexDetailScreen
import com.santhomach.commercialledger.ui.screens.door.DoorDetailScreen
import com.santhomach.commercialledger.ui.screens.expense.ExpenseFormScreen
import com.santhomach.commercialledger.ui.screens.history.TenancyHistoryScreen
import com.santhomach.commercialledger.ui.screens.home.HomeScreen
import com.santhomach.commercialledger.ui.screens.summary.SummaryScreen
import com.santhomach.commercialledger.ui.screens.tenancy.TenancyFormScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Summary : Screen("summary")

    object ComplexDetail : Screen("complex/{complexId}") {
        fun createRoute(complexId: Long) = "complex/$complexId"
    }

    object DoorDetail : Screen("door/{doorId}") {
        fun createRoute(doorId: Long) = "door/$doorId"
    }

    object TenancyForm : Screen("tenancy_form/{roomId}?tenancyId={tenancyId}") {
        fun createRoute(roomId: Long, tenancyId: Long = 0L) =
            "tenancy_form/$roomId?tenancyId=$tenancyId"
    }

    object TenancyHistory : Screen("tenancy_history/{roomId}") {
        fun createRoute(roomId: Long) = "tenancy_history/$roomId"
    }

    object ExpenseForm : Screen("expense_form/{complexId}?roomId={roomId}&expenseId={expenseId}") {
        fun createRoute(complexId: Long, roomId: Long = 0L, expenseId: Long = 0L) =
            "expense_form/$complexId?roomId=$roomId&expenseId=$expenseId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Summary.route) {
            SummaryScreen(navController = navController)
        }

        composable(
            route = Screen.ComplexDetail.route,
            arguments = listOf(navArgument("complexId") { type = NavType.LongType })
        ) { backStack ->
            val args = backStack.arguments ?: return@composable
            ComplexDetailScreen(
                complexId = args.getLong("complexId"),
                navController = navController
            )
        }

        composable(
            route = Screen.DoorDetail.route,
            arguments = listOf(navArgument("doorId") { type = NavType.LongType })
        ) { backStack ->
            val args = backStack.arguments ?: return@composable
            DoorDetailScreen(
                doorId = args.getLong("doorId"),
                navController = navController
            )
        }

        composable(
            route = Screen.TenancyForm.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.LongType },
                navArgument("tenancyId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStack ->
            val args = backStack.arguments ?: return@composable
            TenancyFormScreen(
                roomId = args.getLong("roomId"),
                tenancyId = args.getLong("tenancyId"),
                navController = navController
            )
        }

        composable(
            route = Screen.TenancyHistory.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStack ->
            val args = backStack.arguments ?: return@composable
            TenancyHistoryScreen(
                roomId = args.getLong("roomId"),
                navController = navController
            )
        }

        composable(
            route = Screen.ExpenseForm.route,
            arguments = listOf(
                navArgument("complexId") { type = NavType.LongType },
                navArgument("roomId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("expenseId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStack ->
            val args = backStack.arguments ?: return@composable
            ExpenseFormScreen(
                complexId = args.getLong("complexId"),
                roomId = args.getLong("roomId"),
                expenseId = args.getLong("expenseId"),
                navController = navController
            )
        }
    }
}
