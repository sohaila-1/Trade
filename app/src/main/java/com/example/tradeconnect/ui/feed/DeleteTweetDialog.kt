package com.example.tradeconnect.ui.feed

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color

@Composable
fun DeleteTweetDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer le tweet") },
        text = {
            Text(
                "Cette action est irréversible.",
                style = MaterialTheme.typography.body1
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Supprimer", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
