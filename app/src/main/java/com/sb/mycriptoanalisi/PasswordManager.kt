import android.content.Context
import androidx.core.content.edit

object PasswordManager {
    private const val PREFS_NAME = "cripto_prefs"
    private const val KEY_PASSWORD = "password"
    private const val KEY_DEFAULT_FLAG = "password_default"
    //val db = CriptoDatabase.getDatabase(application)
    //val repository = CriptoRepository(db.criptoDao())
 fun getPassword(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, "mycripto123") ?: "mycripto123"
    }

    fun setPassword(context: Context, nuovaPassword: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_PASSWORD, nuovaPassword)
                .putBoolean(KEY_DEFAULT_FLAG, false)
        }
    }

    fun isDefaultPassword(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DEFAULT_FLAG, true)
    }
    private const val KEY_INIZIALI = "iniziali_utente"

    fun getIniziali(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_INIZIALI, null)
    }

    fun setIniziali(context: Context, iniziali: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_INIZIALI, iniziali) }
    }

    fun hasIniziali(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_INIZIALI)
    }


}