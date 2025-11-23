package com.example.tradeconnect.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private const val PREFS_NAME = "user_prefs"
private val Context.dataStore by preferencesDataStore(name = PREFS_NAME)

class UserPreferences(private val context: Context) : IUserPreferences {
    private val REMEMBER_ME = booleanPreferencesKey("remember_me")

    override val rememberMeFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[REMEMBER_ME] ?: false }

    override suspend fun setRememberMe(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[REMEMBER_ME] = value
        }
    }
}