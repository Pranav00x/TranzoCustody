package com.tranzo.custody.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object CreateOrImport : Screen("create_or_import")
    data object SeedPhrase : Screen("seed_phrase")
    data object VerifySeed : Screen("verify_seed")
    data object ImportWallet : Screen("import_wallet")
    data object SetPin : Screen("set_pin")

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

    data object Security : Screen("security")
    data object DripperDevices : Screen("dripper_devices")
}
