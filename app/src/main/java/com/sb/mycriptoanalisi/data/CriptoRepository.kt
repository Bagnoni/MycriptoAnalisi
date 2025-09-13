package com.sb.mycriptoanalisi.data

import kotlinx.coroutines.flow.Flow

class CriptoRepository(private val dao: CriptoDao) {

    fun getAll(): Flow<List<CriptoPosseduta>> = dao.getAll()

    suspend fun insert(cripto: CriptoPosseduta) = dao.insert(cripto)

    suspend fun update(cripto: CriptoPosseduta) = dao.update(cripto)

    suspend fun delete(cripto: CriptoPosseduta) = dao.delete(cripto)

    suspend fun deleteAll() = dao.deleteAll()

    /**
     * Cerca una cripto nella lista CoinGecko e la inserisce nel DB locale
     * @param input nome o simbolo (case-insensitive)
     * @param quantita quantità posseduta
     * @param lista lista completa da CoinGecko
     */
    suspend fun insertFromInput(
        input: String,
        quantita: Double,
        lista: List<CoinGeckoCoin>,
    ) {
        val query = input.trim().lowercase()
        val coin = lista.firstOrNull {
            it.name.lowercase() == query || it.symbol.lowercase() == query
        }

        if (coin != null) {
            val nuovaCripto = CriptoPosseduta(
                simbolo = coin.symbol.uppercase(),
                nome = coin.name,
                quantita = quantita,
                idCoinGecko = coin.id
            )
            dao.insert(nuovaCripto)
        } else {
            // Puoi gestire l'errore nel ViewModel o mostrare un messaggio all'utente
            throw IllegalArgumentException("Criptovaluta non trovata: $input")
        }
    }
}