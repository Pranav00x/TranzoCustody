package com.tranzo.custody.data.repository

import android.content.Context
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.domain.model.BridgePreview
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.SpendableBalance
import com.tranzo.custody.domain.model.Token
import com.tranzo.custody.domain.model.Wallet
import com.tranzo.custody.domain.model.WalletPortfolio
import com.tranzo.custody.domain.repository.WalletRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

data class BackendWalletResponse(
    val userId: String,
    val address: String,
    val network: String,
    val balances: Map<String, String>
)

interface WalletApiService {
    @GET("wallet/{userId}")
    suspend fun getWalletDetails(@Path("userId") userId: String): BackendWalletResponse
}

@Singleton
class WalletRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WalletRepository {

    private val sessionManager = UserSessionManager(context)

    // Using 10.0.2.2 for Android Emulator connecting to localhost backend
    private val apiService = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WalletApiService::class.java)

    private val _spendable = MutableStateFlow(SpendableBalance(amount = 0.0, currency = "USD", lastTopUpTimestamp = 0L))
    
    // Initial empty state (no mocks)
    private val _portfolio = MutableStateFlow(
        WalletPortfolio(
            walletBalanceFiat = 0.0,
            spendableBalanceFiat = 0.0,
            dailyChangePercent = 0.0,
            dailyChangeAmount = 0.0,
            tokens = emptyList(),
            wallets = emptyList()
        )
    )

    private var cachedWalletAddress: String? = null

    override fun getPortfolio(): Flow<WalletPortfolio> = _portfolio.asStateFlow()

    override fun getTokens(): Flow<List<Token>> = MutableStateFlow(_portfolio.value.tokens).asStateFlow()

    override fun getSpendableBalance(): Flow<SpendableBalance> = _spendable.asStateFlow()

    override suspend fun getWalletAddress(chain: Chain): String {
        cachedWalletAddress?.let { return it }
        
        // Fetch or create deterministic wallet address dynamically via Wallet Service
        val userId = getUserId()
        return try {
            val response = apiService.getWalletDetails(userId)
            cachedWalletAddress = response.address
            response.address
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override suspend fun refreshBalances() {
        val userId = getUserId()
        try {
            val response = apiService.getWalletDetails(userId)
            cachedWalletAddress = response.address

            // Map real RPC balances to App Tokens
            val maticBal = response.balances["MATIC"]?.toDoubleOrNull() ?: 0.0
            val usdcBal = response.balances["USDC"]?.toDoubleOrNull() ?: 0.0
            val usdtBal = response.balances["USDT"]?.toDoubleOrNull() ?: 0.0

            // Estimated prices (in production, fetch from CoinGecko/Oracle)
            val maticPrice = 1.05 
            val usdcPrice = 1.00
            val usdtPrice = 1.00

            val tokens = mutableListOf<Token>()
            tokens.add(Token("MATIC", "Polygon", Chain.POLYGON, maticBal, maticBal * maticPrice, 0.0, iconColor = 0xFF8247E5))
            tokens.add(Token("USDC", "USD Coin", Chain.POLYGON, usdcBal, usdcBal * usdcPrice, 0.0, iconColor = 0xFF2775CA))
            tokens.add(Token("USDT", "Tether", Chain.POLYGON, usdtBal, usdtBal * usdtPrice, 0.0, iconColor = 0xFF50AF95))

            val walletBalance = tokens.sumOf { it.fiatValue }

            _portfolio.value = WalletPortfolio(
                walletBalanceFiat = walletBalance,
                spendableBalanceFiat = usdcBal * usdcPrice, // Treat USDC as spendable for card
                dailyChangePercent = 0.0, // Calculate delta if DB history is available
                dailyChangeAmount = 0.0,
                tokens = tokens,
                wallets = listOf(
                    Wallet(response.address, Chain.POLYGON, walletBalance, true)
                )
            )
            
            _spendable.value = SpendableBalance(
                amount = usdcBal * usdcPrice,
                currency = "USD",
                lastTopUpTimestamp = System.currentTimeMillis()
            )

        } catch (e: Exception) {
            e.printStackTrace()
            // If API fails, we do NOT return random data anymore. 
            // The UI will remain handling an empty or previously cached state.
        }
    }

    override suspend fun sendTransaction(
        chain: Chain,
        toAddress: String,
        amount: Double,
        token: Token
    ): Result<String> {
        // Now requires actual integration (e.g. sending a UserOp)
        return Result.failure(Exception("Not implemented yet. Need active RPC bundler transaction."))
    }

    override suspend fun getBridgePreview(token: Token, amount: Double): BridgePreview {
        val fiatAmount = amount * (if(token.balance > 0) token.fiatValue / token.balance else 1.0)
        return BridgePreview(
            fromToken = token,
            fromAmount = amount,
            toAmountFiat = fiatAmount,
            exchangeRate = 1.0,
            networkFee = 0.0,
            platformFee = 0.0,
            estimatedTotal = fiatAmount
        )
    }

    override suspend fun executeTopUp(token: Token, amount: Double): Result<Double> {
        return Result.failure(Exception("Fiat ramp not connected yet."))
    }

    private suspend fun getUserId(): String {
        return try {
            val email = sessionManager.userEmail.first()
            if (email.isNotEmpty()) email else "anonymous_user"
        } catch (e: Exception) {
            "anonymous_user"
        }
    }
}

