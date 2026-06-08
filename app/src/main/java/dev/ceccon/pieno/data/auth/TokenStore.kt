package dev.ceccon.pieno.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore("pieno_auth")

// Conserva lo stato di autenticazione (AppAuth AuthState serializzato in JSON),
// cifrato con CryptoBox. Una sola chiave: lo stato completo.
class TokenStore(private val context: Context) {

    private val stateKey = stringPreferencesKey("auth_state")

    suspend fun saveState(json: String) {
        val enc = CryptoBox.encrypt(json)
        context.authDataStore.edit { it[stateKey] = enc }
    }

    suspend fun loadState(): String? {
        val enc = context.authDataStore.data.first()[stateKey] ?: return null
        return runCatching { CryptoBox.decrypt(enc) }.getOrNull()
    }

    suspend fun clear() {
        context.authDataStore.edit { it.remove(stateKey) }
    }

    fun isLoggedIn(): Flow<Boolean> =
        context.authDataStore.data.map { it[stateKey] != null }
}
