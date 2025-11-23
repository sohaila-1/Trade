package com.example.tradeconnect.data.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeUserPreferences : IUserPreferences {
    override val rememberMeFlow: Flow<Boolean> = flow { emit(true) }
    override suspend fun setRememberMe(value: Boolean) { /* no-op */ }
}