package dev.ceccon.pieno.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Ordine, nomi e colori personalizzati per targa. Le tessere non ancora ordinate
// seguono in coda, nell'ordine di provenienza.
@Serializable
data class TessereSettings(
    val order: List<String> = emptyList(),
    val names: Map<String, String> = emptyMap(),
    val colors: Map<String, String> = emptyMap(),
    // Carburante scelto dall'utente per una targa (utile per le condivise, che
    // non lo portano nel QR). Sovrascrive quello dedotto.
    val fuels: Map<String, String> = emptyMap(),
)

private val Context.tessereDataStore: DataStore<Preferences> by preferencesDataStore("pieno_tessere")

class TessereStore(private val context: Context) {

    private val key = stringPreferencesKey("settings")
    private val json = Json { ignoreUnknownKeys = true }

    fun flow(): Flow<TessereSettings> = context.tessereDataStore.data.map { decode(it[key]) }

    suspend fun get(): TessereSettings = decode(context.tessereDataStore.data.first()[key])

    suspend fun setOrder(order: List<String>) = write(get().copy(order = order))

    suspend fun setName(targa: String, name: String) {
        val s = get()
        val m = s.names.toMutableMap()
        val trimmed = name.trim()
        if (trimmed.isBlank()) m.remove(targa) else m[targa] = trimmed
        write(s.copy(names = m))
    }

    suspend fun setColor(targa: String, hex: String?) {
        val s = get()
        val m = s.colors.toMutableMap()
        if (hex.isNullOrBlank()) m.remove(targa) else m[targa] = hex
        write(s.copy(colors = m))
    }

    suspend fun setFuel(targa: String, fuel: String?) {
        val s = get()
        val m = s.fuels.toMutableMap()
        if (fuel.isNullOrBlank()) m.remove(targa) else m[targa] = fuel
        write(s.copy(fuels = m))
    }

    private suspend fun write(s: TessereSettings) {
        context.tessereDataStore.edit { it[key] = json.encodeToString(s) }
    }

    private fun decode(s: String?): TessereSettings =
        if (s.isNullOrBlank()) TessereSettings()
        else runCatching { json.decodeFromString<TessereSettings>(s) }.getOrDefault(TessereSettings())
}
