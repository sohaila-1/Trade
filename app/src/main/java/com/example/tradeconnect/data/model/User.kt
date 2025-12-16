package com.example.tradeconnect.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val mobile: String = "",
    val bio: String = "",

    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),

    // Follow system
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),

    val createdAt: Long = System.currentTimeMillis()
) {
    // Compteurs calculés
    val followersCount: Int get() = followers.size
    val followingCount: Int get() = following.size

    // Vérifier si cet utilisateur suit quelqu'un
    fun isFollowing(userId: String): Boolean = following.contains(userId)

    // Vérifier si cet utilisateur est suivi par quelqu'un
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
}