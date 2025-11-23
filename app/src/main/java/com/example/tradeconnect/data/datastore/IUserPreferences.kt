package com.example.tradeconnect.data.datastore

import kotlinx.coroutines.flow.Flow

interface IUserPreferences {
    val rememberMeFlow: Flow<Boolean>
    suspend fun setRememberMe(value: Boolean)
}