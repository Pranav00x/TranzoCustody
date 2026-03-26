package com.tranzo.custody.data.remote

import com.tranzo.custody.data.remote.dto.BalanceResponseDto
import com.tranzo.custody.data.remote.dto.TokenPriceDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TranzoApi {
    @GET("v1/prices")
    suspend fun getTokenPrices(
        @Query("symbols") symbols: String
    ): List<TokenPriceDto>

    @GET("v1/balances/{address}")
    suspend fun getBalances(
        @Path("address") address: String,
        @Query("chain") chain: String
    ): BalanceResponseDto
}
