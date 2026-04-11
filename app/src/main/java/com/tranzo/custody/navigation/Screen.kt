package com.tranzo.custody.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object VerifyPin : Screen("verify_pin")

    /** Nested graph: shared [com.tranzo.custody.ui.onboarding.OnboardingViewModel] for email → create → verify → PIN → backup. */
    data object OnboardingCreateGraph : Screen("onboarding_create")

    /** Nested graph: shared OnboardingViewModel for email → import → PIN → backup. */
    data object OnboardingImportGraph : Screen("onboarding_import")

    data object EmailPassword : Screen("email_password")
    data object CreateWallet : Screen("create_wallet")
    data object VerifySeed : Screen("verify_seed")
    data object ImportWallet : Screen("import_wallet")
    data object SetPin : Screen("set_pin")
    data object RestoreSetPin : Screen("restore_set_pin")
    data object BackupPrompt : Screen("backup_prompt")

    data object Home : Screen("home")
    data object Card : Screen("card")
    data object Activity : Screen("activity")
    data object Settings : Screen("settings")

    data object Send : Screen("send")
    data object Receive : Screen("receive")
    data object Swap : Screen("swap")
    data object Buy : Screen("buy")
    data object Bridge : Screen("bridge")

    data object CardSettings : Screen("card_settings")
    data object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }

    data object Appearance : Screen("appearance")
    data object Security : Screen("security")
    data object HelpSupport : Screen("help_support")
    data object DripperDevices : Screen("dripper_devices")
}
