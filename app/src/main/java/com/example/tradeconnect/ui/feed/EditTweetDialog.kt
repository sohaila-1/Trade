package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun EditTweetDialog(
    oldContent: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newText by remember { mutableStateOf(oldContent) }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = { Text("Modifier le tweet") },

        text = {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Votre tweet…") }
            )
        },

        confirmButton = {
            TextButton(onClick = { onSave(newText) }) {
                Text("Enregistrer")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
