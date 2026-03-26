package com.tranzo.custody.data.repository

import com.tranzo.custody.domain.model.BridgePreview
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.SpendableBalance
import com.tranzo.custody.domain.model.Token
import com.tranzo.custody.domain.model.Wallet
import com.tranzo.custody.domain.model.WalletPortfolio
import com.tranzo.custody.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor() : WalletRepository {

    private val _portfolio = MutableStateFlow(getMockPortfolio())
    private val _spendable = MutableStateFlow(SpendableBalance(amount = 420.50, currency = "USD", lastTopUpTimestamp = System.currentTimeMillis() - 86400000))

    override fun getPortfolio(): Flow<WalletPortfolio> = _portfolio.asStateFlow()

    override fun getTokens(): Flow<List<Token>> {
        return MutableStateFlow(_portfolio.value.tokens).asStateFlow()
    }

    override fun getSpendableBalance(): Flow<SpendableBalance> = _spendable.asStateFlow()

    override suspend fun getWalletAddress(chain: Chain): String {
        return when (chain) {
            Chain.ETHEREUM -> "0x742d35Cc6634C0532925a3b844Bc9e7595f2bD18"
            Chain.BITCOIN -> "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"
            Chain.SOLANA -> "7EcDhSYGxXyscszYEp35KHN8vvw3svAuLKTzXwCFLtV"
            Chain.POLYGON -> "0x742d35Cc6634C0532925a3b844Bc9e7595f2bD18"
            Chain.ARBITRUM -> "0x742d35Cc6634C0532925a3b844Bc9e7595f2bD18"
            Chain.BASE -> "0x742d35Cc6634C0532925a3b844Bc9e7595f2bD18"
        }
    }

    override suspend fun refreshBalances() {
        _portfolio.value = getMockPortfolio()
    }

    override suspend fun sendTransaction(
        chain: Chain,
        toAddress: String,
        amount: Double,
        token: Token
    ): Result<String> {
        return Result.success("0xmocktxhash123456789abcdef")
    }

    override suspend fun getBridgePreview(token: Token, amount: Double): BridgePreview {
        val rate = if (token.balance > 0) token.fiatValue / token.balance else 0.0
        val fiatAmount = amount * rate
        val networkFee = if (token.chain == Chain.ETHEREUM) 2.40 else 0.15
        val platformFee = fiatAmount * 0.005
        return BridgePreview(
            fromToken = token,
            fromAmount = amount,
            toAmountFiat = fiatAmount - networkFee - platformFee,
            exchangeRate = rate,
            networkFee = networkFee,
            platformFee = platformFee,
            estimatedTotal = fiatAmount - networkFee - platformFee
        )
    }

    override suspend fun executeTopUp(token: Token, amount: Double): Result<Double> {
        val preview = getBridgePreview(token, amount)
        val newSpendable = _spendable.value.amount + preview.estimatedTotal
        _spendable.value = SpendableBalance(
            amount = newSpendable,
            currency = "USD",
            lastTopUpTimestamp = System.currentTimeMillis()
        )
        _portfolio.value = _portfolio.value.copy(spendableBalanceFiat = newSpendable)
        return Result.success(preview.estimatedTotal)
    }

    private fun getMockPortfolio(): WalletPortfolio {
        val tokens = listOf(
            Token("ETH", "Ethereum", Chain.ETHEREUM, 2.4518, 7834.52, 3.24, iconColor = 0xFF627EEA),
            Token("BTC", "Bitcoin", Chain.BITCOIN, 0.1205, 12458.30, -1.15, iconColor = 0xFFF7931A),
            Token("SOL", "Solana", Chain.SOLANA, 45.8, 6234.80, 5.67, iconColor = 0xFF9945FF),
            Token("MATIC", "Polygon", Chain.POLYGON, 2500.0, 1125.00, -0.43, iconColor = 0xFF8247E5),
            Token("USDC", "USD Coin", Chain.ETHEREUM, 3200.0, 3200.00, 0.01, iconColor = 0xFF2775CA),
            Token("ARB", "Arbitrum", Chain.ARBITRUM, 850.0, 595.00, 2.18, iconColor = 0xFF28A0F0),
            Token("LINK", "Chainlink", Chain.ETHEREUM, 120.0, 1680.00, -2.35, iconColor = 0xFF2A5ADA),
            Token("UNI", "Uniswap", Chain.ETHEREUM, 200.0, 1340.00, 1.92, iconColor = 0xFFFF007A)
        )
        val walletBalance = tokens.sumOf { it.fiatValue }
        return WalletPortfolio(
            walletBalanceFiat = walletBalance,
            spendableBalanceFiat = _spendable.value.amount,
            dailyChangePercent = 2.41,
            dailyChangeAmount = walletBalance * 0.0241,
            tokens = tokens,
            wallets = listOf(
                Wallet("0x742d35Cc6634C0532925a3b844Bc9e7595f2bD18", Chain.ETHEREUM, walletBalance, true)
            )
        )
    }
}
