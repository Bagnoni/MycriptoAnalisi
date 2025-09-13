package com.sb.mycriptoanalisi

import PortafoglioViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sb.mycriptoanalisi.data.CriptoDatabase
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import com.sb.mycriptoanalisi.viewmodel.PortafoglioViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModelFactory


class StrategieAIActivity : ComponentActivity() { // ✅ questa è la classe che serve
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = CriptoDatabase.getDatabase(application).criptoDao()
        val repository = CriptoRepository(dao)

        val portafoglioFactory = PortafoglioViewModelFactory(application, repository)
        val settingsFactory = SettingsViewModelFactory(application, PortafoglioRepositoryLocale(application))


        setContent {
            val portafoglioViewModel: PortafoglioViewModel = viewModel(factory = portafoglioFactory)
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)

            StrategieAIScreen(settingsViewModel, portafoglioViewModel)
        }
    }
}