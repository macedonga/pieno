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

@Serializable
data class CondivisaEntry(
    val targa: String,
    val id: String? = null,
    val payload: String,
    val endEpochDay: Long? = null,
    val verificata: Boolean = false,
)

private val Context.condiviseDataStore: DataStore<Preferences> by preferencesDataStore("pieno_condivise")

// Tessere condivise importate localmente (auto non intestate all'utente).
// Feature 100% locale: nessun server, come nel client Python.
class CondiviseStore(private val context: Context) {

    private val key = stringPreferencesKey("condivise_json")
    private val json = Json { ignoreUnknownKeys = true }

    fun flow(): Flow<List<CondivisaEntry>> =
        context.condiviseDataStore.data.map { decode(it[key]) }

    suspend fun list(): List<CondivisaEntry> =
        decode(context.condiviseDataStore.data.first()[key])

    suspend fun upsert(entry: CondivisaEntry) {
        val current = list().filterNot { it.targa == entry.targa }
        write(current + entry)
    }

    suspend fun remove(targa: String) {
        write(list().filterNot { it.targa == targa })
    }

    private suspend fun write(entries: List<CondivisaEntry>) {
        val encoded = json.encodeToString(entries)
        context.condiviseDataStore.edit { it[key] = encoded }
    }

    private fun decode(s: String?): List<CondivisaEntry> =
        if (s.isNullOrBlank()) emptyList()
        else runCatching { json.decodeFromString<List<CondivisaEntry>>(s) }.getOrDefault(emptyList())
}
