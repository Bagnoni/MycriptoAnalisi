package com.sb.mycriptoanalisi

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupAvanzatoScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("ai_config", Context.MODE_PRIVATE)

    var openAIKey by remember { mutableStateOf(prefs.getString("openai_key", "") ?: "") }
    var azureKey by remember { mutableStateOf(prefs.getString("azure_key", "") ?: "") }
    var selectedModel by remember { mutableStateOf(prefs.getString("model", "GPT-4") ?: "GPT-4") }

    val modelliDisponibili = listOf("GPT-4", "GPT-3.5", "ND1", "ND2")

    MyCriptoAnalisiTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("⚙️ Setup Avanzato AI") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text("🔐 Inserisci le chiavi API", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = openAIKey,
                    onValueChange = { openAIKey = it },
                    label = { Text("🔑 OpenAI API Key") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = azureKey,
                    onValueChange = { azureKey = it },
                    label = { Text("🔑 Azure AI Key") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("🧠 Seleziona modello AI", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                modelliDisponibili.forEach { modello ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedModel == modello,
                            onClick = { selectedModel = modello }
                        )
                        Text(modello, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        prefs.edit {
                            putString("openai_key", openAIKey)
                                .putString("azure_key", azureKey)
                                .putString("model", selectedModel)
                        }

                        Toast.makeText(context, "✅ Impostazioni salvate", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💾 Salva configurazione", fontSize = 16.sp)
                }
            }
        }
    }
}