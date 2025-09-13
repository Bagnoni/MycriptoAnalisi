package com.sb.mycriptoanalisi

import PortafoglioViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sb.mycriptoanalisi.data.CriptoDatabase
import com.sb.mycriptoanalisi.data.CriptoRepository
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import com.sb.mycriptoanalisi.viewmodel.PortafoglioViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModelFactory
import java.text.NumberFormat
import java.util.Locale


class DashboardActivity : ComponentActivity() {
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

                DashboardScreen(viewModel = viewModel, fontSize = fontSize)
            }
        }
    }
}


@Composable
fun DashboardScreen(viewModel: PortafoglioViewModel, fontSize: Float) {
    val portafoglio by viewModel.portafoglio.collectAsState()
    var usaEuro by remember { mutableStateOf(true) }      // toggle valuta
    var mostraValore by remember { mutableStateOf(false) } // toggle V / %

    val percentuali by remember(portafoglio) {
        mutableStateOf(
            viewModel.calcolaPercentuali().sortedByDescending { it.second }
        )
    }

    // Valore totale portafoglio
    val valoreTotaleEur = portafoglio.sumOf { (it.valoreEur ?: 0.0) * it.quantita }
    val valoreTotaleUsd = portafoglio.sumOf { (it.valoreUsd ?: 0.0) * it.quantita }

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.ITALY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Scaffold(
        topBar = { MyTopBar(titleText = "📊 Dashboard", backToMain = true) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            // 🔹 Totale portafoglio in alto (clic per cambiare valuta)
            val testoValore = if (usaEuro) {
                "💶 Portafoglio (EUR): €${formatter.format(valoreTotaleEur)}"
            } else {
                "💵 Portafoglio (USD): $${formatter.format(valoreTotaleUsd)}"
            }
            Text(
                text = testoValore,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { usaEuro = !usaEuro },
                textAlign = TextAlign.Center
            )

            // 🔹 Intestazioni
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bottone fisso €/$ → toggle valuta
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable { usaEuro = !usaEuro }
                ) {
                    Text(
                        text = "€/$",
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bottone fisso V/% → toggle colonna destra
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable { mostraValore = !mostraValore }
                ) {
                    Text(
                        text = "V/%",
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (percentuali.isEmpty()) {
                Text(
                    "Nessun dato disponibile",
                    fontSize = fontSize.sp,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(percentuali) { (cripto, percentuale) ->
                        val valoreTotaleCripto =
                            if (usaEuro) (cripto.valoreEur ?: 0.0) * cripto.quantita
                            else (cripto.valoreUsd ?: 0.0) * cripto.quantita

                        val quotazioneUnitaria = if (usaEuro) cripto.valoreEur else cripto.valoreUsd

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    // Sinistra: quotazione unitaria
                                    Text(
                                        text = "${formatValore(quotazioneUnitaria)} ${if (usaEuro) "€" else "$"}",
                                        fontSize = fontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )

                                    // Centro: simbolo cripto
                                    Text(
                                        text = cripto.simbolo.uppercase(),
                                        fontSize = fontSize.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.align(Alignment.Center)
                                    )

                                    // Destra: valore o percentuale
                                    val testoDestro = if (mostraValore) {
                                        "${formatValore(valoreTotaleCripto)} ${if (usaEuro) "€" else "$"}"
                                    } else {
                                        "${"%.2f".format(percentuale)}%"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .align(Alignment.CenterEnd)
                                    ) {
                                        Text(
                                            text = testoDestro,
                                            fontSize = fontSize.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Barra sempre in percentuale
                                LinearProgressIndicator(
                                    progress = { (percentuale / 100).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.LightGray,
                                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

