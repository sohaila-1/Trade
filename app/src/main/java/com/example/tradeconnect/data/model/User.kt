package com.example.tradeconnect.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profileImageUrl: String = "",
    val mobile: String = "",

    // Statut en ligne
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),

    // Follow system
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),

    // Métadonnées
    val createdAt: Long = System.currentTimeMillis(),
    val bio: String = ""
) {
    // Compteurs calculés
    val followersCount: Int get() = followers.size
    val followingCount: Int get() = following.size

    // Nom complet
    val fullName: String get() = "$firstName $lastName".trim()

    // Nom d'affichage (fullName ou username si vide)
    val displayName: String get() = fullName.ifEmpty { username }

    // Vérifier si cet utilisateur suit un autre
    fun isFollowing(userId: String): Boolean = following.contains(userId)

    // Vérifier si cet utilisateur est suivi par un autre
    fun isFollowedBy(userId: String): Boolean = followers.contains(userId)

    // Formater "dernière connexion"
    fun getLastSeenText(): String {
        if (isOnline) return "En ligne"

        val now = System.currentTimeMillis()
        val diff = now - lastSeen

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "À l'instant"
            minutes < 60 -> "Il y a $minutes min"
            hours < 24 -> "Il y a $hours h"
            days < 7 -> "Il y a $days j"
            else -> "Il y a longtemps"
        }
    }

    // Convertir en Map pour Firestore
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "username" to username,
        "email" to email,
        "firstName" to firstName,
        "lastName" to lastName,
        "profileImageUrl" to profileImageUrl,
        "mobile" to mobile,
        "isOnline" to isOnline,
        "lastSeen" to lastSeen,
        "followers" to followers,
        "following" to following,
        "createdAt" to createdAt,
        "bio" to bio
    )

    companion object {
        // Créer depuis un Map Firestore
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                uid = map["uid"] as? String ?: "",
                username = map["username"] as? String ?: "",
                email = map["email"] as? String ?: "",
                firstName = map["firstName"] as? String ?: "",
                lastName = map["lastName"] as? String ?: "",
                profileImageUrl = map["profileImageUrl"] as? String ?: "",
                mobile = map["mobile"] as? String ?: "",
                isOnline = map["isOnline"] as? Boolean ?: false,
                lastSeen = map["lastSeen"] as? Long ?: System.currentTimeMillis(),
                followers = (map["followers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                following = (map["following"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                bio = map["bio"] as? String ?: ""
            )
        }
    }
}