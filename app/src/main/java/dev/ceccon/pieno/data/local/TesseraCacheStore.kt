package dev.ceccon.pieno.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Copia persistente delle tessere proprie (QR firmato, valido ~10 giorni). Serve
// a mostrare comunque il QR alla pompa anche se la rete manca o il token non si
// rinnova: il compito primario dell'app non deve mai bloccarsi.
@Serializable
data class CachedTessera(
    val targa: String,
    val intestatario: String,
    val payload: String,
    val validUntil: Long,
    val carburante: String? = null,
)

private val Context.tesseraCacheDataStore: DataStore<Preferences> by preferencesDataStore("pieno_tessera_cache")

class TesseraCacheStore(private val context: Context) {

    private val key = stringPreferencesKey("proprie")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun save(list: List<CachedTessera>) {
        context.tesseraCacheDataStore.edit { it[key] = json.encodeToString(list) }
    }

    suspend fun load(): List<CachedTessera> {
        val s = context.tesseraCacheDataStore.data.first()[key] ?: return emptyList()
        return runCatching { json.decodeFromString<List<CachedTessera>>(s) }.getOrDefault(emptyList())
    }
}
