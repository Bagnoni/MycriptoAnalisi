package com.sb.mycriptoanalisi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cripto")
data class CriptoPosseduta(
    @PrimaryKey val idCoinGecko: String,
    val nome: String,
    val simbolo: String,
    val quantita: Double,
    val valoreUsd: Double? = null,
    val valoreEur: Double? = null,
    val market: String = "CoinGecko",
)



