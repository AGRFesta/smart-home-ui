package org.agrfesta.sh.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
actual fun AuthContent(onTokenSaved: (String) -> Unit) {
    var token by remember { mutableStateOf("") }
    Column {
        TextField(
            value = token,
            onValueChange = { token = it },
            modifier = Modifier.testTag("auth_token_field")
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { if (token.isNotBlank()) onTokenSaved(token) },
            modifier = Modifier.testTag("auth_save_button")
        ) {
            Text("Salva")
        }
    }
}
