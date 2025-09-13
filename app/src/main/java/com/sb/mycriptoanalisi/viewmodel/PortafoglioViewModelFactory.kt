import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.viewmodel.PortafoglioViewModel

class PortafoglioViewModelFactory(
    private val application: Application,
    private val repository: CriptoRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortafoglioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortafoglioViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}