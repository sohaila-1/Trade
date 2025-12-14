package com.example.tradeconnect.repository

import com.example.tradeconnect.model.AppUser
import com.google.firebase.auth.FirebaseUser

interface IAuthRepository {
    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        onResult: (Boolean, String?) -> Unit
    )
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun logout()

    fun getCurrentUser(): FirebaseUser?

    // ❗️ Ici tu avais : fun getCurrentUserModel()
    // ❗️ qui renvoie Unit → provoque toutes les erreurs
    fun getCurrentUserModel(): AppUser?
}
