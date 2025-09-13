package com.sb.mycriptoanalisi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sb.mycriptoanalisi.SetupAvanzatoScreen

class SetupAvanzatoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetupAvanzatoScreen(onBack = { finish() })
        }
    }
}

