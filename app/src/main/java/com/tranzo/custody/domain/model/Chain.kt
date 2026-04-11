package com.tranzo.custody.domain.model

enum class Chain(
    val displayName: String,
    val symbol: String,
    val iconLetter: String,
    val chainId: Int
) {
    ETHEREUM("Ethereum", "ETH", "Ξ", 1),
    BITCOIN("Bitcoin", "BTC", "₿", 0),
    SOLANA("Solana", "SOL", "◎", -1),
    POLYGON("Polygon", "POL", "P", 137),
    ARBITRUM("Arbitrum", "ARB", "A", 42161),
    BASE("Base", "BASE", "B", 8453),
    BNB("BNB Chain", "BNB", "B", 56),
    AVALANCHE("Avalanche", "AVAX", "A", 43114),
    OPTIMISM("Optimism", "OP", "O", 10),
    ZKSYNC("zkSync", "ZK", "Z", 324),
    MANTLE("Mantle", "MNT", "M", 5000),
    BLAST("Blast", "BLAST", "B", 81457)
}
