package com.tranzo.custody.data.repository

import android.content.Context
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.domain.model.*
import com.tranzo.custody.domain.repository.WalletRepository
import com.tranzo.custody.web3.SmartAccountManager
import com.tranzo.custody.web3.SigningManager
import com.tranzo.custody.web3.UserOperationBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

data class CreateWalletRequest(val owner: String, val salt: Long, val chainId: Int)
data class CreateWalletResponse(val smartWalletAddr: String, val ownerAddr: String)

interface WalletApiService {
    @GET("wallet/details/{userId}")
    suspend fun getWalletDetails(@Path("userId") userId: String): BackendWalletResponse

    @POST("wallet/create")
    suspend fun registerWallet(@Body request: CreateWalletRequest): CreateWalletResponse
}

@Singleton
class WalletRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smartAccountManager: SmartAccountManager,
    private val signingManager: SigningManager,
    private val opBuilder: UserOperationBuilder
) : WalletRepository {

    private val sessionManager = UserSessionManager(context)
    private val apiService = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WalletApiService::class.java)

    private val _spendable = MutableStateFlow(SpendableBalance(0.0, "USD", 0L))
    private val _portfolio = MutableStateFlow(WalletPortfolio(0.0, 0.0, 0.0, 0.0, emptyList(), emptyList()))

    private var cachedWalletAddress: String? = null

    override fun getPortfolio(): Flow<WalletPortfolio> = _portfolio.asStateFlow()
    override fun getTokens(): Flow<List<Token>> = MutableStateFlow(_portfolio.value.tokens).asStateFlow()
    override fun getSpendableBalance(): Flow<SpendableBalance> = _spendable.asStateFlow()

    override suspend fun getWalletAddress(chain: Chain): String {
        cachedWalletAddress?.let { return it }

        val owner = "0x..." // Fetch from Android Keystore decrypted storage
        val salt = 1L // Can be constant or incrementing

        return try {
            // Predict local, verify with backend
            val counterfactual = smartAccountManager.computeCounterfactualAddress(owner, BigInteger.valueOf(salt))
            val response = apiService.registerWallet(CreateWalletRequest(owner, salt, 137))
            cachedWalletAddress = response.smartWalletAddr
            response.smartWalletAddr
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override suspend fun refreshBalances() {
        val userId = getUserId()
        try {
            val response = apiService.getWalletDetails(userId)
            // ... Balance mapping logic from response
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun sendTransaction(
        chain: Chain,
        toAddress: String,
        amount: Double,
        token: Token
    ): Result<String> {
        // Implement UserOp submission flow
        return Result.failure(Exception("ERC-4337 submission enabled. Signing UserOperation..."))
    }

    override suspend fun getBridgePreview(token: Token, amount: Double): BridgePreview {
        return BridgePreview(token, amount, amount, 1.0, 0.01, 0.0, amount)
    }

    override suspend fun executeTopUp(token: Token, amount: Double): Result<Double> {
        return Result.failure(Exception("Top-up requires direct card processor integration."))
    }

    private suspend fun getUserId(): String = sessionManager.userEmail.first()
}
