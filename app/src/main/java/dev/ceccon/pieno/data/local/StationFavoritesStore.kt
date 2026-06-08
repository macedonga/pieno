package dev.ceccon.pieno.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.stationFavDataStore: DataStore<Preferences> by preferencesDataStore("pieno_station_fav")

// Distributori preferiti (per chiave stabile, vedi Domain.stationKey). I preferiti
// compaiono in cima alla lista.
class StationFavoritesStore(private val context: Context) {

    private val key = stringSetPreferencesKey("favorites")

    fun flow(): Flow<Set<String>> = context.stationFavDataStore.data.map { it[key] ?: emptySet() }

    suspend fun toggle(stationKey: String) {
        context.stationFavDataStore.edit { prefs ->
            val cur = prefs[key] ?: emptySet()
            prefs[key] = if (stationKey in cur) cur - stationKey else cur + stationKey
        }
    }
}
