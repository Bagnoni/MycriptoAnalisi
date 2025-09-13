package com.sb.mycriptoanalisi

import UtilityComuni.nascondiTastiera
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModelFactory
import com.sb.mycriptoanalisi.data.CriptoDatabase
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsAvanzatiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = CriptoDatabase.getDatabase(application)
        val repository = CriptoRepository(db.criptoDao())

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application, PortafoglioRepositoryLocale(application))
            )

            val fontSize by settingsViewModel.fontSize.collectAsState()
            val passwordSalvata = settingsViewModel.getPassword()
            val isDefaultPassword = settingsViewModel.isPasswordDefault()

            var passwordInput by remember {
                mutableStateOf(if (isDefaultPassword) passwordSalvata else "")
            }
            var messaggio by remember { mutableStateOf("") }
            //var showBackupDialog by remember { mutableStateOf(false) }
           // var showRipristinoDialog by remember { mutableStateOf(false) }
            var showDeleteBackupDialog by remember { mutableStateOf(false) }
            var showResetDialog by remember { mutableStateOf(false) }
            if (!settingsViewModel.hasIniziali()) {
                var inizialiInput by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = inizialiInput,
                    onValueChange = { inizialiInput = it },
                    label = { Text("Inserisci le tue iniziali") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (inizialiInput.isNotBlank()) {
                            settingsViewModel.setIniziali(inizialiInput.trim())
                            messaggio = "✅ Iniziali salvate con successo"
                        } else {
                            messaggio = "⚠️ Inserisci almeno una lettera"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salva Iniziali")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }


            MyCriptoAnalisiTheme {
                Scaffold(
                    topBar = {
                        MyTopBar(titleText = "⚙️ Setup Avanzato", backToMain = true)
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(80.dp)) // evita sovrapposizione con tasti sistema

                            if (showResetDialog) {
                                AlertDialog(
                                    onDismissRequest = { showResetDialog = false },
                                    title = { Text("⚠️ Conferma Reset") },
                                    text = { Text("Tutti i dati dell'app verranno cancellati, incluso il portafoglio.\\n" +
                                            "\uD83D\uDCA1 E' consigliabile effettuare un Backup") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            settingsViewModel.resetApp(repository)
                                            showResetDialog = false
                                            messaggio = "✅ Reset completato"
                                        }) {
                                            Text("Conferma", color = Color.Red)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showResetDialog = false }) {
                                            Text("Annulla")
                                        }
                                    }
                                )
                            }

                            Button(
                                onClick = {
                                    nascondiTastiera(this@SettingsAvanzatiActivity)
                                    showResetDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("💥 Reset App", color = Color.White, fontSize = fontSize.sp)

                            }

                        }
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ---------------- BACKUP ----------------
                        // Campo password visibile sempre sopra il tasto di backup
                        var passwordInput by remember { mutableStateOf("") }
                        var showBackupDialog by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password di Backup") },
                            visualTransformation = if (settingsViewModel.isPasswordDefault()) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                nascondiTastiera(this@SettingsAvanzatiActivity)
                                showBackupDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💾 Backup Portafoglio", fontSize = fontSize.sp)
                        }

                        if (showBackupDialog) {
                            val passwordEffettiva = passwordInput.ifEmpty { "mycripto123" }

                            AlertDialog(
                                onDismissRequest = { showBackupDialog = false },
                                title = { Text("⚠️ Conferma Backup") },
                                text = {
                                    Text(
                                        text = "⚠\uFE0F  Verrà usata la password $passwordEffettiva",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        settingsViewModel.backupPortafoglio(repository, passwordEffettiva)
                                        showBackupDialog = false
                                        messaggio = "✅ Backup completato"
                                    }) {
                                        Text("Conferma")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showBackupDialog = false }) {
                                        Text("Annulla")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ---------------- RIPRISTINO ----------------
                        var pwdRipristino by remember { mutableStateOf("") }
                        var showRipristinoDialog by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                nascondiTastiera(this@SettingsAvanzatiActivity)
                                showRipristinoDialog = true
                                pwdRipristino = "" // resetta il campo ogni volta che apri il dialogo
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📂 Ripristina Backup", fontSize = fontSize.sp)
                        }

                        if (showRipristinoDialog) {
                            AlertDialog(
                                onDismissRequest = { showRipristinoDialog = false },
                                title = { Text("🔑 Inserisci Password") },
                                text = {
                                    Column {
                                        OutlinedTextField(
                                            value = pwdRipristino,
                                            onValueChange = { pwdRipristino = it },
                                            label = { Text("Password") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            visualTransformation = PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (pwdRipristino.isEmpty()) "⚠️ Verrà usata la password di default: mycripto123" else "🔐 Verrà usata la password inserita",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val passwordEffettiva = if (pwdRipristino.isNotEmpty()) pwdRipristino else "mycripto123"
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val successo = settingsViewModel.ripristinaPortafoglio(repository, passwordEffettiva)
                                            showRipristinoDialog = false
                                            messaggio = if (successo) "✅ Ripristino completato" else "❌ Password errata, ripristino annullato"
                                        }
                                    }) {
                                        Text("Conferma")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showRipristinoDialog = false }) {
                                        Text("Annulla")
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // ---------------- CANCELLA BACKUP ----------------
                        Button(
                            onClick = {
                                nascondiTastiera(this@SettingsAvanzatiActivity)
                                showDeleteBackupDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("❌ Cancella Backup", color = Color.White, fontSize = fontSize.sp)
                        }

                        if (showDeleteBackupDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteBackupDialog = false },
                                title = { Text("⚠️ Conferma Eliminazione") },
                                text = { Text("Vuoi davvero cancellare il backup salvato?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        settingsViewModel.cancellaBackup()
                                        showDeleteBackupDialog = false
                                        messaggio = "✅ Backup eliminato"
                                    }) {
                                        Text("Conferma", color = Color.Red)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteBackupDialog = false }) {
                                        Text("Annulla")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (messaggio.isNotEmpty()) {
                            Text(
                                text = messaggio,
                                fontSize = fontSize.sp,
                                color = if (messaggio.startsWith("✅")) Color(0xFF2E7D32) else Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}