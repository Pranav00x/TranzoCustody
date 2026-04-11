package com.tranzo.custody.data.mapper

import com.tranzo.custody.data.local.entity.AssetEntity
import com.tranzo.custody.domain.model.Token

fun AssetEntity.toDomain(): Token {
    return Token(
        symbol = symbol,
        name = name,
        chain = chain,
        balance = balance,
        fiatValue = fiatBalance,
        priceChange24h = priceChange24h,
        contractAddress = contractAddress,
        decimals = decimals
    )
}

fun Token.toEntity(price: Double): AssetEntity {
    return AssetEntity(
        symbol = symbol,
        name = name,
        chain = chain,
        balance = balance,
        fiatBalance = fiatValue,
        price = price,
        priceChange24h = priceChange24h,
        contractAddress = contractAddress,
        decimals = decimals
    )
}
