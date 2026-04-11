package com.tranzo.custody.data.local

import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRegistry @Inject constructor() {
    
    fun getSupportedDepositTokens(): List<Token> {
        return listOf(
            // Ethereum
            Token("ETH", "Ethereum", Chain.ETHEREUM, 0.0, 0.0, 0.0),
            Token("USDC", "USD Coin", Chain.ETHEREUM, 0.0, 1.0, 0.0),
            Token("USDT", "Tether", Chain.ETHEREUM, 0.0, 1.0, 0.0),
            Token("DAI", "Dai Stablecoin", Chain.ETHEREUM, 0.0, 1.0, 0.0),
            
            // Polygon
            Token("POL", "Polygon", Chain.POLYGON, 0.0, 0.0, 0.0),
            Token("USDC", "USD Coin", Chain.POLYGON, 0.0, 1.0, 0.0),
            Token("USDT", "Tether", Chain.POLYGON, 0.0, 1.0, 0.0),
            
            // Base
            Token("ETH", "Ethereum", Chain.BASE, 0.0, 0.0, 0.0),
            Token("USDC", "USD Coin", Chain.BASE, 0.0, 1.0, 0.0),
            Token("CBETH", "Coinbase Wrapped Staked ETH", Chain.BASE, 0.0, 0.0, 0.0),
            
            // Arbitrum
            Token("ETH", "Ethereum", Chain.ARBITRUM, 0.0, 0.0, 0.0),
            Token("ARB", "Arbitrum", Chain.ARBITRUM, 0.0, 0.0, 0.0),
            Token("USDC", "USD Coin", Chain.ARBITRUM, 0.0, 1.0, 0.0),
            
            // BNB Chain
            Token("BNB", "BNB", Chain.BNB, 0.0, 0.0, 0.0),
            Token("BUSD", "Binance USD", Chain.BNB, 0.0, 1.0, 0.0),
            Token("USDT", "Tether", Chain.BNB, 0.0, 1.0, 0.0),
            
            // Avalanche
            Token("AVAX", "Avalanche", Chain.AVALANCHE, 0.0, 0.0, 0.0),
            Token("USDC", "USD Coin", Chain.AVALANCHE, 0.0, 1.0, 0.0),
            
            // Optimism
            Token("ETH", "Ethereum", Chain.OPTIMISM, 0.0, 0.0, 0.0),
            Token("OP", "Optimism", Chain.OPTIMISM, 0.0, 0.0, 0.0),
            
            // zkSync
            Token("ETH", "Ethereum", Chain.ZKSYNC, 0.0, 0.0, 0.0),
            Token("ZK", "zkSync", Chain.ZKSYNC, 0.0, 0.0, 0.0),
            
            // Mantle
            Token("MNT", "Mantle", Chain.MANTLE, 0.0, 0.0, 0.0),
            
            // Blast
            Token("ETH", "Ethereum", Chain.BLAST, 0.0, 0.0, 0.0)
        )
    }

    fun getTokensForChain(chain: Chain): List<Token> {
        return getSupportedDepositTokens().filter { it.chain == chain }
    }
}
