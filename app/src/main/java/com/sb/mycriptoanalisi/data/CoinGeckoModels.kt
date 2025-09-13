package com.sb.mycriptoanalisi.data

// Risposta API di esempio per /simple/price
data class PriceResponse(
    val bitcoin: CoinPrice? = null,
    val ethereum: CoinPrice? = null,
)

data class CoinPrice(
    val eur: Double? = null,
    val usd: Double? = null,
)
