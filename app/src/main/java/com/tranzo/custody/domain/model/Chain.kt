package com.tranzo.custody.domain.model

enum class Chain(
    val displayName: String,
    val symbol: String,
    val iconLetter: String,
    val chainId: Int
) {
    ETHEREUM("Ethereum", "ETH", "E", 1),
    BITCOIN("Bitcoin", "BTC", "B", 0),
    SOLANA("Solana", "SOL", "S", -1),
    POLYGON("Polygon", "MATIC", "P", 137),
    ARBITRUM("Arbitrum", "ARB", "A", 42161),
    BASE("Base", "BASE", "B", 8453)
}
