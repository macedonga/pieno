package dev.ceccon.pieno.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore("pieno_prefs")

// Preferenze varie e leggere: il raggio (km) dell'ultima ricerca "distributore piu'
// economico" e le targhe gia' aggiunte a Google Wallet (per "Aggiungi" vs "Aggiorna").
class PrefsStore(private val context: Context) {

    private val radiusKey = intPreferencesKey("search_radius_km")
    private val walletAddedKey = stringSetPreferencesKey("wallet_added_targhe")

    fun radiusFlow(): Flow<Int> = context.prefsDataStore.data.map { it[radiusKey] ?: DEFAULT_RADIUS_KM }

    suspend fun radius(): Int = context.prefsDataStore.data.first()[radiusKey] ?: DEFAULT_RADIUS_KM

    suspend fun setRadius(km: Int) {
        context.prefsDataStore.edit { it[radiusKey] = km.coerceIn(1, 100) }
    }

    // Targhe il cui pass e' gia' stato aggiunto a Wallet (per cambiare l'etichetta).
    fun walletAddedFlow(): Flow<Set<String>> =
        context.prefsDataStore.data.map { it[walletAddedKey] ?: emptySet() }

    suspend fun markWalletAdded(targa: String) {
        context.prefsDataStore.edit { it[walletAddedKey] = (it[walletAddedKey] ?: emptySet()) + targa }
    }

    companion object {
        const val DEFAULT_RADIUS_KM = 15
    }
}
