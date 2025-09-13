package com.sb.mycriptoanalisi.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    fontSize: Float,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(text = text, fontSize = fontSize.sp)
    }
}
