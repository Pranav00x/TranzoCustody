package com.tranzo.custody.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tranzo.custody.ui.activity.ActivityScreen
import com.tranzo.custody.ui.activity.TransactionDetailScreen
import com.tranzo.custody.ui.card.CardScreen
import com.tranzo.custody.ui.card.CardSettingsScreen
import com.tranzo.custody.ui.components.TranzoBottomBar
import com.tranzo.custody.ui.home.BridgeScreen
import com.tranzo.custody.ui.home.BuyScreen
import com.tranzo.custody.ui.home.HomeScreen
import com.tranzo.custody.ui.home.ReceiveScreen
import com.tranzo.custody.ui.home.SendScreen
import com.tranzo.custody.ui.home.SwapScreen
import com.tranzo.custody.ui.onboarding.CreateImportScreen
import com.tranzo.custody.ui.onboarding.ImportWalletScreen
import com.tranzo.custody.ui.onboarding.SeedPhraseScreen
import com.tranzo.custody.ui.onboarding.SetPinScreen
import com.tranzo.custody.ui.onboarding.VerifySeedScreen
import com.tranzo.custody.ui.onboarding.WelcomeScreen
import com.tranzo.custody.ui.settings.DripperScreen
import com.tranzo.custody.ui.settings.SecurityScreen
import com.tranzo.custody.ui.settings.SettingsScreen

private val mainTabs = setOf("home", "card", "activity", "settings")

@Composable
fun TranzoNavigation(startDestination: String = Screen.Welcome.route) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in mainTabs

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TranzoBottomBar(
                    currentRoute = currentRoute,
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            // Onboarding
            composable(Screen.Welcome.route) {
                WelcomeScreen(onGetStarted = { navController.navigate(Screen.CreateOrImport.route) })
            }
            composable(Screen.CreateOrImport.route) {
                CreateImportScreen(
                    onCreateWallet = { navController.navigate(Screen.SeedPhrase.route) },
                    onImportWallet = { navController.navigate(Screen.ImportWallet.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.SeedPhrase.route) {
                SeedPhraseScreen(
                    onContinue = { navController.navigate(Screen.VerifySeed.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.VerifySeed.route) {
                VerifySeedScreen(
                    onVerified = { navController.navigate(Screen.SetPin.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ImportWallet.route) {
                ImportWalletScreen(
                    onImported = { navController.navigate(Screen.SetPin.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.SetPin.route) {
                SetPinScreen(
                    onPinSet = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Main tabs
            composable(Screen.Home.route) {
                HomeScreen(
                    onSendClick = { navController.navigate(Screen.Send.route) },
                    onReceiveClick = { navController.navigate(Screen.Receive.route) },
                    onSwapClick = { navController.navigate(Screen.Swap.route) },
                    onBuyClick = { navController.navigate(Screen.Buy.route) },
                    onAddToSpend = { navController.navigate(Screen.Bridge.route) }
                )
            }
            composable(Screen.Card.route) {
                CardScreen(
                    onCardSettings = { navController.navigate(Screen.CardSettings.route) },
                    onAddFunds = { navController.navigate(Screen.Bridge.route) }
                )
            }
            composable(Screen.Activity.route) {
                ActivityScreen(
                    onTransactionClick = { txId ->
                        navController.navigate(Screen.TransactionDetail.createRoute(txId))
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onSecurityClick = { navController.navigate(Screen.Security.route) },
                    onDripperClick = { navController.navigate(Screen.DripperDevices.route) },
                    onCardSettingsClick = { navController.navigate(Screen.CardSettings.route) }
                )
            }

            // Sub screens
            composable(Screen.Send.route) {
                SendScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Receive.route) {
                ReceiveScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Swap.route) {
                SwapScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Buy.route) {
                BuyScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Bridge.route) {
                BridgeScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.CardSettings.route) {
                CardSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.TransactionDetail.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
                TransactionDetailScreen(
                    transactionId = txId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Security.route) {
                SecurityScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.DripperDevices.route) {
                DripperScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
