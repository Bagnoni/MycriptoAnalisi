package com.sb.mycriptoanalisi.data

import android.app.Application
import android.os.Environment
import com.google.gson.Gson
import java.io.File
import javax.crypto.Cipher
import javax.crypto.SecretKey

data class BackupWrapper(
    val check: String = "VALID_BACKUP",
    val portafoglio: List<CriptoPosseduta>
)

class PortafoglioRepositoryLocale(application: Application) {

    private val gson = Gson()

    // ✅ Directory pubblica compatibile con Android 10–15
    private val backupDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        "MyCriptoApp"
    )
    private val backupFile = File(backupDir, "backup_portafoglio.json")

    // 🔐 Derivazione chiave AES da iniziali + password
  /*  fun deriveAESKey(iniziali: String, password: String): SecretKey {
        val combined = (iniziali + password).toByteArray(Charsets.UTF_8)
        val sha = MessageDigest.getInstance("SHA-256")
        val key = sha.digest(combined)
        return SecretKeySpec(key.copyOf(16), "AES")
    }*/

    private fun encrypt(data: String, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun decrypt(data: ByteArray, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return String(cipher.doFinal(data), Charsets.UTF_8)
    }

    // 💾 Salvataggio persistente
    fun savePortafoglio(lista: List<CriptoPosseduta>, key: SecretKey) {
        try {
            if (!backupDir.exists()) backupDir.mkdirs()

            val wrapper = BackupWrapper(portafoglio = lista)
            val json = gson.toJson(wrapper)
            val encrypted = encrypt(json, key)

            if (backupFile.exists()) {
                val deleted = backupFile.delete()
                println("DEBUG: Vecchio backup eliminato: $deleted")
                if (!deleted) {
                    println("DEBUG: Impossibile eliminare il file. Permessi mancanti?")
                    return
                }
            }


            backupFile.writeBytes(encrypted)

            println("DEBUG: Backup salvato in ${backupFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("DEBUG: Errore nel salvataggio: ${e.message}")
        }
    }

    // 📂 Ripristino compatibile
    fun loadPortafoglio(key: SecretKey): List<CriptoPosseduta> {
        if (!backupFile.exists()) return emptyList()
        return try {
            val encrypted = backupFile.readBytes()
            val json = decrypt(encrypted, key)
            val wrapper = gson.fromJson(json, BackupWrapper::class.java)

            if (wrapper.check == "VALID_BACKUP") wrapper.portafoglio
            else {
                println("DEBUG: Backup non valido")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("DEBUG: Errore nel ripristino: ${e.message}")
            emptyList()
        }
    }

    fun cancellaBackup(): Boolean {
        return backupFile.exists() && backupFile.delete()
    }

    /*fun backupEsiste(): Boolean = backupFile.exists()

    fun getBackupPath(): String = backupFile.absolutePath*/
}