package dev.ceccon.pieno.data.wallet

import android.content.Context
import dev.ceccon.pieno.BuildConfig
import dev.ceccon.pieno.data.model.Tessera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

// "Aggiungi a Google Wallet". La firma del pass avviene su un Worker Cloudflare
// (vedi wallet-worker/) che custodisce la chiave del service account: l'app chiede
// il JWT e lo passa a Google Pay. Cosi' la chiave non sta MAI nell'APK e tutti
// usano lo stesso issuer senza credenziali proprie. Senza WALLET_SIGNER_URL la
// funzione e' disabilitata (isConfigured = false).
class WalletManager(private val context: Context) {

    private val http = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun isConfigured(): Boolean = BuildConfig.WALLET_SIGNER_URL.isNotBlank()

    // Chiede al Worker il JWT "save to wallet" per la tessera. Restituisce null se
    // non configurato o in caso di errore di rete.
    suspend fun fetchSaveJwt(tessera: Tessera): String? = withContext(Dispatchers.IO) {
        val url = BuildConfig.WALLET_SIGNER_URL
        if (url.isBlank()) return@withContext null
        val header = tessera.etichetta?.takeIf { it.isNotBlank() } ?: tessera.intestatario
        val hexBackgroundColor = tessera.colore?.takeLast(6)?.takeIf { it.length == 6 }?.let { "#$it" }
        val body = buildJsonObject {
            put("targa", tessera.targa)
            put("qrPayload", tessera.qrPayload)
            put("header", header)
            if (hexBackgroundColor != null) put("hexBackgroundColor", hexBackgroundColor)
        }.toString().toRequestBody(JSON_MEDIA)
        val request = Request.Builder().url(url).post(body).build()
        runCatching {
            http.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val text = resp.body?.string() ?: return@use null
                json.parseToJsonElement(text).jsonObject["jwt"]?.jsonPrimitive?.content
            }
        }.getOrNull()
    }

    companion object {
        const val REQUEST_CODE = 4321
        private val JSON_MEDIA = "application/json".toMediaType()
    }
}
