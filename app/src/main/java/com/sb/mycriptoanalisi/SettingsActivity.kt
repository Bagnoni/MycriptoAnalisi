package com.sb.mycriptoanalisi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModelFactory
import com.sb.mycriptoanalisi.data.CriptoDatabase
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = CriptoDatabase.getDatabase(application)
        val repository = CriptoRepository(db.criptoDao())

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    application,
                    PortafoglioRepositoryLocale(application)
                )
            )

            val fontSize by settingsViewModel.fontSize.collectAsState()
            val themeDark by settingsViewModel.themeDark.collectAsState()
            val autoUpdate by settingsViewModel.autoUpdate.collectAsState()
            val notificheOn by settingsViewModel.notificheOn.collectAsState()

            MyCriptoAnalisiTheme(darkTheme = themeDark) {
                Scaffold(
                    topBar = { MyTopBar(titleText = "📊 Settings", backToMain = true) }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Switch impostazioni
                        @Composable
                        fun RowSetting(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontSize = fontSize.sp)
                                Switch(checked = checked, onCheckedChange = onChange)
                            }
                        }

                        RowSetting("Aggiornamento automatico", autoUpdate) {
                            settingsViewModel.setAutoUpdate(it)
                        }
                        RowSetting("Notifiche attive", notificheOn) {
                            settingsViewModel.setNotificheOn(it)
                        }
                        RowSetting("Tema scuro", themeDark) {
                            settingsViewModel.setThemeDark(it)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Slider font
                        Text("Dimensione testo: ${fontSize.toInt()}sp", fontSize = fontSize.sp)
                        Slider(
                            value = fontSize,
                            onValueChange = { settingsViewModel.setFontSize(it) },
                            valueRange = 10f..20f
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Pulsante per aprire i settings avanzati
                        Button(
                            onClick = {
                                val intent = Intent(this@SettingsActivity, SettingsAvanzatiActivity::class.java)
                                startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚙️ Setup Avanzato", fontSize = fontSize.sp)
                        }
                    }
                }
            }
        }
    }
}