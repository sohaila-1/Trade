package com.example.tradeconnect.util

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun Base64ProfileImage(
    base64String: String,
    modifier: Modifier = Modifier
) {
    // Use remember to decode bitmap only once
    val bitmap = remember(base64String) {
        decodeBase64ToBitmap(base64String)
    }

    if (bitmap != null) {
        // Convert Android Bitmap to Compose ImageBitmap
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Profile",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Fallback to default avatar if bitmap is null
        DefaultAvatar(
            letter = "?",
            modifier = modifier
        )
    }
}

@Composable
fun DefaultAvatar(
    letter: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// Helper function to decode base64
private fun decodeBase64ToBitmap(base64String: String): android.graphics.Bitmap? {
    return try {
        // Extract base64 data (remove "data:image/jpeg;base64," prefix)
        val base64Data = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }

        // Decode base64 to byte array using Android's Base64 class
        val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)

        // Convert byte array to bitmap using BitmapFactory
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        null
    }
}