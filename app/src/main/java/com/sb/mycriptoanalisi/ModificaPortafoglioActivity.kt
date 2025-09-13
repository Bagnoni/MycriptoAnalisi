package com.sb.mycriptoanalisi

import PortafoglioViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sb.mycriptoanalisi.data.CriptoDatabase
import com.sb.mycriptoanalisi.data.CriptoPosseduta
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import com.sb.mycriptoanalisi.viewmodel.PortafoglioViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModelFactory

class ModificaPortafoglioActivity : ComponentActivity() {
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
            val themeDark by settingsViewModel.themeDark.collectAsState()
            val fontSize by settingsViewModel.fontSize.collectAsState()

            MyCriptoAnalisiTheme(darkTheme = themeDark) {
                val viewModel: PortafoglioViewModel = viewModel(
                    factory = PortafoglioViewModelFactory(application, repository)
                )
                EditPortafoglioScreen(viewModel, fontSize, themeDark)
            }
        }
    }
}

@Composable
fun EditPortafoglioScreen(
    viewModel: PortafoglioViewModel,
    fontSize: Float,
    themeDark: Boolean,
) {
    val portafoglio by viewModel.portafoglio.collectAsState()
    val messaggio by viewModel.messaggio.observeAsState("")

    var criptoSelezionata by remember { mutableStateOf<CriptoPosseduta?>(null) }
    var criptoTarget by remember { mutableStateOf<CriptoPosseduta?>(null) }

    var quantita by remember { mutableStateOf("") }
    var valoreinEuro by remember { mutableStateOf("") }
    var usaValore by remember { mutableStateOf(false) } // se true input è in €/$

    var tipoOperazione by remember { mutableStateOf("Acquista") } // Acquista, Vendi, Scambia

    Scaffold(
        topBar = { MyTopBar(titleText = "✏️ Modifica Portafoglio", backToMain = true) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Selezione operazione
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("Acquista", "Vendi", "Scambia").forEach { op ->
                    Button(
                        onClick = { tipoOperazione = op },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tipoOperazione == op)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(op, fontSize = fontSize.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Dropdown cripto da usare
            Text("Seleziona cripto", fontSize = fontSize.sp)
            DropdownMenuCripto(
                items = portafoglio,
                selected = criptoSelezionata,
                onSelect = { criptoSelezionata = it },
                fontSize = fontSize
            )

            Spacer(Modifier.height(8.dp))

            // Dropdown cripto target (solo per scambio)
            if (tipoOperazione == "Scambia") {
                Text("Seleziona cripto di arrivo", fontSize = fontSize.sp)
                DropdownMenuCripto(
                    items = portafoglio.filter { it.simbolo != criptoSelezionata?.simbolo },
                    selected = criptoTarget,
                    onSelect = { criptoTarget = it },
                    fontSize = fontSize
                )
                Spacer(Modifier.height(8.dp))
            }

            // Input quantità/valore
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(if (usaValore) "Valore (€)" else "Quantità", fontSize = fontSize.sp)
                TextField(
                    value = if (usaValore) valoreinEuro else quantita,
                    onValueChange = {
                        if (usaValore) valoreinEuro = it else quantita = it
                    },
                    singleLine = true,
                    modifier = Modifier.width(150.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(fontSize = fontSize.sp)
                )
                Switch(
                    checked = usaValore,
                    onCheckedChange = { usaValore = it }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Pulsanti operazione
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {
                    criptoSelezionata?.let { c ->
                        val q = if (usaValore) {
                            val v = valoreinEuro.toDoubleOrNull() ?: 0.0
                            if (c.valoreEur != null && c.valoreEur > 0) v / c.valoreEur else 0.0
                        } else quantita.toDoubleOrNull() ?: 0.0

                        when (tipoOperazione) {
                            "Acquista" -> viewModel.acquistaQuantita(c.simbolo, q)
                            "Vendi" -> viewModel.vendiQuantita(c.simbolo, q)
                            "Scambia" -> {
                                criptoTarget?.let { t ->
                                    viewModel.scambiaQuantita(c.simbolo, t.simbolo, q)
                                }
                            }
                        }
                    }
                }) { Text(tipoOperazione, fontSize = fontSize.sp) }
            }

            Spacer(Modifier.height(16.dp))
            if (messaggio.isNotBlank()) {
                Text(messaggio, fontSize = fontSize.sp, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.height(16.dp))

// Intestazione colonne
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Cripto",
                    fontSize = fontSize.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Text(
                    "Quantità",
                    fontSize = fontSize.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Valore",
                    fontSize = fontSize.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(8.dp))
            // Lista portafoglio in basso
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(portafoglio) { c ->
                    val valoreTotale = (c.valoreEur ?: 0.0) * c.quantita

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Simbolo a sinistra
                            Text(
                                c.simbolo.uppercase(),
                                fontSize = fontSize.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )

                            // Quantità centrata
                            Text(
                                String.format("%.4f", c.quantita),
                                fontSize = fontSize.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )

                            // Valore totale a destra
                            Text(
                                "€${String.format("%.2f", valoreTotale)}",
                                fontSize = fontSize.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuCripto(
    items: List<CriptoPosseduta>,
    selected: CriptoPosseduta?,
    onSelect: (CriptoPosseduta) -> Unit,
    fontSize: Float,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected?.simbolo?.uppercase() ?: "Seleziona", fontSize = fontSize.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.simbolo.uppercase(), fontSize = fontSize.sp) },
                    onClick = {
                        onSelect(c)
                        expanded = false
                    }
                )
            }
        }
    }
}
