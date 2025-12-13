package com.example.tradeconnect.data.model

/**
 * User stocké / lu depuis Firebase (Firestore / Realtime DB)
 */
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val mobile: String = "",
    val profileImageUrl: String = ""
) {
    // 🔥 CONSTRUCTEUR VIDE POUR FIRESTORE
    constructor() : this("", "", "", "", "")
}

