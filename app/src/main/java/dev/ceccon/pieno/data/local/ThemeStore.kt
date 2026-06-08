package dev.ceccon.pieno.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore("pieno_theme")

class ThemeStore(private val context: Context) {

    private val key = stringPreferencesKey("mode")

    fun flow(): Flow<String> = context.themeDataStore.data.map { it[key] ?: SYSTEM }

    suspend fun set(mode: String) {
        context.themeDataStore.edit { it[key] = mode }
    }

    companion object {
        const val SYSTEM = "system"
        const val LIGHT = "light"
        const val DARK = "dark"
    }
}
