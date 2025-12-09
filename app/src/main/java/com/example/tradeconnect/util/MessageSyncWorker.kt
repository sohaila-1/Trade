package com.example.tradeconnect.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tradeconnect.data.local.AppDatabase
import com.example.tradeconnect.data.remote.FirebaseMessagingService
import com.example.tradeconnect.data.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val firebaseService = FirebaseMessagingService(
                FirebaseDatabase.getInstance(),
                FirebaseAuth.getInstance()
            )
            val repository = MessageRepository(database, firebaseService)

            repository.syncPendingMessages()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}