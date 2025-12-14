package com.example.tradeconnect.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun rememberFormattedTime(timestamp: Long): String {
    return remember(timestamp) {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
}
