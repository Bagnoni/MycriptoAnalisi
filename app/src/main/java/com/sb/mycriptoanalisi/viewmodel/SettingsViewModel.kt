package com.sb.mycriptoanalisi.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class SettingsViewModel(
    application: Application,
    private val backupRepo: PortafoglioRepositoryLocale,
) : AndroidViewModel(application) {

    private val sharedPrefs =
        application.getSharedPreferences("settings_prefs", Application.MODE_PRIVATE)
    // Nel tuo ViewModel o Activity
    private var backupUri: Uri? = null

    fun selectBackupFolder(context: Context) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        (context as Activity).startActivityForResult(intent, REQUEST_CODE_BACKUP_FOLDER)
    }

    // Gestisci il risultato della selezione
    fun onBackupFolderSelected(uri: Uri, context: Context) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            backupUri = uri
            println("DEBUG: Backup folder selected: $uri")
        } catch (e: Exception) {
            println("DEBUG: Error taking permission: ${e.message}")
        }
    }


    // ------------------ FONT SIZE ------------------
    private val _fontSize = MutableStateFlow(sharedPrefs.getFloat("FONT_SIZE", 14f))
    val fontSize: StateFlow<Float> = _fontSize
    fun setFontSize(size: Float) {
        _fontSize.value = size
        sharedPrefs.edit { putFloat("FONT_SIZE", size) }
    }

    // ------------------ TEMA SCURO ------------------
    private val _themeDark = MutableStateFlow(sharedPrefs.getBoolean("THEME_DARK", false))
    val themeDark: StateFlow<Boolean> = _themeDark
    fun setThemeDark(dark: Boolean) {
        _themeDark.value = dark
        sharedPrefs.edit { putBoolean("THEME_DARK", dark) }
    }

    // ------------------ Aes Key ------------------
    fun deriveAESKey(iniziali: String, password: String): SecretKey {
        println("DEBUG: Deriving key from initials: '$iniziali'")
        println("DEBUG: Password length: ${password.length}")

            val salt = "MyCryptoFixedSalt2024" // FISSO
            val combined = (iniziali + password + salt).toByteArray(Charsets.UTF_8)
        val hash = MessageDigest.getInstance("SHA-256").digest(combined)
        return SecretKeySpec(hash.copyOf(16), "AES") // AES-128
    }
    // ------------------ AGGIORNAMENTO AUTOMATICO ------------------
    private val _autoUpdate = MutableStateFlow(sharedPrefs.getBoolean("AUTO_UPDATE", true))
    val autoUpdate: StateFlow<Boolean> = _autoUpdate
    fun setAutoUpdate(value: Boolean) {
        _autoUpdate.value = value
        sharedPrefs.edit { putBoolean("AUTO_UPDATE", value) }
    }

    // ------------------ NOTIFICHE ------------------
    private val _notificheOn = MutableStateFlow(sharedPrefs.getBoolean("NOTIFICHE_ON", true))
    val notificheOn: StateFlow<Boolean> = _notificheOn
    fun setNotificheOn(value: Boolean) {
        _notificheOn.value = value
        sharedPrefs.edit { putBoolean("NOTIFICHE_ON", value) }
    }

    // ------------------ MESSAGGI PER UI ------------------
    private val _messaggio = MutableLiveData<String>()
    val messaggio: LiveData<String> = _messaggio

    // ------------------ RESET APP ------------------
    fun resetApp(repository: CriptoRepository) {
        viewModelScope.launch {
            // Cancella tutti i dati dal database locale
            repository.deleteAll()

            // Ripristina le preferenze ai valori di default
            setFontSize(14f)
            setThemeDark(false)
            setAutoUpdate(true)
            setNotificheOn(true)

            // Cancella password e iniziali utente
            sharedPrefs.edit {
                putString("BACKUP_PASSWORD", "mycripto123")
                putBoolean("PASSWORD_DEFAULT", true)
                remove("USER_INIZIALI")
            }

            // Messaggio finale per l'utente
            _messaggio.postValue(
                "⚠️ Reset completato. Tutti i dati sono stati cancellati.\n" +
                        "💾 Se non hai fatto un backup, potresti aver perso il portafoglio."
            )
        }
    }




    // ------------------ BACKUP PORTAFOGLIO ------------------
    fun backupPortafoglio(repository: CriptoRepository, passwordEffettiva: String) {
        viewModelScope.launch {
            println("DEBUG: Avvio backupPortafoglio")

            val lista = repository.getAll().first()
            println("DEBUG: Lista da salvare: ${lista.size} elementi")

            val iniziali = getIniziali()
            if (iniziali == null) {
                println("DEBUG: Iniziali mancanti")
                _messaggio.postValue("⚠️ Iniziali mancanti: impossibile eseguire il backup")
                return@launch
            } else {
                println("DEBUG: Iniziali trovate: $iniziali")
            }

            println("DEBUG: Password effettiva usata: $passwordEffettiva")
            val chiaveAES = deriveAESKey(iniziali, passwordEffettiva)
            println("DEBUG: Chiave AES derivata")

            try {
                backupRepo.savePortafoglio(lista, chiaveAES)
                println("DEBUG: Salvataggio completato")
                _messaggio.postValue("💾 Backup salvato con successo")
            } catch (e: Exception) {
                println("DEBUG: Errore durante il salvataggio")
                e.printStackTrace()
                _messaggio.postValue("❌ Errore nel salvataggio del backup")
            }
        }
    }
    // ------------------ RIPRISTINO PORTAFOGLIO------------------
    suspend fun ripristinaPortafoglio(repository: CriptoRepository, passwordEffettiva: String): Boolean {
        val iniziali = getIniziali() ?: run {
            _messaggio.postValue("⚠️ Iniziali mancanti: impossibile ripristinare il backup")
            return false
        }

        println("DEBUG: Ripristino - Iniziali: '$iniziali'")
        println("DEBUG: Ripristino - Password: '$passwordEffettiva'")

        val chiaveAES = deriveAESKey(iniziali, passwordEffettiva)
        println("DEBUG: Chiave AES derivata: ${chiaveAES.encoded?.size} bytes")

        // AGGIUNGI TRY-CATCH PER ERRORI DI DECIFRAZIONE
        val lista = try {
            backupRepo.loadPortafoglio(chiaveAES)
        } catch (e: Exception) {
            println("DEBUG: Errore durante decifratura: ${e.message}")
            e.printStackTrace()
            emptyList()
        }

        println("DEBUG: Lista ripristinata: ${lista.size} elementi")

        return if (lista.isNotEmpty()) {
            lista.forEach { repository.insert(it) }
            _messaggio.postValue("📂 Backup ripristinato con successo")
            true
        } else {
            _messaggio.postValue("❌ Chiave errata o backup non valido")
            false
        }
    }
    // ------------------ CANCELLA BACKUP ------------------
    fun cancellaBackup() {
        backupRepo.cancellaBackup()
        _messaggio.postValue("❌ Backup cancellato")
    }

    // ------------------ PASSWORD ------------------
    fun setPassword(pw: String) {
        sharedPrefs.edit {
          putString("BACKUP_PASSWORD", pw)
         putBoolean("PASSWORD_DEFAULT", pw == "mycripto123")
       }
  }

    fun getPassword(): String {
        return sharedPrefs.getString("BACKUP_PASSWORD", "mycripto123") ?: "mycripto123"
    }

    fun isPasswordDefault(): Boolean {
        return sharedPrefs.getBoolean("PASSWORD_DEFAULT", true)
    }
    fun setIniziali(iniziali: String) {
        sharedPrefs.edit { putString("USER_INIZIALI", iniziali) }
    }

    fun getIniziali(): String? {
        return sharedPrefs.getString("USER_INIZIALI", null)
    }

    fun hasIniziali(): Boolean {
        return sharedPrefs.contains("USER_INIZIALI")
    }

    /*fun deriveKeyFromIniziali(iniziali: String, salt: String = "MyCriptoSalt"): ByteArray {
        val input = iniziali + salt
        return java.security.MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    }*/


}