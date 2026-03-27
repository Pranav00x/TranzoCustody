package com.tranzo.custody.data.repository

import com.tranzo.custody.BuildConfig
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.data.remote.SendUserOpRequest
import com.tranzo.custody.data.remote.TranzoApi
import com.tranzo.custody.data.remote.WalletBackendApi
import com.tranzo.custody.domain.model.BridgePreview
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.SpendableBalance
import com.tranzo.custody.domain.model.Token
import com.tranzo.custody.domain.model.Wallet
import com.tranzo.custody.domain.model.WalletPortfolio
import com.tranzo.custody.domain.repository.WalletRepository
import com.tranzo.custody.web3.SigningManager
import com.tranzo.custody.web3.SmartAccountManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val signingManager: SigningManager,
    private val smartAccountManager: SmartAccountManager,
    private val walletBackendApi: WalletBackendApi,
    private val tranzoApi: TranzoApi,
    private val web3j: Web3j
) : WalletRepository {

    companion object {
        private const val DEFAULT_SALT = 1L
    }

    private val _spendable = MutableStateFlow(SpendableBalance(0.0, "USD", 0L))
    private val _portfolio = MutableStateFlow(
        WalletPortfolio(0.0, 0.0, 0.0, 0.0, emptyList(), emptyList())
    )

    override fun getPortfolio(): Flow<WalletPortfolio> = _portfolio.asStateFlow()
    override fun getTokens(): Flow<List<Token>> = MutableStateFlow(_portfolio.value.tokens).asStateFlow()
    override fun getSpendableBalance(): Flow<SpendableBalance> = _spendable.asStateFlow()

    override suspend fun getWalletAddress(chain: Chain): String {
        val saved = sessionManager.getSmartWalletAddress()
        if (saved.isNotEmpty()) return saved
        val creds = signingManager.loadCredentials() ?: return ""
        return try {
            smartAccountManager.computeCounterfactualAddress(creds.address, BigInteger.valueOf(DEFAULT_SALT))
        } catch (_: Exception) {
            ""
        }
    }

    override suspend fun refreshBalances() {
        val addr = sessionManager.getSmartWalletAddress()
        if (addr.isEmpty()) return
        val chainId = sessionManager.getChainId()
        val chain = chainFromId(chainId)
        try {
            val dto = tranzoApi.getBalances(addr, chain.toApiSlug())
            val tokens = dto.balances.map { b ->
                val bal = b.balance.toDoubleOrNull() ?: 0.0
                Token(
                    symbol = b.symbol,
                    name = b.symbol,
                    chain = chain,
                    balance = bal,
                    fiatValue = bal,
                    priceChange24h = 0.0,
                    decimals = b.decimals,
                    iconColor = 0xFF8247E5
                )
            }
            val totalFiat = tokens.sumOf { it.fiatValue }
            _portfolio.value = WalletPortfolio(
                walletBalanceFiat = totalFiat,
                spendableBalanceFiat = _portfolio.value.spendableBalanceFiat,
                dailyChangePercent = 0.0,
                dailyChangeAmount = 0.0,
                tokens = tokens,
                wallets = listOf(Wallet(addr, chain, totalFiat, true))
            )
        } catch (_: Exception) {
            val wei = web3j.ethGetBalance(addr, DefaultBlockParameterName.LATEST).send().balance
            val native = Convert.fromWei(BigDecimal(wei), Convert.Unit.ETHER).toDouble()
            val symbol = if (chainId == 80002 || chainId == 137) "MATIC" else "ETH"
            val token = Token(
                symbol = symbol,
                name = symbol,
                chain = chain,
                balance = native,
                fiatValue = native,
                priceChange24h = 0.0,
                decimals = 18,
                iconColor = 0xFF8247E5
            )
            _portfolio.value = WalletPortfolio(
                walletBalanceFiat = native,
                spendableBalanceFiat = 0.0,
                dailyChangePercent = 0.0,
                dailyChangeAmount = 0.0,
                tokens = listOf(token),
                wallets = listOf(Wallet(addr, chain, native, true))
            )
        }
    }

    override suspend fun sendTransaction(
        chain: Chain,
        toAddress: String,
        amount: Double,
        token: Token
    ): Result<String> {
        signingManager.loadCredentials()
            ?: return Result.failure(IllegalStateException("Wallet locked or not initialized"))
        val chainId = sessionManager.getChainId().takeIf { it > 0 } ?: BuildConfig.DEFAULT_CHAIN_ID
        return try {
            val stub = mapOf(
                "sender" to sessionManager.getSmartWalletAddress(),
                "nonce" to "0",
                "callData" to "0x",
                "signature" to "0x"
            )
            val res = walletBackendApi.sendUserOperation(SendUserOpRequest(chainId, stub))
            val h = res.hash
            if (!h.isNullOrBlank()) Result.success(h)
            else Result.failure(Exception(res.error ?: "Bundler rejected UserOperation"))
        } catch (e: Exception) {
            Result.failure(Exception("Send via smart account requires a deployed account and valid UserOp. ${e.message}", e))
        }
    }

    override suspend fun getBridgePreview(token: Token, amount: Double): BridgePreview {
        return BridgePreview(token, amount, amount, 1.0, 0.01, 0.0, amount)
    }

    override suspend fun executeTopUp(token: Token, amount: Double): Result<Double> {
        return Result.failure(Exception("Top-up requires card or exchange integration."))
    }

    private fun chainFromId(chainId: Int): Chain = when (chainId) {
        137, 80002 -> Chain.POLYGON
        8453, 84532 -> Chain.BASE
        1 -> Chain.ETHEREUM
        42161 -> Chain.ARBITRUM
        else -> Chain.POLYGON
    }

    private fun Chain.toApiSlug(): String = when (this) {
        Chain.POLYGON -> "polygon"
        Chain.BASE -> "base"
        Chain.ETHEREUM -> "ethereum"
        Chain.ARBITRUM -> "arbitrum"
        else -> "polygon"
    }
}

