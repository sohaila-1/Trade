// app/src/main/java/com/example/tradeconnect/data/repository/FakeUserRepository.kt
package com.example.tradeconnect.data.repository

import com.example.tradeconnect.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : IUserRepository {

    private val fakeUsers = listOf(
        User(
            uid = "user1",
            username = "john_doe",
            email = "john@example.com",
            mobile = "+33612345678",
            followers = listOf("user2", "user3"),
            following = listOf("user2")
        ),
        User(
            uid = "user2",
            username = "jane_smith",
            email = "jane@example.com",
            mobile = "+33698765432",
            followers = listOf("user1"),
            following = listOf("user1", "user3")
        ),
        User(
            uid = "user3",
            username = "bob_wilson",
            email = "bob@example.com",
            mobile = "+33611223344",
            followers = listOf("user2"),
            following = emptyList()
        )
    )

    private var currentUserId: String? = "user1"
    private val followingSet = mutableSetOf("user2") // user1 suit user2

    override fun getCurrentUserId(): String? = currentUserId

    override suspend fun getUserById(uid: String): Result<User?> {
        val user = fakeUsers.find { it.uid == uid }
        return Result.success(user)
    }

    override fun observeUser(uid: String): Flow<User?> {
        val user = fakeUsers.find { it.uid == uid }
        return flowOf(user)
    }

    override fun observeCurrentUser(): Flow<User?> {
        val user = fakeUsers.find { it.uid == currentUserId }
        return flowOf(user)
    }

    override fun getAllUsers(): Flow<List<User>> {
        val users = fakeUsers.filter { it.uid != currentUserId }
        return flowOf(users)
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        val results = fakeUsers.filter {
            it.uid != currentUserId &&
                    (it.username.contains(query, ignoreCase = true) ||
                            it.email.contains(query, ignoreCase = true))
        }
        return Result.success(results)
    }

    override suspend fun followUser(targetUserId: String): Result<Unit> {
        followingSet.add(targetUserId)
        return Result.success(Unit)
    }

    override suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        followingSet.remove(targetUserId)
        return Result.success(Unit)
    }

    override suspend fun isFollowing(targetUserId: String): Boolean {
        return followingSet.contains(targetUserId)
    }

    override suspend fun getFollowers(userId: String): Result<List<User>> {
        val user = fakeUsers.find { it.uid == userId }
        val followers = fakeUsers.filter { user?.followers?.contains(it.uid) == true }
        return Result.success(followers)
    }

    override suspend fun getFollowing(userId: String): Result<List<User>> {
        val user = fakeUsers.find { it.uid == userId }
        val following = fakeUsers.filter { user?.following?.contains(it.uid) == true }
        return Result.success(following)
    }

    override suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> {
        return Result.success(Unit)
    }
}
