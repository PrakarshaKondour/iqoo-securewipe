package com.wipeproof.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wipeproof.app.ui.assess.AssessScreen
import com.wipeproof.app.ui.demo.DemoScreen
import com.wipeproof.app.ui.handover.HandoverScreen
import com.wipeproof.app.ui.history.HistoryScreen
import com.wipeproof.app.ui.home.HomeScreen
import com.wipeproof.app.ui.identify.IdentifyScreen
import com.wipeproof.app.ui.navigation.Routes
import com.wipeproof.app.ui.prove.ProveScreen
import com.wipeproof.app.ui.sanitize.SanitizeScreen
import com.wipeproof.app.ui.scanner.ScannerScreen
import com.wipeproof.app.ui.verify.VerifyScreen

@Composable
fun WipeProofApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onStartSanitization = { navController.navigate(Routes.IDENTIFY) },
                onVerifyCertificate = { navController.navigate(Routes.SCANNER) },
                onStartDemo = { navController.navigate(Routes.DEMO) },
                onViewHistory = { navController.navigate(Routes.HISTORY) },
                onCaseSelected = { caseId -> navController.navigate(Routes.handover(caseId)) }
            )
        }

        composable(Routes.IDENTIFY) {
            IdentifyScreen(
                onCaseCreated = { caseId -> navController.navigate(Routes.assess(caseId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ASSESS,
            arguments = listOf(navArgument("caseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
            AssessScreen(
                caseId = caseId,
                onProceedToSanitize = { cId -> navController.navigate(Routes.sanitize(cId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SANITIZE,
            arguments = listOf(navArgument("caseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
            SanitizeScreen(
                caseId = caseId,
                onSanitizationDone = { cId -> navController.navigate(Routes.verify(cId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.VERIFY,
            arguments = listOf(navArgument("caseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
            VerifyScreen(
                caseId = caseId,
                onGenerateCertificate = { cId -> navController.navigate(Routes.prove(cId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PROVE,
            arguments = listOf(navArgument("caseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
            ProveScreen(
                caseId = caseId,
                onProceedToHandover = { cId -> navController.navigate(Routes.handover(cId)) },
                onOpenVerifier = { navController.navigate(Routes.SCANNER) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.HANDOVER,
            arguments = listOf(navArgument("caseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
            HandoverScreen(
                caseId = caseId,
                onBackHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SCANNER) {
            ScannerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onCaseSelected = { caseId -> navController.navigate(Routes.handover(caseId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DEMO) {
            DemoScreen(
                onExitDemo = { navController.popBackStack() }
            )
        }
    }
}
