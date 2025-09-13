package com.sb.mycriptoanalisi

import PortafoglioViewModelFactory
import UtilityComuni.nascondiTastiera
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sb.mycriptoanalisi.data.CriptoDatabase
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import com.sb.mycriptoanalisi.ui.components.AppButton
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import com.sb.mycriptoanalisi.viewmodel.PortafoglioViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PortafoglioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = CriptoDatabase.getDatabase(application)
        val repository = CriptoRepository(db.criptoDao())

        setContent {
            val portafoglioViewModel: PortafoglioViewModel = viewModel(
                factory = PortafoglioViewModelFactory(application, repository)
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    application,
                    PortafoglioRepositoryLocale(application)
                )
            )

            val themeDark by settingsViewModel.themeDark.collectAsState()
            val fontSize by settingsViewModel.fontSize.collectAsState()

            MyCriptoAnalisiTheme(darkTheme = themeDark) {
                PortafoglioScreen(
                    viewModel = portafoglioViewModel,
                    fontSize = fontSize,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun PortafoglioScreen(
    viewModel: PortafoglioViewModel,
    fontSize: Float,
    settingsViewModel: SettingsViewModel,
) {
    val scaffoldState = remember { SnackbarHostState() } // Material3
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var cripto by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val criptoList by viewModel.portafoglio.collectAsState()
    val listaCoinGecko by viewModel.listaCoinGecko.collectAsState()
        val messaggio by viewModel.messaggio.observeAsState()


    nascondiTastiera(context)

    // Mostra messaggi come Snackbar
    LaunchedEffect(messaggio) {
        messaggio?.let { msg ->
            scope.launch {
                scaffoldState.showSnackbar(msg)
            }
        }
    }

    // Carica lista CoinGecko all'avvio
    LaunchedEffect(Unit) {
        viewModel.caricaListaCoinGecko()
    }

    Scaffold(
        topBar = { MyTopBar(titleText = "📊 My Wallet", backToMain = true) },
        snackbarHost = { SnackbarHost(hostState = scaffoldState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Input cripto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(BorderStroke(1.dp, Color(0xFFD3D3D3)), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (cripto.isEmpty()) {
                    Text(
                        text = "Nome Cripto",
                        fontSize = fontSize.sp,
                        color = Color(0xFFA9A9A9),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                BasicTextField(
                    value = cripto,
                    onValueChange = { cripto = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = fontSize.sp, color = Color(0xFF696969)),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterStart)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input quantità
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(BorderStroke(1.dp, Color(0xFFD3D3D3)), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (amount.isEmpty()) {
                    Text(
                        text = "Quantità",
                        fontSize = fontSize.sp,
                        color = Color(0xFFA9A9A9),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                BasicTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = fontSize.sp, color = Color(0xFF696969)),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterStart),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val bottoneAbilitato = listaCoinGecko.isNotEmpty()
            AppButton(
                text = if (bottoneAbilitato) "➕ Aggiungi al Portafoglio" else "⏳ Caricamento cripto…",

                onClick = {
                    nascondiTastiera(context)

                    val quant = amount.replace(".", "").replace(',', '.').toDoubleOrNull()
                    focusManager.clearFocus() // chiude tastiera
                    if (cripto.isNotBlank() && quant != null && quant >= 0) {
                        viewModel.inserisciDaInput(cripto, quant)
                        viewModel.aggiornaPrezziDaCoinGecko() // 🔄 aggiorna subito dopo l'inserimento
                        cripto = ""
                        amount = ""

                    } else {
                        scope.launch { scaffoldState.showSnackbar("⚠️ Inserisci dati validi") }
                    }

                },
                enabled = bottoneAbilitato,
                fontSize = fontSize
            )
            viewModel.aggiornaPrezziDaCoinGecko()
            Spacer(modifier = Modifier.height(8.dp))

            AppButton(
                text = "🌐 Aggiorna da CoinGecko",
                onClick = { viewModel.aggiornaPrezziDaCoinGecko() },
                fontSize = fontSize
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Valore totale portafoglio
            val valoreTotaleEur = criptoList.sumOf { (it.valoreEur ?: 0.0) * it.quantita }
            val valoreTotaleUsd = criptoList.sumOf { it.valoreUsd?.times(it.quantita) ?: 0.0 }

            var mostraEuro by remember { mutableStateOf(true) }
            val formatter = NumberFormat.getNumberInstance(Locale.ITALY).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            val testoValore = if (mostraEuro) {
                "💶 Portafoglio (EUR): €${formatter.format(valoreTotaleEur)}"
            } else {
                "💵 Portafoglio (USD): $${formatter.format(valoreTotaleUsd)}"
            }
            Text(
                text = testoValore,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { mostraEuro = !mostraEuro }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista portafoglio scrollabile
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(criptoList) { c ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFD3D3D3), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${c.nome} (${c.simbolo.uppercase()})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Quantità: ${c.quantita}",
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "€${formatValore(c.valoreEur)}",
                                fontSize = 10.sp
                            )
                            Text(
                                text = "$${formatValore(c.valoreUsd)}",
                                fontSize = 10.sp
                            )
                            Button(
                                onClick = { viewModel.delete(c) },
                                modifier = Modifier.height(24.dp),
                                contentPadding = PaddingValues(vertical = 2.dp, horizontal = 6.dp)
                            ) {
                                Text("❌", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Funzione di formattazione valori
fun formatValore(valore: Double?, minDecimali: Int = 4, maxDecimali: Int = 8): String {
    val v = valore ?: 0.0
    if (v == 0.0) return "%.${minDecimali}f".format(0.0)
    val decimali = when {
        v >= 1 -> 2
        v >= 0.01 -> 4
        v >= 0.0001 -> 6
        else -> maxDecimali
    }
    return "%.${decimali}f".format(v)
}
