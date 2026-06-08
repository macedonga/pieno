package dev.ceccon.pieno.data

import android.content.Context
import dev.ceccon.pieno.core.AppConfig
import dev.ceccon.pieno.data.api.ApiInterceptor
import dev.ceccon.pieno.data.api.PienoApi
import dev.ceccon.pieno.data.auth.AuthManager
import dev.ceccon.pieno.data.auth.TokenStore
import dev.ceccon.pieno.data.local.CondiviseStore
import dev.ceccon.pieno.data.local.PrefsStore
import dev.ceccon.pieno.data.local.StationFavoritesStore
import dev.ceccon.pieno.data.local.TesseraCacheStore
import dev.ceccon.pieno.data.local.TessereStore
import dev.ceccon.pieno.data.local.ThemeStore
import dev.ceccon.pieno.data.repo.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class PienoContainer(context: Context) {

    // Scope legato all'app (non alla composizione): per refresh/posizione/ricerca
    // avviati da callback o lifecycle, che non devono crashare se la schermata
    // viene lasciata ("The coroutine scope left the composition").
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val tokenStore = TokenStore(context.applicationContext)
    val authManager = AuthManager(context.applicationContext, tokenStore)
    val condiviseStore = CondiviseStore(context.applicationContext)
    val tessereStore = TessereStore(context.applicationContext)
    val themeStore = ThemeStore(context.applicationContext)
    val prefsStore = PrefsStore(context.applicationContext)
    val tesseraCacheStore = TesseraCacheStore(context.applicationContext)
    val stationFavoritesStore = StationFavoritesStore(context.applicationContext)

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(ApiInterceptor { authManager.blockingFreshToken() })
        // BASIC: registra solo la riga di richiesta, mai gli header (che contengono il token).
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.API_BASE_URL)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: PienoApi = retrofit.create(PienoApi::class.java)
    val repository = Repository(api, authManager, condiviseStore, tessereStore, tesseraCacheStore, stationFavoritesStore)

    init {
        // Tiene calda la cache dei preferiti fin dall'avvio, cosi' la lista li mostra
        // subito (niente inserimento tardivo che sposta lo scroll).
        appScope.launch { repository.stationFavoritesFlow().collect {} }
    }
}
