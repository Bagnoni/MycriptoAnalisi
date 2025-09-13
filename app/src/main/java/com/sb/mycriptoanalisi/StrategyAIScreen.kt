package com.sb.mycriptoanalisi

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sb.mycriptoanalisi.ui.theme.MyCriptoAnalisiTheme
import com.sb.mycriptoanalisi.viewmodel.PortafoglioViewModel
import com.sb.mycriptoanalisi.viewmodel.SettingsViewModel

@Composable
fun StrategieAIScreen(
    settingsViewModel: SettingsViewModel,
    portafoglioViewModel: PortafoglioViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val fontSize by settingsViewModel.fontSize.collectAsState()
    val themeDark by settingsViewModel.themeDark.collectAsState()
    val portafoglio by portafoglioViewModel.portafoglio.collectAsState()

    val percentuali = remember(portafoglio) {
        portafoglioViewModel.calcolaPercentuali().sortedByDescending { it.second }
    }

    val composizione = remember(percentuali) {
        percentuali.joinToString("\n") { (cripto, percentuale) ->
            val nome = cripto.nome.padEnd(14)
            val perc = "%.2f".format(percentuale).padStart(6)
            "- $nome: $perc%"
        }
    }

    var domandaAI by remember(composizione) {
        mutableStateOf(
            """
            |Ecco la composizione attuale del mio portafoglio:
            |$composizione
            |
            |Vorrei un'analisi basata sull'andamento storico del mercato cripto 
            |negli ultimi 6 mesi e confrontata con i cicli storici del mercato cripto.
            |Il mio obiettivo è: accumulo a medio termine con una tipologia di investimento 
            |a rischio bilanciato.
            |
            |Ti chiedo:
            |- Se la composizione è bilanciata rispetto al trend attuale
            |- Se ci sono segnali di rischio od eccessiva esposizione
            |- Quali strategie suggeriscono gli analisti per i prossimi mesi
            |- Se conviene alleggerire, accumulare o diversificare
            """.trimMargin()
        )
    }

    var rispostaAI by remember { mutableStateOf("") }

    var usaOpenAI by remember { mutableStateOf(true) }
    var usaAzureAI by remember { mutableStateOf(false) }
    var usaND1 by remember { mutableStateOf(false) }
    var usaND2 by remember { mutableStateOf(false) }

    MyCriptoAnalisiTheme(darkTheme = themeDark) {
        Scaffold(
            topBar = { MyTopBar(titleText = "🤖 Strategie AI", backToMain = true) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text("Benvenuto nella sezione Strategie AI!", style = MaterialTheme.typography.titleLarge, fontSize = fontSize.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("📨 Domanda generata", fontSize = fontSize.sp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = domandaAI,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.verticalScroll(  rememberScrollState())
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        clipboardManager.setText(AnnotatedString(domandaAI))
                        Toast.makeText(context, "✅ Domanda copiata", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("📤 Copia", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("🤖 Seleziona AI da interrogare", fontSize = fontSize.sp)

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = usaOpenAI, onCheckedChange = { usaOpenAI = it })
                        Text("OpenAI", fontSize = fontSize.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = usaAzureAI, onCheckedChange = { usaAzureAI = it })
                        Text("Azure AI", fontSize = fontSize.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = usaND1, onCheckedChange = { usaND1 = it })
                        Text("ND1 (da configurare)", fontSize = fontSize.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = usaND2, onCheckedChange = { usaND2 = it })
                        Text("ND2 (da configurare)", fontSize = fontSize.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (domandaAI.isBlank()) {
                            Toast.makeText(context, "⚠️ Inserisci una domanda valida", Toast.LENGTH_SHORT).show()
                        } else {
                            rispostaAI = "🧠 Risposta simulata: analisi in corso..."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📤 Invia richiesta", fontSize = fontSize.sp)
                }

                if (rispostaAI.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("🧠 Risposta AI", fontSize = fontSize.sp)
                    Text(rispostaAI, fontSize = fontSize.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, SetupAvanzatoActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⚙️ Setup Avanzato", fontSize = fontSize.sp)
                }
            }
        }
    }
}