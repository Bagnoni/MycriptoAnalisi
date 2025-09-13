package com.sb.mycriptoanalisi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val savedVersion = prefs.getInt("splash_version", -1)
        val currentVersion = packageManager.getPackageInfo(packageName, 0).versionCode

        if (savedVersion == currentVersion) {
            // Splash già disattivato → vai direttamente al Main
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            setContent {
                SplashScreen(
                    onContinue = { dontShowAgain ->
                        if (dontShowAgain) {
                            prefs.edit { putInt("splash_version", currentVersion) }
                        }
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onContinue: (Boolean) -> Unit) {
    var dontShowAgain by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Benvenuto nell'app!", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = dontShowAgain,
                onCheckedChange = { dontShowAgain = it }
            )
            Text("Non mostrare più")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onContinue(dontShowAgain) }) {
            Text("Continua")
        }
    }
}
