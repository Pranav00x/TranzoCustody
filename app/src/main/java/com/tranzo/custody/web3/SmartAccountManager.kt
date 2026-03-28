package com.tranzo.custody.web3

import com.tranzo.custody.BuildConfig
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartAccountManager @Inject constructor(
    private val web3j: Web3j
) {
    private val factoryAddress: String = BuildConfig.ACCOUNT_FACTORY_ADDRESS
    private val entryPoint = "0x0000000071727De22E5E9d8BAf0edAc6f37da032"

    companion object {
        private const val RETRIES_PER_URL = 2
    }

    fun getEntryPointAddress(): String = entryPoint

    /**
     * On-chain counterfactual address from TranzoAccountFactory.getAddress(owner, salt).
     * Tries [AmoyRpcConfig.endpointUrls] with limited retries per URL.
     */
    fun computeCounterfactualAddress(owner: String, salt: BigInteger): String {
        val http = AmoyRpcConfig.httpClient()
        var last: Throwable? = null
        for (url in AmoyRpcConfig.endpointUrls()) {
            repeat(RETRIES_PER_URL) { attempt ->
                val w3 = Web3j.build(HttpService(url, http))
                try {
                    return ethCallGetAddress(w3, owner, salt)
                } catch (e: Throwable) {
                    last = e
                } finally {
                    w3.shutdown()
                }
                if (attempt < RETRIES_PER_URL - 1) {
                    try {
                        Thread.sleep(350L)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            }
        }
        val detail = last?.message?.takeIf { it.isNotBlank() } ?: last?.javaClass?.simpleName
        val hint =
            if (BuildConfig.AMOY_RPC_URL.isBlank()) {
                " Try setting BASE_RPC_URL in local.properties to an Alchemy/Infura Base HTTPS URL."
            } else {
                ""
            }
        throw IllegalStateException(
            "Could not reach Base to compute your smart wallet. Check internet or VPN.$hint${
                detail?.let { " ($it)" } ?: ""
            }",
            last
        )
    }

    private fun ethCallGetAddress(w3: Web3j, owner: String, salt: BigInteger): String {
        val function = Function(
            "getAddress",
            listOf(Address(owner), Uint256(salt)),
            listOf(object : TypeReference<Address>() {})
        )
        val encoded = FunctionEncoder.encode(function)
        val response = w3.ethCall(
            Transaction.createEthCallTransaction(null, factoryAddress, encoded),
            DefaultBlockParameterName.LATEST
        ).send()
        if (response.hasError()) {
            throw IllegalStateException(response.error?.message ?: "eth_call failed")
        }
        val value = response.value ?: throw IllegalStateException("empty eth_call result")
        val decoded = FunctionReturnDecoder.decode(value, function.outputParameters)
        if (decoded.isEmpty()) throw IllegalStateException("could not decode factory getAddress")
        return (decoded[0].value as String).lowercase()
    }

    fun getNonce(sender: String): BigInteger {
        val function = Function(
            "getNonce",
            listOf(Address(sender), Uint256(BigInteger.ZERO)),
            listOf(object : TypeReference<Uint256>() {})
        )
        val encoded = FunctionEncoder.encode(function)
        val response = web3j.ethCall(
            Transaction.createEthCallTransaction(null, entryPoint, encoded),
            DefaultBlockParameterName.LATEST
        ).send()
        if (response.hasError() || response.value == null) return BigInteger.ZERO
        val decoded = FunctionReturnDecoder.decode(response.value, function.outputParameters)
        if (decoded.isEmpty()) return BigInteger.ZERO
        return decoded[0].value as BigInteger
    }
}
