package com.sb.mycriptoanalisi.data

data class CoinGeckoCoin(
    val id: String,       // es. "bitcoin"
    val symbol: String,   // es. "btc"
    val name: String,     // es. "Bitcoin"
    val currentPriceEur: Double? = null,
    val currentPriceUsd: Double? = null,
)