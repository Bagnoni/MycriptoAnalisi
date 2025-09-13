package com.sb.mycriptoanalisi.data

import android.app.Application
import android.os.Environment
import androidx.room.processor.Context
import com.google.gson.Gson
import java.io.File
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

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

    private fun encryptWithIV(data: String, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // ← SPECIFICA MODALITÀ!
        cipher.init(Cipher.ENCRYPT_MODE, key)

        // SALVA L'IV INSIEME AI DATI!
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        return iv + encrypted // IV (16 bytes) + dati cifrati
    }

    private fun decryptWithIV(data: ByteArray, key: SecretKey): String {
        // ESTRAI IV (primi 16 bytes)
        val iv = data.copyOfRange(0, 16)
        val actualData = data.copyOfRange(16, data.size)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // ← STESSA MODALITÀ!
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

        return String(cipher.doFinal(actualData), Charsets.UTF_8)
    }

    // 💾 Salvataggio con SAF
    fun savePortafoglio(lista: List<CriptoPosseduta>, key: SecretKey, context: Context) {
        if (backupUri == null) {
            println("DEBUG: Nessuna cartella di backup selezionata")
            return
        }

        try {
            val wrapper = BackupWrapper(portafoglio = lista)
            val json = gson.toJson(wrapper)
            val encrypted = encryptWithIV(json, key)

            // Crea il file nella cartella selezionata
            val backupFileUri = DocumentsContract.createDocument(
                context.contentResolver,
                backupUri!!,
                "application/json",
                "backup_portafoglio_${System.currentTimeMillis()}.enc"
            )

            backupFileUri?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(encrypted)
                    println("DEBUG: Backup salvato con SAF: $uri")
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Errore salvataggio SAF: ${e.message}")
            e.printStackTrace()
        }
    }

    // 📂 Ripristino con SAF
    fun loadPortafoglio(key: SecretKey, context: Context): List<CriptoPosseduta> {
        if (backupUri == null) {
            println("DEBUG: Nessuna cartella di backup selezionata")
            return emptyList()
        }

        return try {
            // Cerca il file di backup più recente
            val backupFiles = findBackupFiles(context)
            val latestBackup = backupFiles.maxByOrNull { it.second } // (uri, timestamp)

            latestBackup?.first?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val encrypted = inputStream.readBytes()
                    val json = decryptWithIV(encrypted, key)
                    val wrapper = gson.fromJson(json, BackupWrapper::class.java)

                    if (wrapper.check == "VALID_BACKUP") wrapper.portafoglio else emptyList()
                }
            } ?: emptyList()
        } catch (e: Exception) {
            println("DEBUG: Errore ripristino SAF: ${e.message}")
            emptyList()
        }
    }

    // 🗑️ Cancellazione con fallback
    fun cancellaBackup(): Boolean {
        println("DEBUG: Tentativo cancellazione backup: ${backupFile.absolutePath}")
        println("DEBUG: File exists: ${backupFile.exists()}")
        println("DEBUG: File canWrite: ${backupFile.canWrite()}")

        return if (backupFile.exists()) {
            val deleted = backupFile.delete()
            println("DEBUG: Delete risultato: $deleted")

            if (!deleted) {
                // Fallback: rinomina invece di cancellare
                val oldFile = File(backupFile.parent, "deleted_backup_${System.currentTimeMillis()}.enc")
                val renamed = backupFile.renameTo(oldFile)
                println("DEBUG: Rename risultato: $renamed -> ${oldFile.absolutePath}")
                renamed
            } else {
                true
            }
        } else {
            true // File non esiste = successo
        }
    }
}