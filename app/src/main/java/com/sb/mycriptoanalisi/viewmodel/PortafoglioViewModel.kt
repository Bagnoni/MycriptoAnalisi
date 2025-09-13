package com.sb.mycriptoanalisi.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sb.mycriptoanalisi.data.CoinGeckoCoin
import com.sb.mycriptoanalisi.data.CriptoPosseduta
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import com.sb.mycriptoanalisi.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortafoglioViewModel(
    application: Application,
    private val repository: CriptoRepository,
) : AndroidViewModel(application) {

    // Repository locale per backup con password
    private val portafoglioLocaleRepo by lazy { PortafoglioRepositoryLocale(getApplication()) }

    // SharedPreferences perset la password di backup
    private val sharedPrefs =
        application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private fun getPassword(): String =
        sharedPrefs.getString("BACKUP_PASSWORD", "mycripto123") ?: "mycripto123"

    fun setPassword(password: String) {
        sharedPrefs.edit().putString("BACKUP_PASSWORD", password).apply()
    }

    // Portafoglio dal DB principale
    val portafoglio = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Messaggi per UI
    private val _messaggio = MutableLiveData<String>()
    val messaggio: LiveData<String> = _messaggio

    // Lista CoinGecko per autocomplete
    private val _listaCoinGecko = MutableStateFlow<List<CoinGeckoCoin>>(emptyList())
    val listaCoinGecko: StateFlow<List<CoinGeckoCoin>> = _listaCoinGecko

// ---------------------- OPERAZIONI BASE ----------------------

    fun inserisciDaInput(input: String, quantita: Double) = viewModelScope.launch {
        if (input.isBlank()) {
            _messaggio.postValue("❌ Inserisci il nome della cripto")
            return@launch
        }
        if (quantita < 0.0) {
            _messaggio.postValue("❌ Inserisci una quantità valida (>= 0)")
            return@launch
        }

        val cripto = trovaCripto(input)
        if (cripto == null || cripto.id.isBlank()) {
            _messaggio.postValue("❌ Cripto non riconosciuta: \"$input\"")
            return@launch
        }

        val giaPresente = portafoglio.value.any { it.idCoinGecko == cripto.id }
        if (giaPresente) {
            _messaggio.postValue("⚠️ \"${cripto.name}\" è già nel portafoglio")
            return@launch
        }

        val nuovaCripto = CriptoPosseduta(
            idCoinGecko = cripto.id,
            nome = cripto.name,
            simbolo = cripto.symbol.uppercase(),
            quantita = quantita
        )

        repository.insert(nuovaCripto)
        _messaggio.postValue("✅ ${cripto.name} aggiunta al portafoglio")
    }

    fun delete(cripto: CriptoPosseduta) = viewModelScope.launch {
        repository.delete(cripto)
    }

// ---------------------- AGGIORNAMENTO COINGECKO ----------------------

    fun aggiornaPrezziDaCoinGecko() = viewModelScope.launch {
        val criptoList = portafoglio.value.filter { it.idCoinGecko.isNotBlank() }
        if (criptoList.isEmpty()) return@launch

        val ids = criptoList.map { it.idCoinGecko }.distinct().joinToString(",")

        try {
            val response = RetrofitClient.api.getSimplePrice(ids = ids, vs = "eur,usd")
            criptoList.forEach { cripto ->
                val prezzo = response[cripto.idCoinGecko]
                val updated = cripto.copy(
                    valoreUsd = prezzo?.get("usd") ?: cripto.valoreUsd,
                    valoreEur = prezzo?.get("eur") ?: cripto.valoreEur,
                    quantita = cripto.quantita
                )
                repository.update(updated)
            }
        } catch (e: Exception) {
            Log.e("COINGECKO", "Errore aggiornando prezzi", e)
        }
    }

    fun caricaListaCoinGecko() = viewModelScope.launch {
        try {
            _listaCoinGecko.value = RetrofitClient.api.getCoinList()
        } catch (e: Exception) {
            Log.e("COINGECKO", "Errore caricamento lista cripto", e)
        }
    }


// ---------------------- CALCOLI ----------------------

    fun calcolaPercentuali(): List<Pair<CriptoPosseduta, Double>> {
        val totaleEur = portafoglio.value.sumOf { (it.valoreEur ?: 0.0) * it.quantita }
        if (totaleEur == 0.0) return emptyList()
        return portafoglio.value.map { cripto ->
            val valore = (cripto.valoreEur ?: 0.0) * cripto.quantita
            cripto to (valore / totaleEur * 100)
        }
    }

// ---------------------- ACQUISTA/VENDI/SCAMBIA ----------------------

    fun acquistaQuantita(simbolo: String, quantita: Double) = viewModelScope.launch {
        val cripto = portafoglio.value.find { it.simbolo.equals(simbolo, true) }
        if (cripto != null) {
            repository.update(cripto.copy(quantita = cripto.quantita + quantita))
            _messaggio.postValue("✅ Hai acquistato $quantita ${cripto.simbolo}")
            aggiornaPrezziDaCoinGecko()
        } else {
            _messaggio.postValue("❌ Cripto non presente nel portafoglio")
        }
    }

    fun vendiQuantita(simbolo: String, quantita: Double) = viewModelScope.launch {
        val cripto = portafoglio.value.find { it.simbolo.equals(simbolo, true) }
        if (cripto != null) {
            val nuovaQuantita = (cripto.quantita - quantita).coerceAtLeast(0.0)
            repository.update(cripto.copy(quantita = nuovaQuantita))
            _messaggio.postValue("💸 Hai venduto $quantita ${cripto.simbolo}")
            aggiornaPrezziDaCoinGecko()
        } else {
            _messaggio.postValue("❌ Cripto non presente nel portafoglio")
        }
    }

    fun scambiaQuantita(simboloDa: String, simboloA: String, quantita: Double) =
        viewModelScope.launch {
            val criptoDa = portafoglio.value.find { it.simbolo.equals(simboloDa, true) }
            val criptoA = portafoglio.value.find { it.simbolo.equals(simboloA, true) }

            if (criptoDa != null && criptoA != null) {
                if (criptoDa.quantita >= quantita) {
                    val updatedDa = criptoDa.copy(quantita = criptoDa.quantita - quantita)
                    val valoreDa = (criptoDa.valoreEur ?: 0.0) * quantita
                    val quantitaA = if (criptoA.valoreEur != null && criptoA.valoreEur > 0)
                        valoreDa / criptoA.valoreEur else 0.0
                    val updatedA = criptoA.copy(quantita = criptoA.quantita + quantitaA)

                    repository.update(updatedDa)
                    repository.update(updatedA)
                    _messaggio.postValue(
                        "🔄 Scambiati $quantita ${criptoDa.simbolo} in ${"%.4f".format(quantitaA)} ${criptoA.simbolo}"
                    )
                    aggiornaPrezziDaCoinGecko()
                } else {
                    _messaggio.postValue("⚠️ Quantità insufficiente per scambiare")
                }
            } else {
                _messaggio.postValue("⚠️ Una o entrambe le cripto non trovate")
            }
        }

// ---------------------- UTILI ----------------------

    private fun trovaCripto(input: String): CoinGeckoCoin? {
        val query = input.trim().lowercase()
        return listaCoinGecko.value.firstOrNull { it.name.lowercase() == query }
    }
}
