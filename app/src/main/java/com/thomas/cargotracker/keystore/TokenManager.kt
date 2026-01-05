package com.thomas.cargotracker.keystore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class TokenManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val MOCK_EMAIL_KEY = stringPreferencesKey("mock_logged_in_email")
    }

    suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = token
        }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun getAccessToken(): String? {
        val preferences = dataStore.data.first()
        return preferences[ACCESS_TOKEN_KEY]
    }

    suspend fun getRefreshToken(): String? {
        val preferences = dataStore.data.first()
        return preferences[REFRESH_TOKEN_KEY]
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(MOCK_EMAIL_KEY)
        }
    }

    suspend fun hasToken(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[ACCESS_TOKEN_KEY] != null && preferences[REFRESH_TOKEN_KEY] != null
    }

    suspend fun saveMockEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[MOCK_EMAIL_KEY] = email
        }
    }

    suspend fun getMockEmail(): String? {
        val preferences = dataStore.data.first()
        return preferences[MOCK_EMAIL_KEY]
    }

    suspend fun clearMockEmail() {
        dataStore.edit { preferences ->
            preferences.remove(MOCK_EMAIL_KEY)
        }
    }
}

