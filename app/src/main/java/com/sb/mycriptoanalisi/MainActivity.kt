package com.sb.mycriptoanalisi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    application,
                    PortafoglioRepositoryLocale(application)
                )
            )

            val themeDark by settingsViewModel.themeDark.collectAsState()
            val fontSize by settingsViewModel.fontSize.collectAsState()

            MyCriptoAnalisiTheme(darkTheme = themeDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(settingsViewModel = settingsViewModel, fontSize = fontSize)
                }
            }
        }
    }
}

@Composable
fun MainScreen(settingsViewModel: SettingsViewModel, fontSize: Float) {
    val context = LocalContext.current
    val settingsViewModel = viewModel<SettingsViewModel>()
    val inizialiSalvate = settingsViewModel.getIniziali()
    var showDialog by remember { mutableStateOf(inizialiSalvate == null) }
    var inizialiInput by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Inserisci le tue iniziali") },
            text = {
                Column {
                    Text("Le iniziali servono per generare la chiave di sicurezza del backup.")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = inizialiInput,
                        onValueChange = { inizialiInput = it },
                        label = { Text("Iniziali") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (inizialiInput.isNotBlank()) {
                        settingsViewModel.setIniziali(inizialiInput.trim())
                        showDialog = false
                    }
                }) {
                    Text("Conferma")
                }
            }
        )
    }
    val menuItems = listOf(
        "💰 Aggiungi Cripto al Portafoglio" to true,
        "📊 Portafoglio" to true,
        "✏️ Modifica Portafoglio" to true,
        "📈 Analisi Portafoglio" to true,
        "🤖 Strategie AI" to true,
        "⚙️ Settings" to true,
    )

    Scaffold(
        topBar = { MyTopBar(titleText = "📊 My Cripto App", backToMain = false) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(menuItems) { item ->
                    val (title, abilitato) = item
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = abilitato) {
                                when (title) {
                                    "💰 Aggiungi Cripto al Portafoglio" -> context.startActivity(
                                        Intent(context, PortafoglioActivity::class.java)
                                    )

                                    "📊 Portafoglio" -> context.startActivity(
                                        Intent(context, DashboardActivity::class.java)
                                    )

                                    "✏️ Modifica Portafoglio" -> context.startActivity(
                                        Intent(context, ModificaPortafoglioActivity::class.java)
                                    )

                                    "📈 Analisi Portafoglio" -> context.startActivity(
                                        Intent(context, AnalisiPortafoglioActivity::class.java)
                                    )

                                    "🤖 Strategie AI" -> context.startActivity(
                                        Intent(context, StrategieAIActivity::class.java)
                                    )

                                    "⚙️ Settings" -> context.startActivity(
                                        Intent(context, SettingsActivity::class.java)
                                    )
                                }
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = title,
                                fontSize = fontSize.sp,
                                style = if (abilitato) MaterialTheme.typography.bodyLarge
                                else MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        }
                    }
                }

            }
        }
    }
}
