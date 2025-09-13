package com.sb.mycriptoanalisi.data

import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {

    // Ottieni i prezzi attuali di una o più criptovalute
    // Esempio: https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum&vs_currencies=eur,usd
    @GET("simple/price")
    suspend fun getSimplePrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vs: String,
    ): Map<String, Map<String, Double>>


    // Ottieni la lista completa delle criptovalute supportate da CoinGecko
    // Esempio: https://api.coingecko.com/api/v3/coins/list
    @GET("coins/list")
    suspend fun getCoinList(): List<CoinGeckoCoin>
}


