
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.sb.mycriptoanalisi.data.CriptoPosseduta
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.RetrofitClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object UtilityComuni {

    fun nascondiTastiera(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(null, 0)
    }

    fun formattaData(data: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return data.format(formatter)
    }



}

    // Aggiungi qui altre funzioni riutilizzabili

    //------Per richiamarle nelle activity:---------
   // UtilityComuniActivity.mostraToast(this, "Ciao Sandro!")
   // val risultato = CommonUtils.somma(10, 5)





