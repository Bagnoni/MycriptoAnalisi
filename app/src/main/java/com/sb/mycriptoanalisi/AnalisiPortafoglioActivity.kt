package com.sb.mycriptoanalisi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModelFactory

class AnalisiPortafoglioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel =
                viewModel(
                    factory = SettingsViewModelFactory(
                        application,
                        PortafoglioRepositoryLocale(application)
                    )
                )

            val fontSize by settingsViewModel.fontSize.collectAsState()
            val themeDark by settingsViewModel.themeDark.collectAsState()

            MyCriptoAnalisiTheme(darkTheme = themeDark) {
                Scaffold(
                    topBar = { MyTopBar(titleText = "📈 Analisi", backToMain = true) }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Benvenuto nella sezione Analisi!",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = fontSize.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Qui potrai avere Analisi sull'andamento  "
                                    + "del tuo portafoglio cripto\n\n"
                                    + "⚠️ Questa sezione è in fase di sviluppo.",
                            fontSize = fontSize.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                // In futuro qui collegheremo l'analisi AI vera e propria
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🔍 Avvia Analisi", fontSize = fontSize.sp)
                        }
                    }
                }
            }
        }
    }
}
