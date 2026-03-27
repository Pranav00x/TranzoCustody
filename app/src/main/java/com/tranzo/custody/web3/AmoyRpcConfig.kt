package com.tranzo.custody.web3

import com.tranzo.custody.BuildConfig
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Polygon Amoy (80002) JSON-RPC endpoints. Public RPCs are often rate-limited or block bare OkHttp;
 * we set a browser-like User-Agent and try several URLs. Override [BuildConfig.AMOY_RPC_URL] with
 * Alchemy/Infura/etc. if your network blocks public endpoints.
 */
object AmoyRpcConfig {

    private val defaultEndpoints = listOf(
        "https://polygon-amoy-bor-rpc.publicnode.com",
        "https://rpc-amoy.polygon.technology",
        "https://polygon-amoy.drpc.org",
        "https://rpc.ankr.com/polygon_amoy",
        "https://80002.rpc.thirdweb.com"
    )

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "TranzoCustody/1.0 (Android)")
                    .header("Accept", "application/json, text/plain, */*")
                    .build()
                chain.proceed(req)
            }
            .build()
    }

    fun httpClient(): OkHttpClient = client

    /** Custom URL first (if set in Gradle), then defaults, deduped. */
    fun endpointUrls(): List<String> {
        val custom = BuildConfig.AMOY_RPC_URL.trim().trimEnd('/')
        if (custom.isEmpty()) return defaultEndpoints
        return listOf(custom) + defaultEndpoints.filter {
            !it.equals(custom, ignoreCase = true)
        }
    }

    fun primaryUrl(): String = endpointUrls().first()
}
